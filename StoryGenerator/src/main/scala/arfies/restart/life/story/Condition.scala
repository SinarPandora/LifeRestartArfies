package arfies.restart.life.story

import arfies.restart.life.player.Player

/**
 * 条件
 *
 * Author: sinar
 * 2022/6/21 22:50
 */
sealed trait Condition

object Condition {
  val ImmediatelyActivate: Player => Boolean = _ => true
  case class BeforeRound(check: Player => Boolean = ImmediatelyActivate) extends Condition
  case class AfterRound(check: Player => Boolean = ImmediatelyActivate) extends Condition
  case class AfterAttrsChange(names: Seq[String], check: Player => Boolean = ImmediatelyActivate) extends Condition
  case class AfterAttrsUp(names: Seq[String], check: Player => Boolean = ImmediatelyActivate) extends Condition
  case class AfterAttrsDown(names: Seq[String], check: Player => Boolean = ImmediatelyActivate) extends Condition
}
