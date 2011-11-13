package me.lessis

import android.content.Context

trait Toasted { self: Context =>
  import android.widget.Toast
  import android.view.Gravity

  private def showToast(txt: CharSequence, len: Int) = {
    val t = Toast.makeText(self, txt, len)
    t.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
    t.show
  }

  def toast(txt: CharSequence) =
    showToast(txt, Toast.LENGTH_SHORT)

  def longToast(txt: CharSequence) =
    showToast(txt, Toast.LENGTH_LONG)
}
