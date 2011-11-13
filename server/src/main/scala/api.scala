package hush

import unfiltered._
import unfiltered.request._
import unfiltered.response._
import com.codahale.jerkson.Json._

object Api {
  def locations: Cycle.Intent[Any, Any] = {
    case GET(Path(Seg("l" :: Nil))) & Params(p) =>
      p("ll") match {
        case Seq(ll) => ll.split(",") match {
          case Array(lat, lon) =>
            Store.quietPlaces((lat.toDouble, lon.toDouble)) { places =>
              JsonContent ~> ResponseString(generate(places))
            }
          case _ => BadRequest        
        }
        case _ => BadRequest
      }
  }
}
