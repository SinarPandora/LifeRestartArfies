package arfies.restart.life.state

import arfies.restart.life.player.Player

/**
 * 状态（只针对当场游戏）
 *
 * Author: sinar
 * 2022/6/21 21:50
 */
case class GameState
(
  roundCount: Int, // 回合数，在人生重来中代表年龄
  player: Player,
  version: String,
  seed: Long,
  eventHistories: List[String], // 经历过的事件历史
  achievements: Seq[String], // 成就列表
  ending: Option[String],
  roundEventScope: Seq[String] // 当回合事件范围
)
