package hush

abstract class Props(resource: String) {
  import scala.util.control.Exception.allCatch

  trait Provider {
    def get(k: String): String
  }

  object Env extends Provider {
    def get(k: String) = System.getenv(k)
  }

  abstract class FallbackProvider(val fallback: Provider) extends Provider
  
  case class JProps(resource: String) extends FallbackProvider(Env) {
    lazy val props = {
      val p = new java.util.Properties()
      getClass().getResourceAsStream(resource) match {
        case null => println("local resource %s not found. (it's okay fallback on env)" format resource)
        case r => p.load(r)
      }
      p
    }

    def get(k: String) = props.getProperty(k) match {
      case null => fallback.get(k)
      case value => value
    }
  }

  protected lazy val underlying = JProps(resource)

  def get(name: String) = underlying.get(name) match {
    case null => sys.error("undefined property %s" format name)
    case value => value
  }

  def getInt(name: String) = allCatch.opt { get(name) toInt } match {
    case None => sys.error("undefined int property %s" format name)
    case Some(n) => n
  }

  def apply(name: String) = underlying.get(name) match {
     case null => None
     case value => Some(value)
  }

  def int(name: String) = apply(name).map(v => v.toInt)
}

object Props extends Props("/hush.properties")
