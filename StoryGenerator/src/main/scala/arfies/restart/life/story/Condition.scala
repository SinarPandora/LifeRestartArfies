package arfies.restart.life.story

import arfies.restart.life.state.GameState

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
 * 拥有属性/标签/天赋/技能/buff/Buff：[名称]
 * 没有属性/标签/天赋/技能/buff/Buff：[名称]
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
    val PLAYER_INIT: String = "PlayerInit"
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
    val AND: String = "AND"
    val OR: String = "OR"
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

  case class AndCondition(timing: String, conditions: Seq[Condition]) extends Condition(Targets.AND, timing, Targets.NULL, Targets.AND)

  case class OrCondition(timing: String, conditions: Seq[Condition]) extends Condition(Targets.OR, timing, Targets.NULL, Targets.OR)

  /**
   * 是否满足条件
   *
   * @param gameState 当前游戏状态
   * @param condition 条件列表
   * @return 判定结果
   */
  def isMeetCondition(gameState: GameState, condition: Condition): Boolean = {
    val GameState(roundCount, player, _, _, eventHistories, _, _, _) = gameState
    condition match {
      // 立刻执行
      case _: ImmediatelyActivate => true
      // 回合数判定
      case RoundCondition(_, after, before) =>
        ((after, before): @unchecked) match {
          case (Some(after), Some(before)) => roundCount >= after && roundCount < before
          case (Some(after), None) => roundCount >= after
          case (None, Some(before)) => roundCount < before
        }
      // 属性判定
      case NumAttrCondition(_, name, opt, b) =>
        player.attrs.get(name) match {
          case Some(a) =>
            import Opts.Attr.*
            // TODO 在解析阶段处理不合理的判定条件
            opt match {
              case GREAT => a > b
              case GREAT_EQUAL => a >= b
              case LESS => a < b
              case LESS_EQUAL => a <= b
              case EQUAL => a == b
              case NOT_EQUAL => a != b
            }
          case None => false // 属性不存在时视作判定失败
        }
      // 标签判定
      case TagCondition(_, name, opt, b) =>
        player.tags.get(name) match {
          case Some(a) =>
            import Opts.Attr.*
            // TODO 在解析阶段处理不合理的判定条件
            opt match {
              case EQUAL => a == b
              case NOT_EQUAL => a != b
            }
          case None => false
        }
      // 存在判定
      case ExistOrNotCondition(target, _, name, opt) =>
        import Targets.*
        // TODO 在解析阶段处理不合理的判定条件
        val exist = target match {
          case ATTR => player.attrs.contains(name)
          case TAG => player.tags.contains(name)
          case SKILL => player.skills.contains(name)
          case TALENT => player.talents.contains(name)
          case BUFF => player.buffs.contains(name)
        }
        // TODO 在解析阶段处理不合理的判定条件
        if (opt == Opts.ExistOrNot.EXIST) exist else !exist
      // 人生轨道判定
      case PathCondition(_, name, opt) =>
        val onPath = player.path == name
        // TODO 在解析阶段处理不合理的判定条件
        if (opt == Opts.Path.AT) onPath else !onPath
      // 事件历史判定
      case EventHistoryCondition(_, name, opt) =>
        val haveExp = eventHistories.contains(name)
        // TODO 在解析阶段处理不合理的判定条件
        if (opt == Opts.EventHistory.HAVE_EXP) haveExp else !haveExp
      // 和判定
      case AndCondition(_, conditions) =>
        conditions.forall(isMeetCondition(gameState, _))
      // 或判定
      case OrCondition(_, conditions) =>
        conditions.exists(isMeetCondition(gameState, _))
    }
  }
}
