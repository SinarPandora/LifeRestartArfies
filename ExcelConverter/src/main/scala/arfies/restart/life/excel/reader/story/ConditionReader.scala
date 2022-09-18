package arfies.restart.life.excel.reader.story

import arfies.restart.life.condition.parser.Statements.Statement
import arfies.restart.life.condition.parser.{Parser, Statements}
import arfies.restart.life.excel.parser.phase.Keywords
import arfies.restart.life.story.Condition
import arfies.restart.life.story.Condition.*

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
 * 条件读取器
 *
 * Author: sinar
 * 2022/7/7 23:12
 */
object ConditionReader {
  private val NUMBER_OPTS: Set[String] = Set(
    Opts.Attr.GREAT,
    Opts.Attr.GREAT_EQUAL,
    Opts.Attr.LESS,
    Opts.Attr.LESS_EQUAL
  )

  /**
   * 读取
   *
   * @param raw      原始字符串
   * @param timing   触发时机
   * @param keywords 关键词
   * @return 解析结果（可能包含错误信息）
   */
  def read(raw: String, timing: String, keywords: Keywords): Either[String, Condition] = {
    Try(Parser(raw).parse()) match {
      case Failure(exception) => Left(exception.getMessage)
      case Success(stat: Statement) => readStat(timing, keywords)(stat)
    }
  }

  /**
   * 解析语句
   *
   * @param timing   触发时机
   * @param keywords 关键词
   * @param stat     语句
   * @return 解析结果
   */
  private def readStat(timing: String, keywords: Keywords)(stat: Statement): Either[String, Condition] = {
    stat match {
      case Statements.Compare(name, opt, value) =>
        keywords.exists(name, Condition.Targets.ATTR)
          .flatMap { name =>
            if (NUMBER_OPTS.contains(opt)) {
              Try(value.toInt).toEither
                .left.map(_ => s"错误的数值格式：$value，属性名：$name")
                .map(num => NumAttrCondition(timing, name, opt, num))
            } else {
              Right(TagCondition(timing, name, opt, value))
            }
          }
      case Statements.ExistOrNot(target, opt, name) =>
        keywords.exists(name, target)
          .map(name => ExistOrNotCondition(target, timing, name, opt))
      case Statements.AtOrHaveExp(opt, name) =>
        opt match {
          case Condition.Opts.Path.AT | Condition.Opts.Path.NOT_AT =>
            Right(PathCondition(timing, name, opt))
          case Condition.Opts.EventHistory.HAVE_EXP | Condition.Opts.EventHistory.NOT_HAVE =>
            keywords.exists(name, Condition.Targets.EVENT_HISTORY)
              .map(name => EventHistoryCondition(timing, name, opt))
        }
      case Statements.Combine(tpe, stats) =>
        val errors = ListBuffer[String]()
        val conditions = ListBuffer[Condition]()
        stats.map(readStat(timing, keywords)).foreach {
          case Left(value) => errors += value
          case Right(value) => conditions += value
        }
        (if (errors.nonEmpty) Left(errors.mkString("\n")) else Right(conditions.toSeq)).map { conditions =>
          tpe match {
            case Condition.Targets.AND => AndCondition(timing, conditions)
            case Condition.Targets.OR => OrCondition(timing, conditions)
          }
        }
    }
  }
}
