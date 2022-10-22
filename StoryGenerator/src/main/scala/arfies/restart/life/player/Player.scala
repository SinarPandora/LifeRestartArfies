package arfies.restart.life.player

import arfies.restart.life.story.{Condition, Effect}

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
  object AttrType {
    val NUM: String = "NUM"
    val TAG: String = "TAG"
  }

  /**
   * 属性
   *
   * @param name           属性名
   * @param initMax        初始化可设最大值（在可定制时生效）
   * @param attrType       类型，数值或标签
   * @param isCustomizable 是否可定制（是否可 Roll）
   * @param isPinOnHub     是否始终显示在事件列表上方的 HUB
   */
  case class Attr(name: String, initMax: Option[Int], attrType: String, isCustomizable: Boolean, isPinOnHub: Boolean)

  /**
   * 技能（也表示天赋）
   *
   * @param name     名称
   * @param activeOn 触发条件
   * @param effect   效果列表
   * @param isTalent 是否为天赋（即可以在开场抽取）
   *                 没有条件的天赋将在开场时自动生效
   */
  case class Skill(name: String, msg: String, effect: Effect, activeOn: Condition, isTalent: Boolean)

  /**
   * Buff
   *
   * @param name             名称
   * @param activeOn         触发条件
   * @param effect           效果列表
   * @param onAddEffect      启动效果
   * @param onLeaveEffect    离场效果
   * @param roundCount       回合数（无回合数为永久）
   * @param doubleApplicable 是否可以叠加
   */
  case class Buff(name: String, msg: String,
                  effect: Option[Effect], onAddEffect: Option[Effect], onLeaveEffect: Option[Effect],
                  activeOn: Condition, roundCount: Option[Int],
                  doubleApplicable: Boolean)
}
