package arfies.restart.life.excel.parser.phase

import arfies.restart.life.excel.ir.SkillIR
import arfies.restart.life.excel.parser.phase.PhaseResults.{ParsedEndings, ParsedSkillsAndTalents}
import arfies.restart.life.excel.reader.ExcelReader
import arfies.restart.life.excel.reader.story.{ConditionReader, EffectReader, TimingReader}
import arfies.restart.life.player.Player.Skill
import arfies.restart.life.story.Condition.ImmediatelyActivate

import scala.collection.mutable.ListBuffer

/**
 * 阶段 5：解析技能和天赋
 *
 * Author: sinar
 * 2022/7/7 23:15
 */
object Phase5_ParseSkillAndTalents extends ParserPhase[ParsedEndings, ParsedSkillsAndTalents] {
  /**
   * 添加技能和天赋到解析结果
   *
   * @param target  上次解析结果
   * @param skills  解析后的技能
   * @param talents 解析后的天赋
   * @return 带有天赋和技能的解析结果
   */
  private def appendSkillAndTalents(target: ParsedEndings, skills: Map[String, Skill], talents: Map[String, Skill]): ParsedSkillsAndTalents =
    ParsedSkillsAndTalents(
      config = target.config, attrs = target.attrs, skills = skills,
      talents = talents, rawBuffs = target.rawBuffs, rawEvents = target.rawEvents,
      achievements = target.achievements, endings = target.endings, keywords = target.keywords,
    )

  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  override def apply(data: ExcelReader.ExcelData, from: ParsedEndings): Either[Seq[String], ParsedSkillsAndTalents] = {
    val skillOrTalents = data.skillOrTalent
    val errors = ListBuffer[String]()
    val success = ListBuffer[Skill]()

    skillOrTalents.foreach {
      case SkillIR(name, msg, rawEffect, rawTiming, rawCondition, isTalent, rowCount) =>
        (for {
          timing <- TimingReader.read(rawTiming)
          activeOn <- rawCondition.map(ConditionReader.read(_, timing, from.keywords)).getOrElse(Right(ImmediatelyActivate(timing)))
          effect <- EffectReader.read(rawEffect, from.keywords)
          skill = Skill(name, msg, effect, activeOn, isTalent)
        } yield skill) match {
          case Left(error) => errors += s"[技能天赋 第${rowCount}行] $error"
          case Right(skill) => success += skill
        }
    }

    if (errors.nonEmpty) Left(errors.toSeq)
    else {
      val talents = success.filter(it => it.isTalent).map(it => it.name -> it).toMap
      val skills = success.filter(it => !it.isTalent).map(it => it.name -> it).toMap
      Right(appendSkillAndTalents(from, skills, talents))
    }
  }
}
