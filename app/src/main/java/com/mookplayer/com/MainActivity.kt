<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.media3.ui.PlayerView
        android:id="@+id/playerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <Button
        android:id="@+id/selectFileButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Select File"
        android:layout_margin="16dp"
        android:layout_gravity="top|end" />

</FrameLayout>