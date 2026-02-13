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

    private lateinit var btnRewind: ImageButton
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnForward: ImageButton

    private var currentUri: Uri? = null

    private val prefs by lazy {
        getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)

        // Buttons from your layout
        btnRewind = findViewById(R.id.btnRewind)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnForward = findViewById(R.id.btnForward)

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        btnPlayPause.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
            } else {
                player.play()
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
            }
        }

        btnRewind.setOnClickListener {
            player.seekBack()
        }

        btnForward.setOnClickListener {
            player.seekForward()
        }

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

        val lastPosition = prefs.getLong(uri.toString(), 0L)
        if (lastPosition > 0) player.seekTo(lastPosition)

        player.prepare()
        player.play()
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
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