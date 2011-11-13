package me.lessis

import android.content.Context
import android.widget.Toast

trait Decider {
  def quiet(ll: (Double, Double), ctx: Context): (Boolean, Option[String])
}

object DefaultDecider extends Decider with Logged {
  import dispatch._
  import org.json.{JSONTokener, JSONObject, JSONArray}
  // in dispatch 0.8.* call shutdown
  private def http[T](handler: Handler[T]): Option[T] = try {
    val client = new Http
    Some(client(handler))
  } catch {
    case cr: org.apache.http.conn.HttpHostConnectException =>
      log.error("could not connect to host %s" format cr.getMessage, Some(cr))
      None
    case se: java.net.SocketException =>
      log.error("socket exception thrown" format se.getMessage, Some(se))
      None
  }
  
  val host = :/("10.0.2.2",8080)

  def quiet(ll: (Double, Double), ctx: Context) = {
    val (lat, lon) = ll
    val resp = http(host / "l" <<? Map(
      "ll" -> "%s,%s".format(lat, lon)
    ) as_str)
    resp match {
      case Some(json) =>
        log.debug(json)
        val (dec, msg) = new JSONTokener(json).nextValue() match {
          case ary: JSONArray => ary.length match {
            case 0 => (false, "Make as much noise as you like")
            case n =>
              (true, "You have entered a quiet zone\n %s" format(
                ary.get(0).asInstanceOf[JSONObject].getString("name")
              ))
          }
          case obj: JSONObject =>
            (true, "You have entered a quiet zone\n%s" format(
              obj.getString("name")
            ))
        }
        (dec, Some(msg))
      case _ =>
        (false, None)
    }
  }
}
