package com.mookplayer.com

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

    private lateinit var openFileButton: ImageButton
    private lateinit var playPauseButton: ImageButton

    private var currentUri: Uri? = null

    private val prefs by lazy {
        getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Player view
        playerView = findViewById(R.id.playerView)

        // Buttons (ImageButtons — fixes crash)
        openFileButton = findViewById(R.id.openFileButton)
        playPauseButton = findViewById(R.id.playPauseButton)

        // Setup player
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // Button actions
        openFileButton.setOnClickListener { openFilePicker() }

        playPauseButton.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }

        // Handle "Open With"
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        loadMedia(uri)
    }

    private fun loadMedia(uri: Uri) {
        currentUri = uri

        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)

        // Restore last position
        val lastPosition = prefs.getLong(uri.toString(), 0L)
        if (lastPosition > 0) {
            player.seekTo(lastPosition)
        }

        player.prepare()
        player.play()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                loadMedia(uri)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        savePlaybackPosition()
    }

    override fun onStop() {
        super.onStop()
        savePlaybackPosition()
    }

    private fun savePlaybackPosition() {
        currentUri?.let { uri ->
            prefs.edit()
                .putLong(uri.toString(), player.currentPosition)
                .apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}