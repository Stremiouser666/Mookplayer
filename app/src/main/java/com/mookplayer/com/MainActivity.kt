package com.mookplayer.com

import android.app.Activity
import android.content.Context
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
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

    private lateinit var overlayLayout: LinearLayout
    private lateinit var dimView: View
    private lateinit var fileNameText: TextView
    private lateinit var timePlayed: TextView
    private lateinit var timeRemaining: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var resumeButton: Button
    private lateinit var restartButton: Button
    private lateinit var stopButton: Button

    private var currentUri: Uri? = null
    private val handler = Handler(Looper.getMainLooper())

    private val prefs by lazy {
        getSharedPreferences("mookplayer_prefs", Context.MODE_PRIVATE)
    }

    private val pickVideo =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let {
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
        overlayLayout = findViewById(R.id.overlayLayout)
        dimView = findViewById(R.id.dimView)
        fileNameText = findViewById(R.id.fileNameText)
        timePlayed = findViewById(R.id.timePlayed)
        timeRemaining = findViewById(R.id.timeRemaining)
        progressBar = findViewById(R.id.progressBar)
        resumeButton = findViewById(R.id.resumeButton)
        restartButton = findViewById(R.id.restartButton)
        stopButton = findViewById(R.id.stopButton)

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        resumeButton.setOnClickListener {
            hideOverlay()
            player.play()
        }

        restartButton.setOnClickListener {
            player.seekTo(0)
            hideOverlay()
            player.play()
        }

        stopButton.setOnClickListener {
            player.stop()
            hideOverlay()
        }

        loadLastVideo()?.let { playVideo(it) }
    }

    private fun playVideo(uri: Uri) {
        currentUri = uri
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()

        val savedPosition = prefs.getLong(uri.toString(), 0L)
        if (savedPosition > 0) player.seekTo(savedPosition)

        player.play()
    }

    private fun showOverlay() {
        overlayLayout.visibility = View.VISIBLE
        dimView.visibility = View.VISIBLE
        updateOverlay()
        startProgressUpdates()
        resumeButton.requestFocus()
    }

    private fun hideOverlay() {
        overlayLayout.visibility = View.GONE
        dimView.visibility = View.GONE
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateOverlay() {
        currentUri?.let {
            fileNameText.text = File(it.path ?: "").name
        }

        val duration = player.duration
        val position = player.currentPosition

        if (duration > 0) {
            val progress = (position * 1000 / duration).toInt()
            progressBar.progress = progress

            timePlayed.text = formatTime(position)
            timeRemaining.text = "-${formatTime(duration - position)}"
        }
    }

    private fun startProgressUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                updateOverlay()
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun formatTime(ms: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        return String.format("%02d:%02d", minutes, seconds)
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
        return prefs.getString("last_video_uri", null)?.let { Uri.parse(it) }
    }

    override fun onStop() {
        super.onStop()
        currentUri?.let {
            prefs.edit().putLong(it.toString(), player.currentPosition).apply()
        }
        player.pause()
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}