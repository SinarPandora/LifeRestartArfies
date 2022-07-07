package arfies.restart.life.excel.parser.phase

import arfies.restart.life.achievement.Achievement
import arfies.restart.life.excel.ir.{BuffIR, EndingIR, EventIR, SkillIR}
import arfies.restart.life.excel.parser.phase.Phase2_ExtractKeywords.ConfigAndKeywords
import arfies.restart.life.excel.parser.phase.Phase3_ParseAchievements.ParsedAchievements
import arfies.restart.life.excel.reader.ExcelReader
import arfies.restart.life.player.Player.Attr
import arfies.restart.life.story.Story.StoryConfig

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
    rawEndings: Map[String, EndingIR]
  )

  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  override def apply(data: ExcelReader.ExcelData, from: ConfigAndKeywords): Either[Seq[String], ParsedAchievements] = ???
}
