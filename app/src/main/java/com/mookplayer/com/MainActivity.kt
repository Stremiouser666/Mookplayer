package com.mookplayer.com

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private lateinit var overlayLayout: View
    private lateinit var pausedLabel: TextView
    private lateinit var filenameLabel: TextView
    private lateinit var progressBar: SeekBar
    private lateinit var timePlayed: TextView
    private lateinit var timeRemaining: TextView
    private lateinit var btnStop: Button
    private lateinit var btnResume: Button
    private lateinit var btnRestart: Button
    private lateinit var selectFileButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private var updateProgressTask: Runnable? = null

    private var currentFileName: String = "No file selected"

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                currentFileName = getFileName(uri)
                filenameLabel.text = currentFileName
                playUri(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.playerView)
        overlayLayout = findViewById(R.id.overlayLayout)
        pausedLabel = findViewById(R.id.pausedLabel)
        filenameLabel = findViewById(R.id.filenameLabel)
        progressBar = findViewById(R.id.progressBar)
        timePlayed = findViewById(R.id.timePlayed)
        timeRemaining = findViewById(R.id.timeRemaining)
        btnStop = findViewById(R.id.btnStop)
        btnResume = findViewById(R.id.btnResume)
        btnRestart = findViewById(R.id.btnRestart)
        selectFileButton = findViewById(R.id.selectFileButton)

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                pausedLabel.visibility = if (!isPlaying) View.VISIBLE else View.GONE
            }
        })

        btnStop.setOnClickListener { stopPlayer() }
        btnResume.setOnClickListener { resumePlayer() }
        btnRestart.setOnClickListener { restartPlayer() }
        selectFileButton.setOnClickListener { pickFile() }

        startProgressUpdater()
    }

    private fun pickFile() {
        pickFileLauncher.launch("*/*")
    }

    private fun playUri(uri: Uri) {
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
        player.play()
        overlayLayout.visibility = View.GONE
    }

    private fun stopPlayer() {
        player.pause()
        player.seekTo(0)
        overlayLayout.visibility = View.VISIBLE
    }

    private fun resumePlayer() {
        player.play()
        overlayLayout.visibility = View.GONE
    }

    private fun restartPlayer() {
        player.seekTo(0)
        player.play()
        overlayLayout.visibility = View.GONE
    }

    private fun startProgressUpdater() {
        updateProgressTask = object : Runnable {
            override fun run() {
                val duration = player.duration.coerceAtLeast(0)
                val position = player.currentPosition.coerceAtLeast(0)
                if (duration > 0) {
                    val progress = (position * 100 / duration).toInt()
                    progressBar.progress = progress
                    timePlayed.text = formatMillis(position)
                    timeRemaining.text = formatMillis(duration - position)
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateProgressTask!!)
    }

    private fun formatMillis(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (overlayLayout.visibility == View.VISIBLE) {
                overlayLayout.visibility = View.GONE
                player.play()
            } else {
                overlayLayout.visibility = View.VISIBLE
                player.pause()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressTask!!)
        player.release()
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex("_display_name")
                if (idx >= 0) result = cursor.getString(idx)
            }
        }
        return result ?: uri.lastPathSegment ?: "Unknown"
    }
}