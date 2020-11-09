package sangria.http.akka

import sangria.ast.Document

case class GraphQLRequest[T](query: Document, variables: T, operationName: Option[String])

object GraphQLRequest {
  def apply[T](query: Document, variables: Option[T], operationName: Option[String])
              (implicit v: Variables[T]): GraphQLRequest[T] =
    new GraphQLRequest(
      query = query,
      variables = variables.fold(v.empty)(identity),
      operationName = operationName
    )
}