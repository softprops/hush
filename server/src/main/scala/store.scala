package hush

import com.mongodb.casbah._
import com.mongodb.MongoURI
import java.net.URI

object Store {
  import com.mongodb.casbah.commons.Imports.ObjectId
  import com.mongodb.casbah.commons.{MongoDBObject => Obj, MongoDBList => ObjList}
  import com.mongodb.casbah.{MongoCollection}
  import com.mongodb.{BasicDBList, DBObject}
  import com.mongodb.casbah.query.{GeoCoords}
  import com.mongodb.casbah.Implicits._
  import com.mongodb.casbah.query.Imports._
  import java.util.Date

  lazy val db = {
    val uri = new URI(Props.get("MONGOLAB_URI"))
    try {
      val conn = MongoConnection(uri.getHost, uri.getPort)
      val name = uri.getPath.drop(1)
      val mongo = conn(name)
      val Array(user, pass) = uri.getUserInfo.split(":")
      mongo.authenticate(user, pass)
      mongo
    } catch {
      case e:java.io.IOException => println(
        "Error occured whilst connecting to mongo (%s): %s" format(
          uri, e.getMessage), Some(e)
      )
      throw e
    }
  }

  def collection[T](name: String)(f: MongoCollection => T): T = f(db(name))

  private def toDbObjects(places: Seq[Place]) =
    for(p <- places) yield {
      Obj(
        "id" -> p.id,
        "loc" -> ObjList(p.lat, p.lon),
        "name" -> p.name,
        "kind" -> p.kind
      )
    }

  private def toPlace(m:Obj) =
    try {
      val (lat, lon) = m.getAs[BasicDBList]("loc").get match {
        case bdbl: BasicDBList =>
          val l: ObjList = bdbl
          (l(0).asInstanceOf[Double], l(1).asInstanceOf[Double])
      }
      Place(
        m.getAs[String]("id").get,
        lat, lon,
        m.getAs[String]("name").get,
        m.getAs[String]("kind").get
      )
    } catch {
      case e =>
        println("failed to parse %s" format m)
        throw e
    }

  implicit val toPlaces: Iterator[DBObject] => Iterable[Place] =
    (m) => (for(p <- m) yield toPlace(p)).toSeq

  def save(places: Seq[Place]) = {
    collection("quiet_places") { c =>
      for(dbo <- toDbObjects(places)) c.insert(dbo)
    }
  }

  def quietPlaces[T](ll: (Double, Double))(f: Iterable[Place] => T) =
    collection("quiet_places") { c =>
      val (lat, lon) = ll
      val query =  "loc".$within $center ((lat, lon), 0.01)
      println(query)
      f(toPlaces(c.find( query )))
    }
}
