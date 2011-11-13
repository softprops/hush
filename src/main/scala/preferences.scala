package me.lessis

import android.content.Context

trait Preferences { self: Context =>
  import android.content.SharedPreferences

  def pref[T](name: String)(f: SharedPreferences.Editor => T) = {
    val settings = getSharedPreferences(name, Context.MODE_PRIVATE)
    val editor = settings.edit()
    f(editor)
    editor.commit()
  }
}
