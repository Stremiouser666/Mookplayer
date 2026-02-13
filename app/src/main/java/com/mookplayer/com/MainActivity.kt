package com.mookplayer.com

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var player: SimpleExoPlayer
    private lateinit var playerView: PlayerView
    private var currentUri: Uri? = null

    private val prefs by lazy {
        getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        playerView = PlayerView(this)
        setContentView(playerView)

        player = SimpleExoPlayer.Builder(this).build()
        playerView.player = player

        handleIntent()
    }

    private fun handleIntent() {
        val uri = intent?.data ?: return
        currentUri = uri

        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)

        // Restore last position
        val lastPosition = prefs.getLong(uri.toString(), 0L)
        if (lastPosition > 0) {
            player.seekTo(lastPosition)
        }

        player.prepare()
        player.playWhenReady = true
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