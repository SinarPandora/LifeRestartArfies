package arfies.restart.life.excel.parser.phase

import arfies.restart.life.achievement.Achievement
import arfies.restart.life.excel.ir.{AchievementIR, BuffIR, EndingIR, EventIR, SkillIR}
import arfies.restart.life.excel.parser.phase.Phase2_ExtractKeywords.ConfigAndKeywords
import arfies.restart.life.excel.parser.phase.Phase3_ParseAchievements.ParsedAchievements
import arfies.restart.life.excel.reader.ExcelReader
import arfies.restart.life.excel.reader.story.{ConditionReader, TimingReader}
import arfies.restart.life.player.Player.Attr
import arfies.restart.life.story.Story.StoryConfig

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * 阶段 3：解析成就
 *
 * Author: sinar
 * 2022/7/7 23:10
 */
object Phase3_ParseAchievements extends ParserPhase[ConfigAndKeywords, ParsedAchievements] {
  case class ParsedAchievements
  (
    config: StoryConfig,
    attrs: Map[String, Attr],
    rawSkills: Map[String, SkillIR],
    rawTalents: Map[String, SkillIR],
    rawBuffs: Map[String, BuffIR],
    rawEvents: Map[String, EventIR],
    achievements: Map[String, Achievement],
    rawEndings: Map[String, EndingIR],
    keywords: Keywords
  )

  /**
   * 添加成就到解析结果
   *
   * @param target       上次解析结果
   * @param achievements 解析后的成就
   * @return 带有成就的解析结果
   */
  private def appendAchievements(target: ConfigAndKeywords, achievements: Map[String, Achievement]): ParsedAchievements =
    ParsedAchievements(
      config = target.config, attrs = target.attrs, rawSkills = target.rawSkills,
      rawTalents = target.rawTalents, rawBuffs = target.rawBuffs, rawEvents = target.rawEvents,
      achievements = achievements, rawEndings = target.rawEndings, keywords = target.keywords,
    )

  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  override def apply(data: ExcelReader.ExcelData, from: ConfigAndKeywords): Either[Seq[String], ParsedAchievements] = {
    val achievements = data.achievements
    val errors = ListBuffer[String]()
    val success = ListBuffer[Achievement]()
    achievements.foreach {
      case AchievementIR(name, msg, timing, condition, rowCount) =>
        if (condition.isDefined) {
          if (timing.isEmpty) errors += s"[成就 第${rowCount}行] 缺少条件触发的时机"
          else {
            (for {
              timing <- TimingReader.read(timing.get)
              condition <- ConditionReader.read(condition.get, timing, from.keywords)
              achievement = Achievement(name, msg, Some(condition))
            } yield achievement) match {
              case Left(error) => errors += s"[成就 第${rowCount}行] $error"
              case Right(achievement) => success += achievement
            }
          }
        } else {
          if (timing.nonEmpty) errors += s"[成就 第${rowCount}行] 缺少该时机下的条件"
          else success += Achievement(name, msg, None)
        }
    }
    if (errors.nonEmpty) Left(errors.toSeq)
    else Right(appendAchievements(from, success.map(it => it.name -> it).toMap))
  }
}
