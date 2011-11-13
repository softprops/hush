package hush

object Csv {
  import au.com.bytecode.opencsv._
  import scala.collection.mutable.ListBuffer
  import java.io.{BufferedReader, InputStreamReader}

  trait Converter[A] {
    def convert(a: Array[String]): A
  }

  def read[A](path: String)(implicit c: Converter[A]) = {
    @annotation.tailrec
    def consume(csv: CSVReader, buffer: ListBuffer[A]): Seq[A] =
      (csv, buffer, csv.readNext) match {
        case (_, buffer, null) =>
          buffer.toSeq
        case (csv, buffer, ary) =>
          buffer + c.convert(ary)
          consume(csv, buffer)
      }

    var reader = new CSVReader(
      new BufferedReader(new InputStreamReader(
        getClass().getResourceAsStream(path)))
    )
    reader.readNext // header line
    consume(
      reader,
      ListBuffer.empty[A]
    )
  }
}

object Source {
  def main(a: Array[String]) {
    var Coords = """\((-?\d+[.]\d+)\s*,\s*(-?\d+[.]\d+)\)""".r
    implicit val toTheater =
      new Csv.Converter[Place] {
        def convert(a: Array[String]) = a match {
          case Array(id, shape, name, tel, url, addr1, addr2, city, zip) =>
            val (lat, lon) = shape.trim match {
              case Coords(lat, lon) =>
                (lat.trim().toDouble, lon.trim().toDouble)
              case err => error(
                "expected lat, lon encoding but got %s" format err.toList
              )
            }
            Place(id, lat, lon, name, "theater")
          case _ => error("%s was not the the expected" format a.toString)
        }
      }
    val theaters = Csv.read("/Theaters.csv")
    println(theaters)
    //Store.save(theaters)
  }
}
