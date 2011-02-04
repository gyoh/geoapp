package com.netprice.geoapp {
package snippet {

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import com.netprice.geoapp.lib._
import Helpers._
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.SHtml._
import model.User

class HelloWorld {
  lazy val date: Box[Date] = DependencyFactory.inject[Date] // inject the date

  def howdy(in: NodeSeq): NodeSeq =
  Helpers.bind("b", in, "time" -> date.map(d => Text(d.toString)))

  /*
   lazy val date: Date = DependencyFactory.time.vend // create the date via factory

   def howdy(in: NodeSeq): NodeSeq = Helpers.bind("b", in, "time" -> date.toString)
   */

  // Ajaxを利用したボタンを生成するSnippet関数
  def greeting(html: NodeSeq) : NodeSeq = { // *1     // bind関数を利用してボタンを生成
    bind("greeting", html,

      // ajaxButto関数でサーバーへAjaxでリクエストを
      // 送信するボタンが生成される
      "button" -> ajaxButton( Text( "押して"),{ // *2

        // 第2引数には、Ajaxで呼び出されたときの
        // サーバー側の処理を関数オブジェクトで渡す
        () => {

          // ログ出力
          println("Ajaxで呼び出されました。") // *3
          // ログインしているユーザー名を取得
          val username  = User.currentUser.dmap( "Guest" ){ _.shortName } // *4
          // "greeting-div"に挨拶文を出力するJavaScriptを生成する
          SetHtml("greeting-div",
            Text ("こんにちわ! %sさん。".format( username ))) // *5
        }
      })
   )
  }
}

}
}
