package me.lessis

trait Async { self: Logged =>
  lazy val handler = new android.os.Handler
  def async(block: => Unit) { 
    handler.post(new Runnable{
      def run {
        try   { block }
        catch { case e =>
          log.error("error in async block: %s" format e.getMessage, Some(e))
          throw e
        }
      }
    })
  }
}
