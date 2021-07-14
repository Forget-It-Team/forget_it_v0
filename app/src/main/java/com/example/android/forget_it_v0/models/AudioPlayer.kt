package com.example.android.forget_it_v0.models

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

object AudioPlayer {
    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying : Boolean = false


    public fun playAudio(c : Context, id : Uri){
        mediaPlayer = MediaPlayer.create(c, id)
        mediaPlayer.start()
        isPlaying = true
    }

    public fun stopAudio(){
        mediaPlayer.stop()
//        isPlaying = false
    }

    public fun isPlaying() : Boolean{
        return isPlaying
    }
}