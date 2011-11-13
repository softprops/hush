package hush

object Server {
  import unfiltered.netty._
  def main(args: Array[String]) {
    Http(8080).handler(cycle.Planify { Api.locations }).run
  }
}

