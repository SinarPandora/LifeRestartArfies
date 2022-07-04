package arfies.restart.life.story

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
  weight: Int,
  includeCond: Condition,
  effects: Seq[Effect],
  path: Seq[String],
  nextEventScope: Seq[String] // 下回合事件将从这之中抽取
)
