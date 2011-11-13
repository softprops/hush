package me.lessis

import android.content.Context

trait Played { self: Context =>
  import android.media.MediaPlayer

  def playAudio(res: Int) = {
    val player = MediaPlayer.create(getApplicationContext, res)
    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      override def onCompletion(mp: MediaPlayer) {
        mp.release()
      }
    })
    player.start
  }
}
