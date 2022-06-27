package arfies.restart.life.player

import arfies.restart.life.output.GameOutput
import arfies.restart.life.state.GameState
import arfies.restart.life.story.Story

/**
 * 角色变化
 * 为了让故事对象能完全转换为 JSON，
 * 因此故事数据不能带有任何 Lambda；
 * 所有变化内容需要在这里使用 Model 表示
 * -------------------------------------
 * [属性名]+[数字]
 * [属性名]-[数字]
 * [属性名]*[数字]
 * [属性名]x[数字]
 * [属性名]/[数字]
 * [属性名]设置为[值]
 * 删除[属性名]
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
    val ADD: String = "ADD" // 全部
    val SUB: String = "SUB" // 属性，标签，Buff
    val MUL: String = "MUL" // 属性，标签
    val DIV: String = "DIV" // 属性，标签
    val SET: String = "SET" // 属性，标签
    val DEL: String = "DEL" // 除了结局，轨道和成就
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
    val AND: String = "AND"
    val OR: String = "OR"
  }

  case class NumAttrChange(name: String, value: Int, opt: String) extends PlayerChange(Targets.ATTR)

  case class TageChange(name: String, value: Option[String], opt: String) extends PlayerChange(Targets.TAG)

  case class SkillChange(name: String, ot: String) extends PlayerChange(Targets.SKILL)

  case class TalentChange(name: String, opt: String) extends PlayerChange(Targets.TALENT)

  case class BuffChange(name: String, opt: String, roundCount: Option[Int]) extends PlayerChange(Targets.BUFF)

  case class PathMove(name: String) extends PlayerChange(Targets.PATH)

  case class AchievementGet(name: String) extends PlayerChange(Targets.ACHIEVEMENT)

  case class EndingGet(name: String) extends PlayerChange(Targets.ENDING)

  case class AndChanges(changes: Seq[PlayerChange]) extends PlayerChange(Targets.AND)

  case class OrChanges(changes: Seq[PlayerChange]) extends PlayerChange(Targets.OR)

  /**
   * 起效果
   * （不处理和与或效果）
   *
   * @param gameState 游戏状态
   * @param change    角色变化
   * @param out       游戏输出
   * @param story     故事对象
   * @return 效果后状态，当无效时返回 None
   */
  def perform(gameState: GameState, change: PlayerChange, out: GameOutput, story: Story): Option[GameState] = {
    val GameState(_, player, _, _, _, achievements, ending) = gameState
    (change: @unchecked) match {
      // ============================= 数值属性变化效果 =============================
      case NumAttrChange(name, value, opt) =>
        // TODO 解析阶段判断属性是否存在
        import Opts.*
        if (player.attrs.contains(name)) {
          val oldValue = player.attrs(name)
          var newValue = opt match {
            case ADD =>
              out.log(s"属性：$name 上升了 $value")
              oldValue + value
            case SET =>
              out.log(s"属性：$name 被设置为 $value")
              value
            case SUB =>
              if (oldValue - value <= 0) {
                out.log(s"属性：$name 减少到 0")
                0
              } else {
                out.log(s"属性：$name 减少了 $value")
                oldValue - value
              }
            case MUL =>
              out.log(s"属性：$name 变成了 $value 倍")
              oldValue * value
            case DIV =>
              out.log(s"属性：$name 变成了 $value 分之一")
              math.round(oldValue * 1.0 / value) // 四舍五入
            case DEL =>
              out.log(s"属性：$name 被删除")
              return Some(
                gameState.copy(player = player.copy(
                  attrs = player.attrs - name
                )))
          }
          // 越界检查
          newValue = if (newValue > Int.MaxValue) {
            out.log(s"属性：$name ${if (opt == ADD) "已达到最大值" else "设置为"}：$value")
            Int.MaxValue
          } else newValue
          Some(
            gameState.copy(player = player.copy(
              attrs = player.attrs + (name -> newValue.toInt)
            )))
        } else {
          opt match {
            case ADD | SET =>
              out.log(s"属性：$name ${if (opt == ADD) "已增加" else "设置为"}：$value")
              Some(
                gameState.copy(player = player.copy(
                  attrs = player.attrs + (name -> value)
                )))
            case _ =>
              out.debug(s"属性：$name 不存在，不进行操作")
              None
          }
        }
      // ============================= 标签变化效果 =============================
      case TageChange(name, value, opt) =>
        // TODO 解析阶段判断标签是否存在
        import Opts.{DEL, SET}
        opt match {
          case SET =>
            // TODO 解析阶段判断标签的值是否存在
            out.log(s"标签：$name 设置为：$value")
            Some(gameState.copy(player = player.copy(tags = player.tags + (name -> value.get))))
          case DEL =>
            if (player.tags.contains(name)) {
              out.log(s"标签：$name 已删除")
              Some(gameState.copy(player = player.copy(tags = player.tags - name)))
            } else {
              out.debug(s"标签：$name 不存在，不进行删除")
              None
            }
        }
      // ============================= 技能变化效果 =============================
      case SkillChange(name, opt) =>
        import Opts.{ADD, DEL}
        // TODO 解析阶段判断天赋是否存在
        opt match {
          case ADD =>
            if (player.skills.contains(name)) {
              out.debug(s"技能：$name 已存在")
              None
            } else {
              out.print(s"获得技能：$name", GameOutput.Level.SUCCESS)
              Some(gameState.copy(player = player.copy(skills = player.skills + name)))
            }
          case DEL =>
            if (player.skills.contains(name)) {
              out.print(s"失去技能：$name", GameOutput.Level.WARNING)
              Some(gameState.copy(player = player.copy(skills = player.skills - name)))
            } else {
              out.debug(s"技能：$name 不存在，不进行删除")
              None
            }
        }
      // ============================= 天赋变化效果 =============================
      case TalentChange(name, opt) =>
        import Opts.{ADD, DEL}
        // TODO 解析阶段判断天赋是否存在
        opt match {
          case ADD =>
            if (player.talents.contains(name)) {
              out.debug(s"天赋：$name 已存在")
              None
            } else {
              out.print(s"获得天赋：$name", GameOutput.Level.SUCCESS)
              Some(gameState.copy(player = player.copy(talents = player.talents + name)))
            }
          case DEL =>
            if (player.talents.contains(name)) {
              out.print(s"失去天赋：$name", GameOutput.Level.WARNING)
              Some(gameState.copy(player = player.copy(talents = player.talents - name)))
            } else {
              out.debug(s"天赋：$name 不存在，不进行删除")
              None
            }
        }
      // ============================= Buff变化效果 =============================
      case BuffChange(name, opt, roundCount) =>
        import Opts.{ADD, SUB}
        // TODO 解析阶段判断 Buff 是否存在
        val buff = story.buffs(name)
        opt match {
          case ADD =>
            player.buffs.get(name) match {
              case Some(remainOpt) =>
                // 1. 如果 buff 不可叠加，直接退出
                // 2. 如果 buff 可以叠加，计算叠加回合数
                if (buff.doubleApplicable) {
                  remainOpt match {
                    case Some(remain) =>
                      // 当不给出回合数时，使用默认回合数
                      val newRemain = roundCount.orElse(buff.roundCount).map(_ + remain)
                      out.log(s"Buff：$name，已延长为${newRemain.getOrElse("永续")}回合")
                      out.print(s"Buff：$name 已延长")
                      Some(
                        gameState.copy(player = player.copy(
                          buffs = player.buffs + (buff.name -> newRemain)
                        )))
                    case None =>
                      // buff 已经为永续，叠加无效
                      out.debug(s"Buff：$name，已为永续，叠加无效")
                      None
                  }
                } else None
              case None =>
                out.print(s"已获得 Buff：$name")
                Some(
                  gameState.copy(player = player.copy(
                    buffs = player.buffs + (buff.name -> roundCount.orElse(buff.roundCount))
                  ))) // 不存在 Buff 时直接叠
            }
          case SUB =>
            player.buffs.get(name) match {
              case Some(remainOpt) =>
                // 1. 如果 buff 已经是永续，减少无效
                // 2. 计算减少，如果结果非正数，则拿掉 buff
                remainOpt match {
                  case Some(remain) =>
                    // 如果未给出减少回合数，视为直接清除
                    val newRemain = remain - roundCount.getOrElse(remain)
                    Some {
                      gameState.copy(player = player.copy(
                        buffs = if (newRemain <= 0) {
                          // 非正数直接清除
                          out.print(s"Buff：$name 已消失")
                          player.buffs - name
                        } else {
                          out.print(s"Buff：$name 已缩短")
                          out.log(s"Buff：$name，已缩短为${newRemain}回合")
                          player.buffs + (buff.name -> Some(newRemain))
                        }
                      ))
                    }
                  case None =>
                    out.debug(s"Buff：$name，已为永续，缩短无效")
                    None
                }
              case None => None
            }
        }
      // ============================= 移动轨道效果 =============================
      case PathMove(name) =>
        if (player.path != name) {
          out.print(s"已解锁人生轨道：$name")
          Some(gameState.copy(player = player.copy(path = name)))
        }
        else None
      // ============================= 获得成就效果 =============================
      case AchievementGet(name) =>
        if (achievements.contains(name)) None
        else {
          // TODO 解析阶段判断成就是否存在
          val achievement = story.achievements(name)
          out.toast(
            s"""获得成就：$name
               |${achievement.msg}""".stripMargin, GameOutput.Level.SUCCESS)
          Some(gameState.copy(achievements = achievements :+ name))
        }
      // ============================= 获得结局效果 =============================
      case EndingGet(name) =>
        if (ending.isEmpty) {
          // TODO 解析阶段判断结局是否存在
          val ending = story.endings(name)
          out.print(
            s"""达成结局：$name
               |${ending.msg}""".stripMargin)
          Some(gameState.copy(ending = Some(name)))
        }
        else None
    }
  }
}
