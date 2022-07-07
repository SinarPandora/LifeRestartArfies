package arfies.restart.life.excel.ir

/**
 * 结局 IR
 *
 * Author: sinar
 * 2022/7/4 23:54
 */
case class EndingIR
(
  id: Int,
  name: String,
  timing: Option[String],
  condition: Option[String],
  achievement: Option[String],
  msg: String,
  rowCount: Int
) extends Named
