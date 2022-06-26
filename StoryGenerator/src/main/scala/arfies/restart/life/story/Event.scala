package arfies.restart.life.story

import arfies.restart.life.player.PlayerChange

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
  roundGreatEqual: Option[Int],
  roundLessThan: Option[Int],
  includeCond: Condition,
  effects: Seq[PlayerChange],
  path: Option[Seq[String]]
)
