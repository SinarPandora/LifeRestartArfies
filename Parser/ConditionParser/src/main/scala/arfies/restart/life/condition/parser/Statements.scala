package arfies.restart.life.condition.parser

/**
 * 语句
 *
 * Author: sinar
 * 2022/8/21 23:03
 */
object Statements {
  sealed trait Statement

  case class Compare(name: String, opt: String, value: String) extends Statement

  case class ExistOrNot(target: String, opt: String, name: String) extends Statement

  case class AtOrHaveExp(opt: String, name: String) extends Statement

  case class Combine(tpe: String, stats: Seq[Statement]) extends Statement
}
