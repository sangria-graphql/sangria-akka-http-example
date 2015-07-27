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
import sangria.integration.Json4sSupport._

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

  val route: Route =
    (get & path("graphql")) {
      parameters('query, 'args.?, 'operation.?) { (query, args, operation) =>

        QueryParser.parse(query) match {

          // query parsed successfully, time to execute it!
          case Success(queryAst) =>
            complete(executor.execute(queryAst,
              operationName = operation,
              arguments = args flatMap (parseOpt(_, true))))

          // can't parse GraphQL query, return error
          case Failure(error: SyntaxError) =>
            complete(BadRequest, JObject("error" -> JString(error.message)))
        }

      }
    }

  Http().bindAndHandle(route, "0.0.0.0", 8080)
}
