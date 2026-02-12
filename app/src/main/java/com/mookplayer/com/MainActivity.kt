package com.mookplayer.com

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

    private val prefs by lazy {
        getSharedPreferences("mookplayer_prefs", Context.MODE_PRIVATE)
    }

    private val pickVideo =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    // Persist permission so we can reopen later
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

        // 🔥 TV auto-focus
        chooseButton.isFocusable = true
        chooseButton.isFocusableInTouchMode = true
        chooseButton.requestFocus()

        chooseButton.setOnClickListener {
            openFileChooser()
        }

        // 🔥 Auto-resume last video if exists
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
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        // 🔥 Auto-hide button once video starts
        chooseButton.visibility = View.GONE
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
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}