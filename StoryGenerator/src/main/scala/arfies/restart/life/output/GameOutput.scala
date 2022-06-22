package arfies.restart.life.output

import arfies.restart.life.output.GameOutput.Level
import arfies.restart.life.output.GameOutput.Level.OutputLevel

/**
 * 游戏输出
 *
 * Author: sinar
 * 2022/6/21 22:07
 */
trait GameOutput {
  /**
   * 输出消息（流程内）
   *
   * @param msg   消息
   * @param level 消息等级
   */
  def print(msg: String, level: OutputLevel = Level.Info): Unit

  /**
   * 弹出吐司消息（流程外）
   *
   * @param msg   消息
   * @param level 消息等级
   */
  def toast(msg: String, level: OutputLevel = Level.Info): Unit
}

object GameOutput {
  object Level {
    sealed trait OutputLevel

    case object Info extends OutputLevel

    case object Warning extends OutputLevel

    case object Danger extends OutputLevel

    case object Success extends OutputLevel
  }
}
