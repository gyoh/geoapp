package com.netprice.geoapp.model

import java.util.Date
import net.liftweb.mapper._

/**
 * Created by IntelliJ IDEA.
 * User: gyo
 * Date: Nov 10, 2010
 * Time: 7:12:31 PM
 * To change this template use File | Settings | File Templates.
 */

class Venue extends LongKeyedMapper[Venue] with IdPK {
  def getSingleton = Venue
  object name extends MappedString(this, 255)
  object address extends MappedString(this, 255)
  object zip extends MappedString(this, 255)
  object latitude extends MappedDouble(this)
  object longitude extends MappedDouble(this)
  object woeid extends MappedLong(this)
  object url extends MappedString(this, 255)

  object dateCreated extends MappedDateTime(this) {
    override def defaultValue = new Date
  }

  object lastUpdated extends MappedDateTime(this)
}

object Venue extends Venue
        with LongKeyedMetaMapper[Venue] {
  override def fieldOrder = List(id)

  def getClosestVenues(lat: Double, lng: Double, limit: Int): List[Venue] = {
    Venue.findAll(OrderBySql("abs(latitude - " + lat.toString + ") +" +
            " abs(longitude - " + lng.toString + ")",
      IHaveValidatedThisSQL("gyo", "2010-11-11")), MaxRows(limit))
  }

  import net.liftweb.util.Helpers.tryo
  def unapply(id: String): Option[Venue] = tryo {
    find(By(Venue.id, id.toLong)).toOption
  } openOr None
}