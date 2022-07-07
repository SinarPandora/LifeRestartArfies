package arfies.restart.life.excel.parser.phase

import arfies.restart.life.excel.ir.*
import arfies.restart.life.excel.parser.phase.Phase2_ExtractKeywords.ConfigAndKeywords
import arfies.restart.life.excel.reader.ExcelReader
import arfies.restart.life.player.Player.Attr
import arfies.restart.life.story.Story.StoryConfig

/**
 * 阶段 2：提取关键词
 *
 * Author: sinar
 * 2022/7/7 22:40
 */
object Phase2_ExtractKeywords extends ParserPhase[StoryConfig, ConfigAndKeywords] {
  case class ConfigAndKeywords
  (
    config: StoryConfig,
    attrs: Map[String, Attr],
    rawSkills: Map[String, SkillIR],
    rawTalents: Map[String, SkillIR],
    rawBuffs: Map[String, BuffIR],
    rawEvents: Map[String, EventIR],
    rawAchievements: Map[String, AchievementIR],
    rawEndings: Map[String, EndingIR],
    keywords: Keywords
  )

  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  //noinspection DuplicatedCode
  override def apply(data: ExcelReader.ExcelData, from: StoryConfig): Either[Seq[String], ConfigAndKeywords] = data match {
    case ExcelReader.ExcelData(_, attrSeq, skillOrTalent, buffs, events, achievements, endings) =>
      val attrs = attrSeq.map(it => it.name -> it).toMap
      val rawSkills = skillOrTalent.filterNot(_.isTalent).map(it => it.name -> it).toMap
      val rawTalents = skillOrTalent.filter(_.isTalent).map(it => it.name -> it).toMap
      val rawBuffs = buffs.map(it => it.name -> it).toMap
      val rawEvents = events.map(it => it.name -> it).toMap
      val rawAchievements = achievements.map(it => it.name -> it).toMap
      val rawEndings = endings.map(it => it.name -> it).toMap
      Right {
        ConfigAndKeywords(
          config = from,
          attrs = attrs,
          rawSkills = rawSkills,
          rawTalents = rawTalents,
          rawBuffs = rawBuffs,
          rawEvents = rawEvents,
          rawAchievements = rawAchievements,
          rawEndings = rawEndings,
          keywords = Keywords(
            attrs = attrs.keySet,
            skills = rawSkills.keySet,
            talents = rawTalents.keySet,
            buffs = rawBuffs.keySet,
            events = rawEvents.keySet,
            achievements = rawAchievements.keySet,
            endings = rawEndings.keySet,
          )
        )
      }
  }
}
