package com.mookplayer.com

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var chooseButton: Button
    private lateinit var overlayContainer: LinearLayout
    private lateinit var pausedText: TextView
    private lateinit var fileNameText: TextView
    private lateinit var dimOverlay: View

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
        overlayContainer = findViewById(R.id.overlayContainer)
        pausedText = findViewById(R.id.pausedText)
        fileNameText = findViewById(R.id.fileNameText)
        dimOverlay = findViewById(R.id.dimOverlay)

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        chooseButton.setOnClickListener {
            openFileChooser()
        }

        loadLastVideo()?.let {
            playVideo(it)
        } ?: showOverlay()
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

        fileNameText.text = getFileName(uri)

        hideOverlay()
    }

    private fun showOverlay() {
        overlayContainer.visibility = View.VISIBLE
        dimOverlay.visibility = View.VISIBLE
        chooseButton.requestFocus()
    }

    private fun hideOverlay() {
        overlayContainer.visibility = View.GONE
        dimOverlay.visibility = View.GONE
    }

    private fun getFileName(uri: Uri): String {
        return uri.lastPathSegment ?: "Selected Video"
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN &&
            event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER
        ) {

            if (player.isPlaying) {
                player.pause()
                showOverlay()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun saveLastVideo(uri: Uri) {
        prefs.edit().putString("last_video_uri", uri.toString()).apply()
    }

    private fun loadLastVideo(): Uri? {
        val uriString = prefs.getString("last_video_uri", null)
        return uriString?.let { Uri.parse(it) }
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