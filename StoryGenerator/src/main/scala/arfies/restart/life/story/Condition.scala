package arfies.restart.life.story

/**
 * 条件
 * 与玩家变化相同，必须可以一比一序列化为 JSON，
 * 因此不能出现任何 Lambda
 * --------------------------------------------
 * [属性名]>[数字]
 * [属性名]>=[数字]
 * [属性名]<[数字]
 * [属性名]<=[数字]
 * [属性名]=[数字]
 * [属性名]==[数字]
 * [属性名]!=[数字]
 * [属性名]=!=[数字]
 * 拥有[天赋/技能/buff名]
 * 没有[天赋/技能/buff名]
 * 处于[人生轨道名]
 * 不在[人生轨道名]
 * 经历过[事件名]
 * 未经历[事件名]
 *
 * Author: sinar
 * 2022/6/21 22:50
 */
sealed abstract class Condition(val target: String, timing: String, name: String, opt: String) {
  def timing: String
}

object Condition {
  object Timing {
    val BEFORE_ROUND: String = "BeforeRound"
    val AFTER_ROUND: String = "AfterRound"
    val AFTER_ATTRS_CHANGE: String = "AfterAttrsChange"
    val AFTER_ATTRS_UP: String = "AfterAttrsUp"
    val AFTER_ATTRS_DOWN: String = "AfterAttrsDown"
  }

  object Targets {
    val ATTR: String = "ATTR"
    val TAG: String = "TAG"
    val SKILL: String = "SKILL"
    val TALENT: String = "TALENT"
    val BUFF: String = "BUFF"
    val PATH: String = "PATH"
    val EVENT_HISTORY: String = "EVENT_HISTORY"
    val ROUND: String = "ROUND"
    val NULL: String = "NULL" // 用于立刻触发
  }

  object Opts {
    object Direct {
      val PASS: String = "PASS" // 不判断，直接通过
    }
    object Attr {
      val GREAT: String = ">"
      val GREAT_EQUAL: String = ">="
      val LESS: String = "<"
      val LESS_EQUAL: String = "<="
      val EQUAL: String = "==" // 解析判断相等时，从属性/标签池判断目标类型
      val NOT_EQUAL: String = "!=" // 对判断不等同理
    }
    object ExistOrNot {
      val EXIST: String = "EXIST"
      val NON_EXIST: String = "NON_EXIST"
    }
    object Path {
      val AT: String = "AT"
      val NOT_AT: String = "NOT_AT"
    }
    object EventHistory {
      val HAVE_EXP: String = "HAVE_EXP" // 经历过
      val NOT_HAVE: String = "NOT_HAVE" // 未经历
    }
  }
  case class RoundCondition(timing: String, after: Option[Int], before: Option[Int]) extends Condition(Targets.ROUND, timing, Targets.ROUND, Opts.Direct.PASS)
  case class ImmediatelyActivate(timing: String) extends Condition(Targets.NULL, timing, Targets.NULL, Opts.Direct.PASS)
  case class NumAttrCondition(timing: String, name: String, opt: String, value: Int) extends Condition(Targets.ATTR, timing, name, opt)
  case class TagCondition(timing: String, name: String, opt: String, value: String) extends Condition(Targets.TAG, timing, name, opt)
  case class ExistOrNotCondition(override val target: String, timing: String, name: String, opt: String) extends Condition(target, timing, name, opt)
  case class PathCondition(timing: String, name: String, opt: String) extends Condition(Targets.PATH, timing, name, opt)
  case class EventHistoryCondition(timing: String, name: String, opt: String) extends Condition(Targets.EVENT_HISTORY, timing, name, opt)

}
