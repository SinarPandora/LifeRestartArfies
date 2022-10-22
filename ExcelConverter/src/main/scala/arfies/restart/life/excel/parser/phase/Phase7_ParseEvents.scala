package arfies.restart.life.excel.parser.phase

import arfies.restart.life.excel.ir.EventIR
import arfies.restart.life.excel.parser.phase.Phase6_ParseBuffs.ParsedBuffs
import arfies.restart.life.excel.reader.ExcelReader
import arfies.restart.life.excel.reader.story.{ConditionReader, EffectReader}
import arfies.restart.life.story.Condition.{AndCondition, RoundCondition}
import arfies.restart.life.story.{Condition, Event, Story}

import scala.collection.mutable.ListBuffer

/**
 * 阶段 5：解析技能和天赋
 *
 * Author: sinar
 * 2022/7/7 23:15
 */
object Phase7_ParseEvents extends ParserPhase[ParsedBuffs, Story] {
  /**
   * 添加事件到解析结果
   *
   * @param target 上次解析结果
   * @param events 解析后的事件
   * @return 完整的故事对象
   */
  private def appendEvents(target: ParsedBuffs, events: Map[String, Event]): Story =
    Story(
      config = target.config, attrs = target.attrs, skills = target.skills,
      talents = target.talents, buffs = target.buffs, events = events,
      achievements = target.achievements, endings = target.endings
    )

  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  override def apply(data: ExcelReader.ExcelData, from: ParsedBuffs): Either[Seq[String], Story] = {
    val events = data.events
    val errors = ListBuffer[String]()
    val success = ListBuffer[Event]()

    events.foreach {
      case EventIR(name, msg, weight, afterRound, beforeRound, rawIncludeCond, rawEffect, path, nextEventScope, rowCount) =>
        val effectOpt = rawEffect.map(EffectReader.read(_, from.keywords))
        val includeCondOpt = rawIncludeCond.map(ConditionReader.read(_, Condition.Timing.BEFORE_ROUND, from.keywords))
        (for {
          effect <- if (effectOpt.isEmpty) Right(None) else effectOpt.get.map(Some(_))
          includeCond <- if (includeCondOpt.isEmpty) Right(None) else includeCondOpt.get.map(Some(_))
          combineCond <- appendRoundCondition(afterRound, beforeRound, includeCond, rowCount)
          event = Event(name, msg, weight, combineCond, effect,
            path.map(_.split("[,，]").toSeq).getOrElse(Seq.empty),
            nextEventScope.map(_.split("[,，]").toSeq).getOrElse(Seq.empty))
        } yield event) match {
          case Left(error) => errors += s"[事件列表 第${rowCount}行] $error"
          case Right(event) => success += event
        }
    }

    if (errors.nonEmpty) Left(errors.toSeq)
    else Right(appendEvents(from, success.map(it => it.name -> it).toMap))
  }

  /**
   * 追加回合条件
   *
   * @param afterRound   在 N 回合后
   * @param beforeRound  在 N 回合前
   * @param existingCond 已存在的条件
   * @param rowCount     当前行数
   * @return 追加后的条件
   */
  private def appendRoundCondition(afterRound: Option[Int], beforeRound: Option[Int], existingCond: Option[Condition], rowCount: Int): Either[String, Option[Condition]] = {
    (afterRound, beforeRound) match {
      case (Some(after), Some(before)) =>
        if (after < 0) return Left(s"[事件列表 第${rowCount}行] 年龄下限应为非负数")
        else if (before < 0) return Left(s"[事件列表 第${rowCount}行] 年龄上限应为非负数")
      case (None, Some(round)) =>
        if (round < 0) return Left(s"[事件列表 第${rowCount}行] 年龄上限应为非负数")
      case (Some(round), None) =>
        if (round < 0) return Left(s"[事件列表 第${rowCount}行] 年龄下限应为非负数")
      case (None, None) => return Right(existingCond)
    }
    val roundCondition = RoundCondition(Condition.Timing.BEFORE_ROUND, afterRound, beforeRound)
    Right(Some(existingCond.map(existing => AndCondition(existing.timing, Seq(roundCondition, existing))).getOrElse(roundCondition)))
  }
}
