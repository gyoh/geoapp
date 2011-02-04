package com.netprice.geoapp.api

import net.liftweb.http.rest._
import net.liftweb.common.Full
import com.netprice.geoapp.model.Venue
import net.liftweb.http.AtomResponse

object RestAPI extends RestHelper {
  // Import our methods for converting things around
  import RestFormatters._

  // Service Atom and requests that don't request a specific format
  serve {
    // Default to XML
    case XmlGet("api" :: "venue" :: "latitude" :: lat :: "longitude" :: lng :: "limit" :: limit :: "atom" :: _, _) =>
//      () => Full(AtomResponse(toAtom(lat.toDouble, lng.toDouble, limit.toInt)))
      () => Full(AtomResponse(fromMongoToAtom(lat.toDouble, lng.toDouble, limit.toInt)))
  }

  // Define an implicit conversion from a Venue to XML or JSON
  import net.liftweb.http.rest.{JsonSelect, XmlSelect}
  implicit def venueToRestResponse: JxCvtPF[Venue] = {
    case (JsonSelect, venue, _) => toJSON(venue)
    case (XmlSelect, venue, _) => toXML(venue)
  }

  serveJx {
    case Get(List("api", "venue", Venue(venue)), _) => Full(venue)
  }

}