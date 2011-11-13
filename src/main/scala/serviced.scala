package me.lessis

import android.content.Context

trait Serviced { self: Context =>
  import android.location.LocationManager
  import android.media.AudioManager

  def locations[T](f: LocationManager => T) =
    self.getSystemService(Context.LOCATION_SERVICE) match {
      case lm: LocationManager => f(lm)
      case _ => error("could not resolve Location Service")
    }

  def audios[T](f: AudioManager => T) =
    self.getSystemService(Context.AUDIO_SERVICE) match {
      case am: AudioManager => f(am)
      case _ => error("could not resolve Audio Manager")
    }
}
