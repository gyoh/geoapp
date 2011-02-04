package com.netprice.geoapp.api

import net.liftweb.json.Xml
import java.text.SimpleDateFormat
import net.liftweb.json.JsonAST.JValue
import com.netprice.geoapp.model.Venue
import xml.Node
import net.liftweb.mongodb.MongoDB
import com.mongodb.{DBObject, BasicDBList, BasicDBObject}
import collection.mutable.ListBuffer

/**
 * Created by IntelliJ IDEA.
 * User: gyo
 * Date: Nov 11, 2010
 * Time: 3:42:59 PM
 * To change this template use File | Settings | File Templates.
 */

object RestFormatters {
  /* The REST timestamp format. Not threadsafe, so we create a new one each time. */
  def timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
  // A simple helper to generate the REST ID of an Expense
  def restId(venue: Venue) = "http://www.hamamoto.org/geoapp/api/venue/" + venue.id
  // A simple helper to generate the REST timestamp of an Expense
  def restDateCreated(venue: Venue): String = timestamp.format(venue.dateCreated.is)
  def restLastUpdated(venue: Venue): String = timestamp.format(venue.lastUpdated.is)

  /**
   * Generates the JSON REST representation of a Venue
   */
  def toJSON(venue: Venue): JValue = {
    import net.liftweb.json.JsonDSL._
    import net.liftweb.json.JsonAST._

    ("venue" ->
      ("id" -> restId(venue)) ~
      ("name" -> venue.name.is) ~
      ("address" -> venue.address.is) ~
      ("latitude" -> venue.latitude.is) ~
      ("longitude" -> venue.longitude.is) ~
      ("woeid" -> venue.woeid.is) ~
      ("dateCreated" -> restDateCreated(venue)) ~
      ("lastUpdated" -> restLastUpdated(venue)))
  }

  /**
   * Generates the XML REST representation of a Venue
   */
  def toXML(venue: Venue): Node = Xml.toXml(toJSON(venue)).first

  /*
   * Generates an Atom 1.0 feed from the last 10 Venues close to the given
   * latitude and longitude.
   */
  def toAtom (lat: Double, lng: Double, limit: Int) : Node = {
    val entries = Venue.getClosestVenues(lat, lng, limit)

    <feed xmlns="http://www.w3.org/2005/Atom"
          xmlns:dc="http://purl.org/dc/elements/1.1/"
          xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
          xmlns:georss="http://www.georss.org/georss"
          xmlns:woe="http://where.yahooapis.com/v1/schema.rng">

      <title>Geocrawler RSS</title>
      <id>http://www.hamamoto.org/geoapp/api/venue/</id>
      <updated>{timestamp.format(new java.util.Date)}</updated>
      { entries.flatMap(toAtom) }
    </feed>
  }

  /*
   * Generates the XML Atom representation of a Venue
   */
  def toAtom (venue : Venue) : Node = {
    <entry>
      <id>{restId(venue)}</id>
      <title>{venue.name.is}</title>
      <published>{restDateCreated(venue)}</published>
      <updated>{restLastUpdated(venue)}</updated>
      <content type="html">
        &lt;p&gt;&lt;a href=&quot;{venue.url.is}&quot;&gt;{venue.url.is}&lt;/a&gt;&lt;/p&gt;
      </content>
      <author>
        <name>gyo</name>
      </author>
      <georss:point>{venue.latitude.is} {venue.longitude.is}</georss:point>
      <geo:lat>{venue.latitude.is}</geo:lat>
      <geo:long>{venue.longitude.is}</geo:long>
      <woe:woeid>{venue.woeid.is}</woe:woeid>
    </entry>
  }

  def fromMongoToAtom(lat: Double, lng: Double, limit: Int): Node = {
    MongoDB.useCollection("venues")(coll => {
      val query = new BasicDBObject()
      val geo = new BasicDBObject()
      val near = new BasicDBList()
      near.put(0, lat)
      near.put(1, lng)
      geo.put("$near", near)
      query.put("geo", geo)
      val venues = coll.find(query).limit(limit)
      val buf = new ListBuffer[DBObject]
      while (venues.hasNext) buf += venues.next

      <feed xmlns="http://www.w3.org/2005/Atom"
            xmlns:dc="http://purl.org/dc/elements/1.1/"
            xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
            xmlns:georss="http://www.georss.org/georss"
            xmlns:woe="http://where.yahooapis.com/v1/schema.rng">

        <title>Geocrawler RSS</title>
        <id>http://www.hamamoto.org/geoapp/api/venue/</id>
        <updated>{timestamp.format(new java.util.Date)}</updated>
        { buf.flatMap(toAtom) }
      </feed>
    })
  }

  def toAtom(venue: DBObject): Node = {
    <entry>
      <id>{venue.get("url")}</id>
      <title>{venue.get("name")}</title>
      <published>{timestamp.format(venue.get("lastUpdated"))}</published>
      <updated>{timestamp.format(venue.get("lastUpdated"))}</updated>
      <content type="html">
        &lt;p&gt;&lt;a href=&quot;{venue.get("url")}&quot;&gt;{venue.get("url")}&lt;/a&gt;&lt;/p&gt;
      </content>
      <author>
        <name>gyo</name>
      </author>
      <georss:point>{venue.get("geo").asInstanceOf[DBObject].get("latitude")} {venue.get("geo").asInstanceOf[DBObject].get("longitude")}</georss:point>
      <geo:lat>{venue.get("geo").asInstanceOf[DBObject].get("latitude")}</geo:lat>
      <geo:long>{venue.get("geo").asInstanceOf[DBObject].get("longitude")}</geo:long>
    </entry>
  }
}