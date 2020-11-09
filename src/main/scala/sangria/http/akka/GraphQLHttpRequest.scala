package sangria.http.akka

case class GraphQLHttpRequest[T](query: Option[String], variables: Option[T], operationName: Option[String])

