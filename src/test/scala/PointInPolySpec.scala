package geosearch

import akka.event.{ Logging, LoggingAdapter }
import akka.http.testkit.ScalatestRouteTest
import com.typesafe.config.Config
import org.scalatest.{ MustMatchers, FlatSpec }
import akka.http.model.StatusCodes._
import akka.http.model.MediaTypes._

class PointInPolySpec extends FlatSpec with MustMatchers with ScalatestRouteTest with Service {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config: Config = testConfig

  override val logger: LoggingAdapter = Logging(system, getClass)

  "A point in polygon geosearch" must "return features that overlap" in {
    Get("/features?latitude=39.5&longitude=-77.2") ~> routes ~> check {
      status mustBe OK
    }
  }

}
