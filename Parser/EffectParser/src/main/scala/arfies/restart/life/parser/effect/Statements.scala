package arfies.restart.life.parser.effect

/**
 * 语句
 *
 * Author: sinar
 * 2022/8/21 23:03
 */
object Statements {
  sealed trait Statement

  case class Update(name: String, opt: String, value: String) extends Statement

  case class GetOrLost(tpe: String, name: String, opt: String, round: Option[Int] = None) extends Statement

  case class Into(tpe: String, name: String) extends Statement

  case class Combine(tpe: String, stats: Seq[Statement]) extends Statement
}
