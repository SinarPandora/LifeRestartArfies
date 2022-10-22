package arfies.restart.life.excel.ir

/**
 * Buff IR
 * TODO: 三个效果必须有一个存在
 *
 * Author: sinar
 * 2022/7/4 22:27
 */
case class BuffIR
(
  name: String, msg: String,
  effect: Option[String], onAddEffect: Option[String], onLeaveEffect: Option[String],
  timing: String, condition: Option[String], roundCount: Option[Int],
  doubleApplicable: Boolean, rowCount: Int
) extends Named
