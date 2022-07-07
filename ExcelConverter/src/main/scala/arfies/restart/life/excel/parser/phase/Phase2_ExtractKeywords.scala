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
    rawEndings: Map[String, EndingIR]
  )

  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  override def apply(data: ExcelReader.ExcelData, from: StoryConfig): Either[Seq[String], ConfigAndKeywords] = data match {
    case ExcelReader.ExcelData(_, attrs, skillOrTalent, buffs, events, achievements, endings) =>
      Right {
        ConfigAndKeywords(
          config = from,
          attrs = attrs.map(it => it.name -> it).toMap,
          rawSkills = skillOrTalent.filterNot(_.isTalent).map(it => it.name -> it).toMap,
          rawTalents = skillOrTalent.filter(_.isTalent).map(it => it.name -> it).toMap,
          rawBuffs = buffs.map(it => it.name -> it).toMap,
          rawEvents = events.map(it => it.name -> it).toMap,
          rawAchievements = achievements.map(it => it.name -> it).toMap,
          rawEndings = endings.map(it => it.name -> it).toMap,
        )
      }
  }
}
