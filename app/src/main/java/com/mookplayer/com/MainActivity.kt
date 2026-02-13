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
    private lateinit var btnRewind: ImageButton
    private lateinit var btnForward: ImageButton

    private var currentUri: Uri? = null
    private var openWithMode = false

    private val prefs by lazy {
        getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)
        openFileButton = findViewById(R.id.openFileButton)
        playPauseButton = findViewById(R.id.btnPlayPause)
        btnRewind = findViewById(R.id.btnRewind)
        btnForward = findViewById(R.id.btnForward)

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // ▶ Short press = play in Mookplayer
        openFileButton.setOnClickListener {
            openWithMode = false
            openFilePicker()
        }

        // ▶ Long press = Open With chooser
        openFileButton.setOnLongClickListener {
            openWithMode = true
            openFilePicker()
            true
        }

        playPauseButton.setOnClickListener {
            if (player.isPlaying) player.pause() else player.play()
        }

        btnRewind.setOnClickListener {
            player.seekTo((player.currentPosition - 10000).coerceAtLeast(0))
        }

        btnForward.setOnClickListener {
            player.seekTo(player.currentPosition + 10000)
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
            val uri = data?.data ?: return

            if (openWithMode) {
                openWithChooser(uri)
            } else {
                loadMedia(uri)
            }
        }
    }

    private fun openWithChooser(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Open with"))
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
        currentUri?.let {
            prefs.edit().putLong(it.toString(), player.currentPosition).apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}