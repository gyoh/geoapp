package com.netprice.geoapp.comet

import net.liftweb.http._
import net.liftweb.http.js.jquery.JqJsCmds._
import net.liftweb.common.{Box, Full}
import com.netprice.geoapp.model.User

/**
 * ブラウザとComet接続し、非同期にメッセージを
 * 表示させるSnippet。
 * CometActorを継承し、他のListenerManagerを継承した
 * Actorに登録するため、CometListeneeトレイトを継承する。
 */
class CometTwit extends CometActor with CometListenee { // *1

  // このSnippetのNameSpace
  override def defaultPrefix = Full("twit")
  // 非同期にメッセージを表示させるタグのID
  private lazy val spanId = uniqueId + "_messages_span"

  // SinpetがTemplateから呼び出された時、
  // <Twit:messages/>タグの内容を出力する
  def render = bind("messages" -> // *2
          <span id={spanId}>
            <div></div>
          </span>)

  // このActorを他のActorに登録する。
  // ここでは、TwitServer Actorに登録
  protected def registerWith = TwitServer // *3

  // このActorがメッセージを受け取った時の処理
  override def highPriority = { // *4

    // caseクラス Messagesを受け取る
    case Messages(msgs) =>
      // partialUpdate関数にをPrependHtmlオブジェクト渡すことで、
      // htmlを部分的に書き換えるJavaScrpitが生成される
      partialUpdate(PrependHtml(spanId, // *5
        <xml:Group>
          {msgs.map(m =>
          <ul class="status">
            <li class="message">
              {m.status.is}
            </li>
            <li class="user">
              {userName(m.user.obj)}
            </li>
            <li class="dateOf">
              {m.dateOf.is.toString}
            </li>
              <hr/>
          </ul>)}
        </xml:Group>))
  }

  /**
   * Userモデルオブジェクトからユーザー名を取得するユーティリティ関数
   */
  def userName(user: Box[User]) = user.dmap("Guest") {
    user =>
      user.shortName
  }
}