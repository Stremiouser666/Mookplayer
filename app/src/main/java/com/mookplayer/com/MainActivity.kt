package com.mookplayer.com

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

    private lateinit var filenameLabel: TextView
    private lateinit var pausedLabel: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var timePlayed: TextView
    private lateinit var timeRemaining: TextView

    private var currentUri: Uri? = null

    private val prefs by lazy {
        getSharedPreferences("playback_positions", Context.MODE_PRIVATE)
    }

    private val filePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let { playMedia(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)
        filenameLabel = findViewById(R.id.filenameLabel)
        pausedLabel = findViewById(R.id.pausedLabel)
        progressBar = findViewById(R.id.progressBar)
        timePlayed = findViewById(R.id.timePlayed)
        timeRemaining = findViewById(R.id.timeRemaining)

        val btnResume: Button = findViewById(R.id.btnResume)
        val btnStop: Button = findViewById(R.id.btnStop)
        val btnRestart: Button = findViewById(R.id.btnRestart)
        val selectFileButton: Button = findViewById(R.id.selectFileButton)

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        btnResume.setOnClickListener { player.play() }
        btnStop.setOnClickListener { player.pause() }
        btnRestart.setOnClickListener { player.seekTo(0) }
        selectFileButton.setOnClickListener { openFilePicker() }

        handleIntent(intent)
    }

    private fun openFilePicker() {
        filePicker.launch(arrayOf("*/*"))
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        playMedia(uri)
    }

    private fun playMedia(uri: Uri) {
        currentUri = uri
        filenameLabel.text = uri.lastPathSegment ?: "Playing"

        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)

        val lastPosition = prefs.getLong(uri.toString(), 0L)
        if (lastPosition > 0) {
            player.seekTo(lastPosition)
        }

        player.prepare()
        player.play()
    }

    override fun onPause() {
        super.onPause()
        savePosition()
    }

    override fun onStop() {
        super.onStop()
        savePosition()
    }

    private fun savePosition() {
        currentUri?.let {
            prefs.edit().putLong(it.toString(), player.currentPosition).apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}