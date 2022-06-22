package arfies.restart.life.generator

import arfies.restart.life.player.Player
import arfies.restart.life.player.Player.AttrChange
import arfies.restart.life.state.GameState
import arfies.restart.life.story.Event

/**
 * 回合
 * "游戏就是状态的变化与表示"
 * 为了避免连续触发，每个回合每个技能/状态/天赋只触发一次
 *
 * Author: sinar
 * 2022/6/21 22:05
 */
class Round(ctx: StoryContext) {
  /**
   * 回合开始阶段
   * 判定顺序：
   * 1. 状态
   * 2. 天赋
   * 3. 技能
   * 4. 结局
   * *. 引发的属性变化
   *
   * @param state 游戏状态
   * @return 变更后的状态
   */
  def onStart(state: GameState): GameState = {
    ???
  }

  /**
   * 回合运行阶段
   * 判定顺序：
   * 1. 事件
   * *. 引发的属性变化
   *
   * @param state 游戏状态
   * @return 变更后的状态
   */
  def run(state: GameState): GameState = {
    ???
  }

  /**
   * 回合结束阶段
   * 判定顺序：
   * 1. 状态
   * 2. 天赋
   * 3. 技能
   * 4. 结局
   * *. 引发的属性变化
   *
   * @param state 游戏状态
   * @return 变更后的状态
   */
  def onEnd(state: GameState): GameState = {
    ???
  }

  /**
   * 当属性变更时
   * 判定顺序：
   * 1. 状态
   * 2. 天赋
   * 3. 技能
   * 4. 结局
   * *. 引发的属性变化
   * *. 属性变化向下取整
   *
   * @param state  游戏状态
   * @param change 属性变化
   * @return
   */
  def onAttrChange(state: GameState, change: AttrChange): GameState = {
    ???
  }
}

object Round {
  /**
   * 检出可触发的事件
   *
   * @param player     角色对象
   * @param roundCount 当前回合数
   * @param allEvents  全部事件
   * @return
   */
  def pickEvent(player: Player, roundCount: Int, allEvents: Seq[Event]): Seq[Event] = {
    ???
  }
}
