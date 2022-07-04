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
  includeCond: String,
  effects: String,
  path: String,
  nextEventScope: String
)
