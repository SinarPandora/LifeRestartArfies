package arfies.restart.life.excel.ir

/**
 * 事件 IR
 *
 * Author: sinar
 * 2022/7/4 23:09
 */
case class EventIR
(
  name: String,
  msg: String,
  weight: Int,
  afterRound: Option[Int],
  beforeRound: Option[Int],
  includeCond: Option[String],
  effect: Option[String],
  path: Option[String],
  nextEventScope: Option[String],
  rowCount: Int
) extends Named
