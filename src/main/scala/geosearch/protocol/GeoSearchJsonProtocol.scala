package geosearch.protocol

import geosearch.model.{ GeoSearchResult, Status }
import spray.json.DefaultJsonProtocol

object GeoSearchJsonProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat4(Status.apply)
  implicit val resultFormat = jsonFormat1(GeoSearchResult.apply)
}
