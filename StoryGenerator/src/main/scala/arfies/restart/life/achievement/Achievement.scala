package arfies.restart.life.achievement

import arfies.restart.life.story.Condition

/**
 * 成就
 *
 * Author: sinar
 * 2022/6/21 22:04
 */
case class Achievement(name: String, msg: Option[String], condition: Condition)
