package arfies.restart.life.player

/**
 * 玩家变化
 * 为了让故事对象能完全转换为 JSON，
 * 因此故事数据不能带有任何 Lambda；
 * 所有变化内容需要在这里使用 Model 表示
 * -------------------------------------
 * [属性名]+[数字]
 * [属性名]-[数字]
 * [属性名]*[数字]
 * [属性名]x[数字]
 * [属性名]/[数字]
 * 获得天赋: [技能名]
 * 失去天赋: [技能名]
 * 获得技能: [技能名]
 * 失去技能: [技能名]
 * 获得buff: [buff名]
 * 失去buff: [buff名]
 * 获得buff: [buff名][n]回合
 * 失去buff: [buff名][n]回合
 * 转入: [人生轨道名]
 * 获得成就: [成就名]
 * 进入结局：[结局名]
 *
 * Author: sinar
 * 2022/6/23 23:23
 */
sealed abstract class PlayerChange(val target: String)

object PlayerChange {
  object Opts {
    val ADD: String = "ADD"
    val SUB: String = "SUB"
    val MUL: String = "MUL"
    val DIV: String = "DIV"
    val SET: String = "SET"
    val DEL: String = "DEL"
  }
  object Targets {
    val ATTR: String = "ATTR"
    val TAG: String = "TAG"
    val SKILL: String = "SKILL"
    val TALENT: String = "TALENT"
    val BUFF: String = "BUFF"
    val PATH: String = "PATH"
    val ACHIEVEMENT: String = "ACHIEVEMENT"
    val ENDING: String = "ENDING"
  }
  case class AttrChange(name: String, value: Int, opt: String) extends PlayerChange(Targets.ATTR)
  case class TageChange(name: String, value: Option[String], opt: String) extends PlayerChange(Targets.TAG)
  case class SkillChange(name: String, opt: String)  extends PlayerChange(Targets.SKILL)
  case class TalentChange(name: String, opt: String)  extends PlayerChange(Targets.TALENT)
  case class BuffChange(name: String, opt: String)  extends PlayerChange(Targets.BUFF)
  case class PathMove(name: String)  extends PlayerChange(Targets.PATH)
  case class AchievementGet(name: String)  extends PlayerChange(Targets.ACHIEVEMENT)
  case class EndingGet(name: String)  extends PlayerChange(Targets.ENDING)
}
