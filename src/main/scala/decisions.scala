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
  private def http[T](handler: Handler[T]): T = try {
    val client = new Http
    client(handler)
  } catch {
    case cr: org.apache.http.conn.HttpHostConnectException =>
      log.error("could not connect to host %s" format cr.getMessage, Some(cr))
      throw cr
  }
  
  val host = :/("10.0.2.2",8080)

  def quiet(ll: (Double, Double), ctx: Context) = {
    val (lat, lon) = ll
    val json = http(host / "l" <<? Map(
      "ll" -> "%s,%s".format(lat, lon)
    ) as_str)
    log.debug(json)
    val msg = new JSONTokener(json).nextValue() match {
      case ary: JSONArray => ary.length match {
        case 0 => "Make as much noise as you like"
        case n => "You have entered a quiet zone %s" format(
          ary.get(0).asInstanceOf[JSONObject].getString("name")
        )
      }
      case obj: JSONObject => "You have entered a quiet zone %s" format(
        obj.getString("name")
      )
    }
    Toast.makeText(
      ctx,
      msg,
      Toast.LENGTH_LONG
    ).show
    (true, Some(msg))
  }
}
