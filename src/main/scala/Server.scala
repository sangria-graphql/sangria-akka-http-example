import scala.util.{Failure, Success, Try}
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.slowlog.SlowLog
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.MediaTypes.*
import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.*
import org.webjars.WebJarAssetLocator
import sangria.http.akka.Util.explicitlyAccepts
import sangria.marshalling.circe.*

// This is the trait that makes `prepareGraphQLRequest` available
import sangria.http.akka.circe.CirceHttpSupport

object Server extends App with CorsSupport with CirceHttpSupport {
  implicit val system: ActorSystem = ActorSystem("sangria-server")
  import system.dispatcher

  /** Tool for locating WebJar resources in the classpath. */
  private[this] val webJarAssetLocator = new WebJarAssetLocator()

  /** Route for WebJar assets.
    *
    * Tries to resolve the unmatched path with WebJar resources that are on the classpath.
    * Completes if so, rejects if it fails to find a unique asset with the path name.
    */
  private[this] val webJars: Route = extractUnmatchedPath { path =>
    Try(webJarAssetLocator.getFullPath(path.toString)) match {
      case Success(fullPath) => getFromResource(fullPath)
      case Failure(_: IllegalArgumentException) => reject
      case Failure(e) => failWith(e)
    }
  }

  /** Route for our GraphiQL page. */
  private[this] val graphiql: Route =  get {
    explicitlyAccepts(`text/html`) {
      // This asset needs to be provided in the classpath; it's not in any WebJar.
      getFromResource("assets/graphiql.html")
    }
  }

  val route: Route =
    optionalHeaderValueByName("X-Apollo-Tracing") { tracing =>
      path("graphql") {
        graphiql ~
        prepareGraphQLRequest {
          case Success(req) =>
            val middleware = if (tracing.isDefined) SlowLog.apolloTracing :: Nil else Nil
            val deferredResolver = DeferredResolver.fetchers(SchemaDefinition.characters)
            val graphQLResponse = Executor.execute(
                schema = SchemaDefinition.StarWarsSchema,
                queryAst = req.query,
                userContext = new CharacterRepo,
                variables = req.variables,
                operationName = req.operationName,
                middleware = middleware,
                deferredResolver = deferredResolver
              ).map(OK -> _)
               .recover {
                 case error: QueryAnalysisError => BadRequest -> error.resolveError
                 case error: ErrorWithResolver => InternalServerError -> error.resolveError
               }
            complete(graphQLResponse)
          case Failure(preparationError) => complete(BadRequest, formatError(preparationError))
        }
      }
    } ~
    (get & pathEndOrSingleSlash) {
      redirect("/graphql", PermanentRedirect)
    } ~
    webJars

  val PORT = sys.props.get("http.port").fold(8080)(_.toInt)
  val INTERFACE = "0.0.0.0"
  Http().newServerAt(INTERFACE, PORT).bindFlow(corsHandler(route))
}
