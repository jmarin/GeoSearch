package geosearch.api

import java.time.{ Duration, Instant }
import akka.event.{ Logging, LoggingAdapter }
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.Config
import org.scalatest.FlatSpec
import org.scalatest.MustMatchers
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

class PointInPolySpec extends FlatSpec with MustMatchers with ScalatestRouteTest with Service {
  override def testConfigSource = "akka.loglevel = WARNING"
  override def config: Config = testConfig

  override val logger: LoggingAdapter = Logging(system, getClass)

  "GeoSearch" should "respond to status" in {
    Get("/status") ~> routes ~> check {
      status mustBe OK
      contentType.mediaType mustBe `application/json`
      val resp = responseAs[geosearch.model.Status]

      resp.status mustBe "OK"
      resp.service mustBe "geosearch"

      val statusTime = Instant.parse(resp.time)
      val timeDiff = Duration.between(statusTime, Instant.now).getSeconds
      timeDiff must be <= 1l
    }
  }

  "A point in polygon geosearch" must "return features that overlap" in {
    Get("/features?latitude=39.5&longitude=-77.2") ~> routes ~> check {
      status mustBe OK
    }
  }

}
