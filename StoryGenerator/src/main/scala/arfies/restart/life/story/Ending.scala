package arfies.restart.life.story

/**
 * 结局
 *
 * Author: sinar
 * 2022/6/22 22:56
 */
case class Ending(id: Int, name: String, condition: Option[Condition], achievement: Option[String], msg: String)
