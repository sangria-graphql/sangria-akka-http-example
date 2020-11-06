
import akka.http.scaladsl.server.{Route, StandardRoute}
import akka.http.scaladsl.server.Directives._
import io.circe.Json
import io.circe.optics.JsonPath.root
import io.circe.parser.parse
import sangria.ast.Document
import sangria.parser.{QueryParser, SyntaxError}
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport.jsonUnmarshaller
import GraphQLRequestUnmarshaller._
import akka.http.scaladsl.model.MediaTypes._

import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

case class GraphQLRequest(query: Document, variables: Json, operationName: Option[String])
object GraphQLRequest {
    val emptyVariables = Json.obj()
    def apply(query: Document, variables: Json, operationName: Option[String]): GraphQLRequest =
      new GraphQLRequest(
        query = query,
        variables = if (variables.isNull) emptyVariables else variables,
        operationName = operationName
      )
}

object SangriaAkkaHttp {
  final case class MalformedRequest(private val message: String = "",
                                   private val cause: Throwable = None.orNull)
    extends Exception(message, cause)

  def graphQLPlayground: Route = get {
    explicitlyAccepts(`text/html`) {
      getFromResource("assets/playground.html")
    }
  }

  def formatError(error: Throwable): Json = error match {
    case syntaxError: SyntaxError =>
      Json.obj("errors" -> Json.arr(
        Json.obj(
          "message" -> Json.fromString(syntaxError.getMessage),
          "locations" -> Json.arr(Json.obj(
            "line" -> Json.fromBigInt(syntaxError.originalError.position.line),
            "column" -> Json.fromBigInt(syntaxError.originalError.position.column))))))
    case NonFatal(e) =>
      formatError(e.getMessage)
    case e =>
      throw e
  }

  def formatError(message: String): Json =
    Json.obj("errors" -> Json.arr(Json.obj("message" -> Json.fromString(message))))


  // This is intended to resolve some duplication of code where `GET` and `POST` requests
  // mostly function the same way, with some differing source data.
  def prepareQueryAndVars(maybeQuery: Option[String], maybeVars: Option[String]): Try[(Document, Option[Json])] = {
    (maybeQuery.map(QueryParser.parse(_)) match {
      case Some(Success(query)) =>
        maybeVars.map(parse) match {
          case Some(Left(error)) => Left(error)
          case Some(Right(json)) => Right((query, Some(json)))
          case None => Right((query, None))
        }
      case Some(Failure(error)) => Left(error)
      case None => Left(MalformedRequest(
        """Could not extract `query` from request.
          | Please confirm you have included a valid GraphQL
          |  query either as a QueryString parameter, or in the body of your request.""".stripMargin))
    }).toTry
  }

  private def prepareGraphQLPost(inner: Try[GraphQLRequest] => StandardRoute): Route =
    parameters(Symbol("query").?, Symbol("operationName").?, Symbol("variables").?) { (queryParam, operationNameParam, variablesParam) =>
        // Content-Type: application/json
        entity(as[Json]) { body =>
          val maybeOperationName = operationNameParam orElse root.operationName.string.getOption(body)
          val maybeQuery = queryParam orElse root.query.string.getOption(body)
          // Variables may be provided in the QueryString, or possibly in the body as a String.
          val maybeVariables = variablesParam orElse root.variables.string.getOption(body)

          prepareQueryAndVars(maybeQuery, maybeVariables) match {
            case Success((document, variablesOpt)) =>
              val varsFromBody = root.variables.json.getOption(body)
              // If we were unable to parse the variables from the body as a string,
              // we read them as JSON, and finally if no variables have been located
              // in the QueryString, Body (as a String) or Body (as JSON), we provide
              // an empty JSON object as the final result
              val finalVars = variablesOpt.orElse(varsFromBody).getOrElse(GraphQLRequest.emptyVariables)
              val result = GraphQLRequest(
                query = document,
                variables = finalVars,
                operationName = maybeOperationName
              )
              inner(Success(result))
            case Failure(error) => inner(Failure(error))
          }
        } ~
        // Content-Type: application/graphql
        entity(as[Document]) { document =>
          variablesParam.map(parse) match {
            case Some(Left(error)) => inner(Failure(error))
            case Some(Right(json)) =>
              val result = GraphQLRequest(query = document, variables = json, operationName = operationNameParam)
              inner(Success(result))
            case None =>
              val result = GraphQLRequest(query = document, variables = GraphQLRequest.emptyVariables, operationName = operationNameParam)
              inner(Success(result))
          }
        }

    }

  private def prepareGraphQLGet(inner: Try[GraphQLRequest] => StandardRoute): Route =
    parameters(Symbol("query").?, Symbol("operationName").?, Symbol("variables").?) { (maybeQuery, maybeOperationName, maybeVariables) =>
      prepareQueryAndVars(maybeQuery, maybeVariables) match {
        case Success((document, variablesOpt)) =>
          val finalVars = variablesOpt.getOrElse(GraphQLRequest.emptyVariables)
          val result = GraphQLRequest(
            query = document,
            variables = finalVars,
            maybeOperationName
          )
          inner(Success(result))
        case Failure(error) => inner(Failure(error))
      }
  }

  def prepareGraphQLRequest(inner: PartialFunction[Try[GraphQLRequest], StandardRoute]): Route = 
    get {
      prepareGraphQLGet{ inner }
    } ~ post {
      prepareGraphQLPost{ inner }
    }
}
