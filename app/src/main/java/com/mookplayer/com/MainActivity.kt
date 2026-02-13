package com.mookplayer.com

import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

    private lateinit var btnPlayPause: Button
    private lateinit var btnRewind: Button
    private lateinit var btnForward: Button
    private lateinit var videoTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // View bindings
        playerView = findViewById(R.id.playerView)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnRewind = findViewById(R.id.btnRewind)
        btnForward = findViewById(R.id.btnForward)
        videoTitle = findViewById(R.id.videoTitle)

        // Initialize player
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // Example media (replace with your file picker later)
        val mediaItem = MediaItem.fromUri(
            Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
        )

        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true

        videoTitle.text = "Sample Media"

        setupControls()
    }

    private fun setupControls() {

        btnPlayPause.setOnClickListener {
            player.playWhenReady = !player.playWhenReady
            updatePlayPauseText()
        }

        btnRewind.setOnClickListener {
            player.seekBack()
        }

        btnForward.setOnClickListener {
            player.seekForward()
        }

        updatePlayPauseText()
    }

    private fun updatePlayPauseText() {
        btnPlayPause.text = if (player.isPlaying) "⏸" else "▶"
    }

    // Handle TV remote play/pause button
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER -> {
                player.playWhenReady = !player.playWhenReady
                updatePlayPauseText()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }
}