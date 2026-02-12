package com.mookplayer.com

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var chooseButton: Button

    private var currentUri: Uri? = null

    private val prefs by lazy {
        getSharedPreferences("mookplayer_prefs", Context.MODE_PRIVATE)
    }

    private val pickVideo =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    saveLastVideo(it)
                    playVideo(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)
        chooseButton = findViewById(R.id.chooseButton)

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        chooseButton.isFocusable = true
        chooseButton.isFocusableInTouchMode = true
        chooseButton.requestFocus()

        chooseButton.setOnClickListener {
            openFileChooser()
        }

        loadLastVideo()?.let {
            playVideo(it)
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        }
        pickVideo.launch(intent)
    }

    private fun playVideo(uri: Uri) {
        currentUri = uri

        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()

        val savedPosition = prefs.getLong(uri.toString(), 0L)
        if (savedPosition > 0) {
            player.seekTo(savedPosition)
        }

        player.play()

        chooseButton.visibility = View.GONE
    }

    private fun saveLastVideo(uri: Uri) {
        prefs.edit().putString("last_video_uri", uri.toString()).apply()
    }

    private fun loadLastVideo(): Uri? {
        val uriString = prefs.getString("last_video_uri", null)
        return uriString?.let { Uri.parse(it) }
    }

    // 🔥 TV behaviour: Pause + Show Overlay
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN &&
            event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER
        ) {

            if (player.isPlaying) {
                // Pause playback
                player.pause()

                // Show overlay button
                chooseButton.visibility = View.VISIBLE
                chooseButton.requestFocus()

                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }

    override fun onStop() {
        super.onStop()
        savePlaybackPosition()
        player.pause()
    }

    override fun onDestroy() {
        savePlaybackPosition()
        player.release()
        super.onDestroy()
    }

    private fun savePlaybackPosition() {
        currentUri?.let {
            val position = player.currentPosition
            prefs.edit().putLong(it.toString(), position).apply()
        }
    }
}