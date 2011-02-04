package bootstrap.liftweb

import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.http.provider._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import com.netprice.geoapp.model._
import com.netprice.geoapp.api.RestAPI
import net.liftweb.mapper._
import net.liftweb.mongodb.{MongoDB, DefaultMongoIdentifier, MongoAddress, MongoHost}

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor =
      new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
        Props.get("db.url") openOr
                "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
        Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
      //DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)
    }

    MongoDB.defineDb(
		  DefaultMongoIdentifier,
		  MongoAddress(MongoHost(), "venuecrawler")
		)

    // where to search snippet
    LiftRules.addToPackages("com.netprice.geoapp")
    Schemifier.schemify(true, Schemifier.infoF _, User, Message, Venue)

    // Build SiteMap
    def sitemap() = SiteMap(
      Menu("Home") / "index" :: // Simple menu form
              // Menu with special Link
              Menu(Loc("Static", Link(List("static"), true, "/static/index"),
                "Static Content")) ::
              Menu(Loc("Twit", List("twit"), "Twitter Clone")) ::
              Menu(Loc("Comet", List("comet"), "Twitter Clone (Comet)")) ::
              Menu(Loc("GeoRSS", List("georss"), "GeoRSS")) ::
              // Menu entries for the User management stuff
              User.sitemap: _*)

    LiftRules.setSiteMapFunc(sitemap)

    // Tie in the REST API.
    LiftRules.statelessDispatchTable.append(RestAPI)

    /*
     * Show the spinny image when an Ajax call starts
     */
    LiftRules.ajaxStart =
            Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    /*
     * Make the spinny image go away when it ends
     */
    LiftRules.ajaxEnd =
            Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(makeUtf8)
    LiftRules.responseTransformers.append(setContentType)

    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    S.addAround(DB.buildLoanWrapper)
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
    req.setCharacterEncoding("UTF-8")
  }

  /**
   * Force response content-type to be "text/html"
   * rather than "application/xhtml+xml".
   * This is a workaround for using google maps api.
   */
  private def setContentType(org: LiftResponse): LiftResponse = {
    org match {
      case x: XhtmlResponse =>
        S.skipXmlHeader = true
        val m = x.toResponse
        val h = x.headers ::: ("Content-Type", "text/html; charset=UTF-8") :: Nil
        InMemoryResponse(m.data, h, m.cookies, m.code)
      case _ => org
    }
  }

}
