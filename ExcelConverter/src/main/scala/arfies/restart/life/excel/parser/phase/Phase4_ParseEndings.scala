package arfies.restart.life.excel.parser.phase

import arfies.restart.life.achievement.Achievement
import arfies.restart.life.excel.ir.{BuffIR, EventIR, SkillIR}
import arfies.restart.life.excel.parser.phase.Phase3_ParseAchievements.ParsedAchievements
import arfies.restart.life.excel.parser.phase.Phase4_ParseEndings.ParsedEndings
import arfies.restart.life.excel.reader.ExcelReader
import arfies.restart.life.player.Player.Attr
import arfies.restart.life.story.Ending
import arfies.restart.life.story.Story.StoryConfig

/**
 * 阶段 4：解析结局
 *
 * Author: sinar
 * 2022/7/7 23:15
 */
object Phase4_ParseEndings extends ParserPhase[ParsedAchievements, ParsedEndings] {
  case class ParsedEndings
  (
    config: StoryConfig,
    attrs: Map[String, Attr],
    rawSkills: Map[String, SkillIR],
    rawTalents: Map[String, SkillIR],
    rawBuffs: Map[String, BuffIR],
    rawEvents: Map[String, EventIR],
    achievements: Map[String, Achievement],
    endings: Map[String, Ending],
    keywords: Keywords
  )

  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  override def apply(data: ExcelReader.ExcelData, from: ParsedAchievements): Either[Seq[String], ParsedEndings] = ???
}
