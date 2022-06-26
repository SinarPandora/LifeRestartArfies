package arfies.restart.life.output

import arfies.restart.life.output.GameOutput.Level

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
  def print(msg: String, level: String = Level.INFO): Unit

  /**
   * 弹出吐司消息（流程外）
   *
   * @param msg   消息
   * @param level 消息等级
   */
  def toast(msg: String, level: String = Level.INFO): Unit

  /**
   * 打印日志
   *
   * @param msg   消息
   * @param level 消息等级
   */
  def log(msg: String, level: String = Level.INFO): Unit

  /**
   * 输出调试信息
   *
   * @param msg 调试信息
   */
  def debug(msg: String): Unit = log(msg, level = Level.DEBUG)

  /**
   * 抛出异常
   *
   * @param msg 错误信息
   */
  def throws(msg: String): Unit
}

object GameOutput {
  object Level {
    val DEBUG: String = "Debug"
    val INFO: String = "Info"
    val WARNING: String = "Warning"
    val DANGER: String = "Danger"
    val SUCCESS: String = "Success"
  }
}
