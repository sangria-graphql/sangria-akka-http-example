package sangria.http.akka

import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, FromStringUnmarshaller}
import Util.explicitlyAccepts
import sangria.ast.Document
import sangria.parser.{QueryParser, SyntaxError}
import GraphQLRequestUnmarshaller._

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object SangriaAkkaHttp {
  final case class MalformedRequest(private val message: String = "",
                                   private val cause: Throwable = None.orNull)
    extends Exception(message, cause)

  case class Location(line: Int, column: Int)
  case class GraphQLError(massage: String, locations: Option[List[Location]] = None)
  case class GraphQLErrorResponse(errors: List[GraphQLError])

  def graphQLPlayground: Route = get {
    explicitlyAccepts(`text/html`) {
      getFromResource("assets/playground.html")
    }
  }

  def formatError(error: Throwable): GraphQLErrorResponse = error match {
    case syntaxError: SyntaxError =>
      GraphQLErrorResponse(
        GraphQLError(
          syntaxError.getMessage,
          Some(Location(syntaxError.originalError.position.line, syntaxError.originalError.position.column) :: Nil)
        ) :: Nil
      )
    case NonFatal(e) =>
      GraphQLErrorResponse(GraphQLError(e.getMessage) :: Nil)
    case e =>
      throw e
  }

  def prepareQuery(maybeQuery: Option[String]): Try[Document] = maybeQuery match {
    case Some(q) => QueryParser.parse(q)
    case None => Left(MalformedRequest(
      """Could not extract `query` from request.
        | Please confirm you have included a valid GraphQL
        |  query either as a QueryString parameter, or in the body of your request.""".stripMargin)).toTry
  }

  private def prepareGraphQLPost[T](inner: Try[GraphQLRequest[T]] => StandardRoute)
                                (implicit reqUm: FromRequestUnmarshaller[GraphQLHttpRequest[T]],
                                 varUm: FromStringUnmarshaller[T],
                                 v: Variables[T]): Route =
    parameters(Symbol("query").?, Symbol("operationName").?, Symbol("variables").as[T].?) { (queryParam, operationNameParam, variablesParam) =>
        // Content-Type: application/json
        entity(as[GraphQLHttpRequest[T]]) { body =>
          val maybeOperationName = operationNameParam orElse body.operationName
          val maybeQuery = queryParam orElse body.query

          // Variables may be provided in the QueryString, or possibly in the body as a String:
          // If we were unable to parse the variables from the body as a string,
          // we read them as JSON, and finally if no variables have been located
          // in the QueryString, Body (as a String) or Body (as JSON), we provide
          // an empty JSON object as the final result
          val maybeVariables = variablesParam orElse body.variables

          prepareQuery(maybeQuery) match {
            case Success(document) =>
              val result = GraphQLRequest(
                query = document,
                variables = maybeVariables,
                operationName = maybeOperationName
              )
              inner(Success(result))
            case Failure(error) => inner(Failure(error))
          }
        } ~
        // Content-Type: application/graphql
        entity(as[Document]) { document =>
          val result = GraphQLRequest(query = document, variables = variablesParam, operationName = operationNameParam)
          inner(Success(result))
        }

    }

  private def prepareGraphQLGet[T](inner: Try[GraphQLRequest[T]] => StandardRoute)
                                  (implicit varUm: FromStringUnmarshaller[T], v: Variables[T]): Route =
    parameters(Symbol("query").?, Symbol("operationName").?, Symbol("variables").as[T].?) { (maybeQuery, maybeOperationName, maybeVariables) =>
      prepareQuery(maybeQuery) match {
        case Success(document) =>
          val result = GraphQLRequest(
            query = document,
            variables = maybeVariables,
            maybeOperationName
          )
          inner(Success(result))
        case Failure(error) => inner(Failure(error))
      }
  }

  def prepareGraphQLRequest[T](inner: PartialFunction[Try[GraphQLRequest[T]], StandardRoute])
                           (implicit reqUm: FromRequestUnmarshaller[GraphQLHttpRequest[T]],
                            varUm: FromStringUnmarshaller[T], v: Variables[T]): Route =
    get {
      prepareGraphQLGet{ inner }
    } ~ post {
      prepareGraphQLPost{ inner }
    }
}
