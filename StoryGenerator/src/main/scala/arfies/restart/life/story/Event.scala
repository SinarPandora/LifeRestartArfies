package arfies.restart.life.story

import arfies.restart.life.player.Player
import arfies.restart.life.player.Player.AttrChange
import arfies.restart.life.story.Event.AgeCondition

/**
 * 事件
 *
 * Author: sinar
 * 2022/6/21 23:17
 */
case class Event
(
  name: String,
  msg: String,
  ageCond: AgeCondition,
  includeCond: Player => Boolean,
  excludeCond: Player => Boolean,
  attrChanges: Seq[AttrChange],
  otherEffect: Player => Player,
  path: Option[Seq[String]]
)

object Event {
  /**
   * 年龄条件（左闭右开）
   *
   * @param after  大于等于
   * @param before 小于
   */
  case class AgeCondition(after: Option[Int], before: Option[Int])
}
