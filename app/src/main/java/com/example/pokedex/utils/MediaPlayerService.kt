package com.example.pokedex.utils

import android.content.Context
import android.net.Uri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

@ActivityScoped
class MediaPlayerService @Inject constructor(
    @ActivityContext private val context: Context
): DefaultLifecycleObserver {
    private lateinit var player: ExoPlayer

    override fun onStart(owner: LifecycleOwner) {
        player = ExoPlayer.Builder(context).build()
    }

    override fun onStop(owner: LifecycleOwner) {
        player.release()
    }

    fun playerErrorFlow() = callbackFlow {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                trySend(error)
            }
        }
        player.addListener(listener)
        awaitClose { player.removeListener(listener) }
    }

    fun play(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }
}