package arfies.restart.life.excel.reader.story

import arfies.restart.life.story.Condition

/**
 * 触发时机读取器
 *
 * Author: sinar
 * 2022/9/18 12:23
 */
object TimingReader {
  /**
   * 读取时机
   *
   * @param raw 原内容
   * @return 转换后的时机
   */
  def read(raw: String): Either[String, String] = {
    raw match {
      case "回合开始" => Right(Condition.Timing.BEFORE_ROUND)
      case "回合结束" => Right(Condition.Timing.AFTER_ROUND)
      case "属性变化后" => Right(Condition.Timing.AFTER_ATTRS_CHANGE)
      case "属性上升后" => Right(Condition.Timing.AFTER_ATTRS_UP)
      case "属性下降后" => Right(Condition.Timing.AFTER_ATTRS_DOWN)
      case "角色初始化" => Right(Condition.Timing.PLAYER_INIT)
      case _ => Left(s"错误的触发时机：$raw")
    }
  }
}
