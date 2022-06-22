package arfies.restart.life.player

import arfies.restart.life.story.Condition

/**
 * 角色
 *
 * Author: sinar
 * 2022/6/21 21:55
 */
case class Player
(
  attrs: Map[String, Int], // 属性
  tags: Map[String, String], // 标签
  skills: Set[String], // 持有技能（因为技能实现为 lambda，所以此处只保留名称）
  talents: Set[String], // 持有天赋（因为天赋实现为 lambda，所以此处只保留名称）
  buffs: Map[String, Option[Int]], // Buff，名称 + 剩余回合数
  path: String, // 人生轨道
  isAlive: Boolean // 角色是否存活（死亡代表游戏结束）
)

object Player {
  /**
   * 属性
   *
   * @param name           属性名
   * @param attrType       类型，数值或标签
   * @param isCustomizable 是否可定制（是否可 Roll）
   * @param isPinOnHub     是否始终显示在事件列表上方的 HUB
   */
  case class Attr(name: String, attrType: String, isCustomizable: Boolean, isPinOnHub: Boolean)

  /**
   * 属性变更
   */
  sealed trait AttrChange

  /**
   * 数值属性变更
   *
   * @param name   属性名
   * @param change 变更函数
   */
  case class NumValueChange(name: String, change: Int => Int) extends AttrChange

  /**
   * 标签属性变更
   *
   * @param name    属性名
   * @param toValue 目标值
   */
  case class TagChange(name: String, toValue: String) extends AttrChange

  /**
   * 技能（也表示天赋）
   *
   * @param name        名称
   * @param activeOn    触发条件
   * @param attrChanges 属性变更
   * @param otherEffect 其他效果
   * @param isTalent    是否为天赋（即可以在开场抽取）
   *                    没有条件的天赋将在开场时自动生效
   */
  case class Skill(name: String, msg: String, otherEffect: Player => Player, attrChanges: Seq[AttrChange], activeOn: Condition, isTalent: Boolean)

  /**
   * Buff
   *
   * @param name        名称
   * @param activeOn    触发条件（主动）
   * @param attrChanges 属性变更
   * @param otherEffect 其他效果
   * @param roundCount  回合数
   */
  case class Buff(name: String, msg: String, otherEffect: Player => Player, attrChanges: Seq[AttrChange], activeOn: Condition, roundCount: Option[Int])
}
