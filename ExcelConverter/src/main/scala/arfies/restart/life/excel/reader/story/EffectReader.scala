package arfies.restart.life.excel.reader.story

import arfies.restart.life.excel.parser.phase.Keywords
import arfies.restart.life.parser.effect.{Parser, Statements}
import arfies.restart.life.parser.effect.Statements.Statement
import arfies.restart.life.story.{Condition, Effect}
import arfies.restart.life.story.Effect.{AchievementGet, AndChanges, BuffChange, EndingEnter, NumAttrChange, Opts, OrChanges, PathMove, SkillChange, TagChange, TalentChange, Targets}

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
 * 效果读取器
 *
 * Author: sinar
 * 2022/10/10 22:56
 */
object EffectReader {
  private val NUMBER_OPTS: Set[String] = Set(Opts.ADD, Opts.SUB, Opts.MUL, Opts.DIV)

  /**
   * 读取
   *
   * @param raw      原始字符串
   * @param keywords 关键词
   * @return 解析结果（可能包含错误信息）
   */
  def read(raw: String, keywords: Keywords): Either[String, Effect] = {
    Try(Parser(raw).parse()) match {
      case Failure(exception) => Left(exception.getMessage)
      case Success(stat: Statement) => readStat(keywords)(stat)
    }
  }

  /**
   * 解析语句
   *
   * @param keywords 关键词
   * @param stat     语句
   * @return 解析结果
   */
  private def readStat(keywords: Keywords)(stat: Statement): Either[String, Effect] = {
    stat match {
      case Statements.Update(name, opt, value) =>
        val isAttr = keywords.exists(name, Condition.Targets.ATTR)
        val isTag = keywords.exists(name, Condition.Targets.TAG)
        if (isAttr.isLeft && isTag.isLeft) Left("属性/标签不存在")
        else {
          if (isAttr.isRight) Right(NumAttrChange(name, value.toInt, opt))
          else if (NUMBER_OPTS.contains(opt)) Left(s"不能对标签 $name 应用数学运算符：$opt")
          else Right(TagChange(name, Some(value), opt))
        }
      case Statements.GetOrLost(tpe, name, opt, round) =>
        if (tpe == Targets.ATTR) {
          if (opt == Opts.ADD) Left(s"请使用 设置为 操作符设置标签${name}的值")
          else Right(TagChange(name, None, Opts.DEL))
        } else Right {
          tpe match {
            case Targets.TALENT => TalentChange(name, opt)
            case Targets.SKILL => SkillChange(name, opt)
            case Targets.ACHIEVEMENT => AchievementGet(name)
            case Targets.BUFF => BuffChange(name, opt, round)
          }
        }
      case Statements.Into(tpe, name) =>
        Right {
          tpe match {
            case Targets.ENDING => EndingEnter(name)
            case Targets.PATH => PathMove(name)
          }
        }
      case Statements.Combine(tpe, stats) =>
        val errors = ListBuffer[String]()
        val effects = ListBuffer[Effect]()
        stats.map(readStat(keywords)).foreach {
          case Left(value) => errors += value
          case Right(value) => effects += value
        }
        (if (errors.nonEmpty) Left(errors.mkString("\n")) else Right(effects.toSeq)).map { effects =>
          tpe match {
            case Targets.AND => AndChanges(effects)
            case Targets.OR => OrChanges(effects)
          }
        }
    }
  }
}
