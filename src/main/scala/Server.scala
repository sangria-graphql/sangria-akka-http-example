import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer

import org.json4s.native.JsonMethods._
import org.json4s._

import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import sangria.parser.{SyntaxError, QueryParser}
import sangria.execution.Executor
import sangria.integration.json4s._

import scala.util.{Success, Failure}

object Server extends App {
  implicit val system = ActorSystem("sangria-server")
  implicit val materializer = ActorMaterializer()
  implicit val serialization = native.Serialization
  implicit val formats = DefaultFormats

  import system.dispatcher

  val executor = Executor(
    schema = SchemaDefinition.StarWarsSchema,
    userContext = new CharacterRepo,
    deferredResolver = new FriendsResolver)

  import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

val route: Route =
  (post & path("graphql")) {
    entity(as[JValue]) { requestJson =>
      val JString(query) = requestJson \ "query"
      val operation = requestJson \ "operation" match {
        case JString(op) => Some(op)
        case JNothing => None
      }
      val vars = requestJson \ "variables" match {
        case JString(s) => parse(s, true)
        case JNothing => JObject()
      }

      QueryParser.parse(query) match {

        // query parsed successfully, time to execute it!
        case Success(queryAst) =>
          complete(executor.execute(queryAst,
            operationName = operation,
            variables = vars))

        // can't parse GraphQL query, return error
        case Failure(error) =>
          complete(BadRequest, JObject("error" -> JString(error.getMessage)))
      }
    }
  }

  Http().bindAndHandle(route, "0.0.0.0", sys.props.get("http.port").fold(8080)(_.toInt))
}
