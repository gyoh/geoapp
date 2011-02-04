package com.netprice.geoapp.snippet

import scala.xml.NodeSeq
import net.liftweb.http.S
import net.liftweb.http.SHtml._
import net.liftweb.util.Helpers._
import com.netprice.geoapp.model._
import net.liftweb.common.Box
import com.netprice.geoapp.comet.TwitServer

/**
 * ついったーのようなものの画面で使用するSnippet
 */
class Twit {
  /**
   * <lift:Twit.post>タグで呼び出されるSnippet関数
   * 入力フォームを生成する
   */
  def post( xhtml:NodeSeq  ):NodeSeq  =  {
    // User.currentUserで現在ログインしているユーザーを取得。
    val user = User.currentUser

    // Messageモデルを作成する
    val message = Message.create.user( user )

    // ユーザー名
    val name = userName( User.currentUser )

    // submitされた時点で呼び出され、メッセージの内容を
    // データベースに保存する関数
    def addMessage:Unit = message.validate match {
      // パターンマッチで、入力チェックの結果エラーが
      // 発生していない場合のみ登録
      //case Nil => message.save ; S.notice("メッセージを投稿しました。")
      case Nil => {
        message.save
        TwitServer ! message
        S.notice("メッセージを投稿しました。")
      }
      // エラーが発生してる場合はメッセージを表示
      case x => S.error( x )
    }

    // bind関数を利用して、引数のXHTML内で<twit:...>で始まるタグの
    // 内容を置き換える
    bind("twit", xhtml,
      "name" -> name,
      "status" -> message.status.toForm,
      "submit" -> submit( "投稿する", () => addMessage )
    )
  }

  /**
   * <lift:Twit.show>タグで呼び出されるSnippet関数
   *  投稿されたメッセージを表示する
   */
  def show( xhtml:NodeSeq  ):NodeSeq = {
    // <xml:Group>で複数のタグをグルーピング
    <xml:Group>{
      // Messageモデルから全件検索して、
      // それぞれのレコードをbind関数を利用してXHMTLに変換
      Message.findAll.flatMap{ msg =>
        bind("twit", xhtml,
          "message" -> msg.status.is ,
          "user" -> userName( msg.user.obj ),
          "dateOf" -> msg.dateOf.is.toString
        )
      }
    }</xml:Group>
  }

  /**
   * Userモデルオブジェクトからユーザー名を取得するユーティリティ関数
   */
  def userName( user:Box[User] ) = user.dmap( "Guest" ){ user =>
    user.shortName
  }
}