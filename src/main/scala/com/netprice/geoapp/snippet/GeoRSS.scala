package com.netprice.geoapp.snippet

import net.liftweb.util.Helpers._
import xml.{Text, NodeSeq}
import net.liftweb.http.S

/**
 * Created by IntelliJ IDEA.
 * User: gyo
 * Date: Nov 15, 2010
 * Time: 10:43:00 AM
 * To change this template use File | Settings | File Templates.
 */

class GeoRSS {
  def geo(xhtml: NodeSeq): NodeSeq = {
    val limit = S.param("limit") openOr "100"
    bind("geo", xhtml, "limit" -> Text(limit))
  }
}