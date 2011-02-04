package com.netprice.geoapp.model

import java.util.Date
import net.liftweb.mapper._
import scala.xml.Text
import net.liftweb.util.FieldError

/**
 * 投稿されたメッセージのModel
 */
class Message extends LongKeyedMapper[Message] with IdPK {
  def getSingleton = Message

  /**
   * メッセージ本文
   */
  object status extends MappedTextarea(this, 140) {
    /**Textareaのカラム数 */
    override def textareaCols = 40

    /**Textareaの行数 */
    override def textareaRows = 4

    /**入力チェックの定義 */
    override def validations =
    // 入力されていない場合はエラー
      valNotNull("メッセージを入力してください。") _ ::
      // 140文字を超える場合はエラー
      valMaxLen(140, "メッセージは140文字以内です。") _ ::
      super.validations

    /**入力されているかチェックするための関数 */
    def valNotNull(msg: => String)(value: String): List[FieldError] =
      if ((value ne null) && (value ne "")) Nil
      else List(FieldError(this, Text(msg)))
  }

  /**
   * 投稿日時
   */
  object dateOf extends MappedDateTime(this) {
    override def defaultValue = new Date
  }

  /**
   * 投稿したユーザー
   */
  object user extends MappedLongForeignKey(this, User)
}

/**
 * Messageモデルに対するMetaMapper
 */
object Message extends Message
        with LongKeyedMetaMapper[Message] {
  override def fieldOrder = List(id)

  import net.liftweb.util.Helpers.tryo  
  def unapply(id: String): Option[Message] = tryo {
    find(By(Message.id, id.toLong)).toOption
  } openOr None
}