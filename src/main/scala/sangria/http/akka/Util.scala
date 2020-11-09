package sangria.http.akka

import akka.http.scaladsl.model.MediaType
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.{headerValuePF, pass}

object Util {
  def explicitlyAccepts(mediaType: MediaType): Directive0 =
    headerValuePF {
      case Accept(ranges) if ranges.exists(range => !range.isWildcard && range.matches(mediaType)) => ranges
    }.flatMap(_ => pass)
}
