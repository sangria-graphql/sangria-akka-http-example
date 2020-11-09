package sangria.http.circe

import sangria.http.akka.SangriaAkkaHttp.{GraphQLError, GraphQLErrorResponse, Location}
import io.circe._
import io.circe.generic.semiauto._
import sangria.http.akka.{GraphQLHttpRequest, Variables}


object CirceHttpSupport {
  implicit val locationEncoder: Encoder[Location] = deriveEncoder[Location]
  implicit val graphQLErrorEncoder: Encoder[GraphQLError] = deriveEncoder[GraphQLError]
  implicit val graphQLErrorResponseEncoder: Encoder[GraphQLErrorResponse] = deriveEncoder[GraphQLErrorResponse]

  implicit val graphQLRequestDecoder: Decoder[GraphQLHttpRequest[Json]] = deriveDecoder[GraphQLHttpRequest[Json]]

  implicit object JsonVariables extends Variables[Json] {
    override def empty: Json = Json.obj()
  }

}
