package arfies.restart.life.excel.ir

/**
 * 技能/天赋 IR
 *
 * Author: sinar
 * 2022/7/4 22:26
 */
case class SkillIR
(
  name: String,
  msg: String,
  effect: Option[String],
  timing: String,
  condition: Option[String],
  isTalent: Boolean,
  rowCount: Int
) extends Named
