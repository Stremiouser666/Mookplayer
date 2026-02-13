package com.mookplayer.com

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

    // File picker
    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { playVideo(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)
        val openFileButton: ImageButton = findViewById(R.id.openFileButton)
        val playPause: ImageButton = findViewById(R.id.btnPlayPause)
        val rewind: ImageButton = findViewById(R.id.btnRewind)
        val forward: ImageButton = findViewById(R.id.btnForward)

        // Setup player
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // 🔹 OPEN FILE BUTTON
        openFileButton.setOnClickListener {
            openFileLauncher.launch("video/*")
        }

        // 🔹 PLAY / PAUSE
        playPause.setOnClickListener {
            player.playWhenReady = !player.playWhenReady
            playPause.setImageResource(
                if (player.playWhenReady)
                    android.R.drawable.ic_media_pause
                else
                    android.R.drawable.ic_media_play
            )
        }

        // 🔹 REWIND 10s
        rewind.setOnClickListener {
            player.seekTo((player.currentPosition - 10_000).coerceAtLeast(0))
        }

        // 🔹 FORWARD 10s
        forward.setOnClickListener {
            player.seekTo(player.currentPosition + 10_000)
        }
    }

    private fun playVideo(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}