package arfies.restart.life.achievement

import arfies.restart.life.output.GameOutput
import arfies.restart.life.story.Condition

/**
 * 成就
 *
 * Author: sinar
 * 2022/6/21 22:04
 */
case class Achievement(name: String, msg: Option[String], condition: Option[Condition])

object Achievement {
  /**
   * 展示成就信息
   *
   * @param out 游戏输出
   */
  def show(achievement: Achievement, out: GameOutput): Unit = {
    out.toast(s"获得成就：${achievement.name}", achievement.msg, GameOutput.Level.SUCCESS)
  }
}
