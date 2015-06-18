package geosearch.api

import java.net.InetAddress
import java.nio.file.{ FileSystems, Files }
import java.time.Instant
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.coding.{ Deflate, Gzip, NoCoding }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorFlowMaterializer
import com.typesafe.config.Config
import geometry.Point
import geosearch.model.{ GeoSearchResult, Status }
import geosearch.protocol.GeoSearchJsonProtocol._
import io.geojson.GeoJsonReader
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContextExecutor
import scala.util.Properties

trait Service {
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
        encodeResponseWith(NoCoding, Gzip, Deflate) {
          complete {
            val now = Instant.now.toString
            val host = InetAddress.getLocalHost.getHostName
            val status = Status("OK", "geosearch", now, host)
            ToResponseMarshallable(status)
          }
        }
      }
    } ~
      path("features") {
        parameters('latitude.as[Double], 'longitude.as[Double]) { (lat, lon) =>
          val p = Point(lon, lat)
          val t = fc.pointInPoly(p).getOrElse(Nil).toList
          val contained = t.nonEmpty
          val result = GeoSearchResult(contained)

          encodeResponseWith(NoCoding, Gzip, Deflate) {
            complete {
              ToResponseMarshallable(result)
            }
          }
        }
      }
  }

}
