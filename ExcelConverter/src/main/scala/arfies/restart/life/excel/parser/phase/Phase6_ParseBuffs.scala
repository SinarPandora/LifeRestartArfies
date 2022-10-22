package arfies.restart.life.excel.parser.phase

import arfies.restart.life.achievement.Achievement
import arfies.restart.life.excel.ir.{BuffIR, EventIR}
import arfies.restart.life.excel.parser.phase.Phase5_ParseSkillAndTalents.ParsedSkillsAndTalents
import arfies.restart.life.excel.parser.phase.Phase6_ParseBuffs.ParsedBuffs
import arfies.restart.life.excel.reader.ExcelReader
import arfies.restart.life.excel.reader.story.{ConditionReader, EffectReader, TimingReader}
import arfies.restart.life.player.Player.{Attr, Buff, Skill}
import arfies.restart.life.story.Condition.ImmediatelyActivate
import arfies.restart.life.story.{Effect, Ending}
import arfies.restart.life.story.Story.StoryConfig

import scala.collection.mutable.ListBuffer

/**
 * 阶段 5：解析技能和天赋
 *
 * Author: sinar
 * 2022/7/7 23:15
 */
object Phase6_ParseBuffs extends ParserPhase[ParsedSkillsAndTalents, ParsedBuffs] {
  case class ParsedBuffs
  (
    config: StoryConfig,
    attrs: Map[String, Attr],
    skills: Map[String, Skill],
    talents: Map[String, Skill],
    buffs: Map[String, Buff],
    rawEvents: Map[String, EventIR],
    achievements: Map[String, Achievement],
    endings: Map[String, Ending],
    keywords: Keywords
  )

  /**
   * 添加 Buff 到解析结果
   *
   * @param target 上次解析结果
   * @param buffs  解析后 Buff
   * @return 带有 Buff 的解析结果
   */
  private def appendBuffs(target: ParsedSkillsAndTalents, buffs: Map[String, Buff]): ParsedBuffs =
    ParsedBuffs(
      config = target.config, attrs = target.attrs, skills = target.skills,
      talents = target.talents, buffs = buffs, rawEvents = target.rawEvents,
      achievements = target.achievements, endings = target.endings, keywords = target.keywords,
    )

  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  override def apply(data: ExcelReader.ExcelData, from: ParsedSkillsAndTalents): Either[Seq[String], ParsedBuffs] = {
    val buffs = data.buffs
    val errors = ListBuffer[String]()
    val success = ListBuffer[Buff]()

    buffs.foreach {
      case BuffIR(name, msg, rawEffect, rawOnAddEffect, rawOnLeaveEffect, rawTiming, rawCondition, roundCount, doubleApplicable, rowCount) =>
        val effectOpt = rawEffect.map(EffectReader.read(_, from.keywords))
        val onAddEffectOpt = rawOnAddEffect.map(EffectReader.read(_, from.keywords))
        val onLeaveEffectOpt = rawOnLeaveEffect.map(EffectReader.read(_, from.keywords))
        if (effectOpt.isEmpty && onAddEffectOpt.isEmpty && onLeaveEffectOpt.isEmpty)
          errors += s"[Buff 第${rowCount}行] Buff 应至少包含任何一种效果"
        else (for {
          timing <- TimingReader.read(rawTiming)
          effect <- if (effectOpt.isEmpty) Right(None) else effectOpt.get.map(Some(_))
          onAddEffect <- if (onAddEffectOpt.isEmpty) Right(None) else onAddEffectOpt.get.map(Some(_))
          onLeaveEffect <- if (onLeaveEffectOpt.isEmpty) Right(None) else onLeaveEffectOpt.get.map(Some(_))
          activeOn <- rawCondition.map(ConditionReader.read(_, timing, from.keywords)).getOrElse(Right(ImmediatelyActivate(timing)))
          buff = Buff(name, msg, effect, onAddEffect, onLeaveEffect, activeOn, roundCount, doubleApplicable)
        } yield buff) match {
          case Left(error) => errors += s"[Buff 第${rowCount}行] $error"
          case Right(buff) => success += buff
        }
    }

    if (errors.nonEmpty) Left(errors.toSeq)
    else Right(appendBuffs(from, success.map(it => it.name -> it).toMap))
  }
}
