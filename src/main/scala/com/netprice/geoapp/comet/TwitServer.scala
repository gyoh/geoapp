package com.netprice.geoapp.comet

import com.netprice.geoapp.model._
import net.liftweb.http.ListenerManager
import net.liftweb.actor.LiftActor

/**
  * メッセージを配信するTwitServer
  * ScalaのActorを継承し、Liftで用意されている
  * ListenerManagerトレイトを継承することで、
  * 登録されている他のActorにメッセージを配信できる。
  */
object TwitServer extends LiftActor with ListenerManager  { // *1

  // 投稿されたメッセージ(配信されるとクリアされる)
  private var msgs: List[Message] = Nil

  // 登録されている他のActorに配信するメッセージ
  protected def createUpdate = Messages(msgs)

  // メッセージを受信した際の処理
  override def highPriority = { // *2
    case m: Message =>
      msgs ::= m
      // 他のActorにメッセージを配信する
      updateListeners()
      msgs = Nil
  }
  //this.start // *3
}

case class Messages(msgs: List[Message]) // *4