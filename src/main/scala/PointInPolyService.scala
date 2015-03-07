package geosearch

import java.nio.file.{ FileSystems, Files }
import java.util.Calendar
import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.http.Http
import akka.http.marshallers.sprayjson.SprayJsonSupport._
import akka.http.marshalling.ToResponseMarshallable
import akka.http.server.Directives._
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.{ Config, ConfigFactory }
import feature._
import geometry._
import io.geojson.GeoJsonReader
import spray.json.DefaultJsonProtocol
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContextExecutor
import scala.util.Properties

case class Status(status: String, time: String)

trait JsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat2(Status.apply)
}

trait Service extends JsonProtocol {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: ActorFlowMaterializer

  def config: Config
  val logger: LoggingAdapter

  lazy val shpDir = Properties.envOrElse("SHP_DIR", "src/main/resources")
  lazy val shpPath = FileSystems.getDefault().getPath(shpDir)
  lazy val geoJsonFile = Files.newDirectoryStream(shpPath, "*.geojson").iterator.toList.head.toAbsolutePath.toString
  lazy val geoJson = GeoJsonReader(geoJsonFile)
  val fc = geoJson.fc

  val routes = {
    path("status") {
      get {
        compressResponseIfRequested() {
          complete {
            val now = Calendar.getInstance().getTime()
            ToResponseMarshallable(Status("OK", now.toString))
          }
        }
      }
    } ~
      path("features") {
        parameters('latitude.as[Double], 'longitude.as[Double]) { (lat, lon) =>
          val p = Point(lon, lat)
          val t = fc.pointInPoly(p).getOrElse(Nil).toList
          val result = FeatureCollection(t).features.map { f =>
            f.get("GEOID10").getOrElse("")
          }.head.toString

          compressResponseIfRequested() {
            complete {
              ToResponseMarshallable(result)
            }
          }
        }
      }
  }

}

object PointInPolyService extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorFlowMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bind(interface = config.getString("http.interface"), port = config.getInt("http.port")).startHandlingWith(routes)

}
