package me.lessis

trait Logger {
  def info(msg: => String): Unit
  def debug(msg: => String): Unit
  def warn(msg: => String): Unit
  def error(msg: => String, t: Option[Throwable]): Unit
}

trait Logged {
  import android.util.Log
  lazy val tag = getClass().getSimpleName()
  lazy val log = new Logger {
    def info(msg: => String) = Log.i(tag, msg)
    def debug(msg: => String) = Log.d(tag, msg)
    def warn(msg: => String) = Log.w(tag, msg)
    def error(msg: => String, t: Option[Throwable] = None) = t match {
      case Some(t) => Log.e(tag, msg, t)
      case _ => Log.e(tag, msg)
    }
  }
}
