package arfies.restart.life.excel.parser.phase

import arfies.restart.life.achievement.Achievement
import arfies.restart.life.excel.ir.{BuffIR, EventIR}
import arfies.restart.life.excel.parser.phase.Phase4_ParseEndings.ParsedEndings
import arfies.restart.life.excel.parser.phase.Phase5_ParseSkillAndTalents.ParsedSkillsAndTalents
import arfies.restart.life.excel.reader.ExcelReader
import arfies.restart.life.player.Player.{Attr, Skill}
import arfies.restart.life.story.Ending
import arfies.restart.life.story.Story.StoryConfig

/**
 * 阶段 5：解析技能和天赋
 *
 * Author: sinar
 * 2022/7/7 23:15
 */
object Phase5_ParseSkillAndTalents extends ParserPhase[ParsedEndings, ParsedSkillsAndTalents] {
  case class ParsedSkillsAndTalents
  (
    config: StoryConfig,
    attrs: Map[String, Attr],
    skills: Map[String, Skill],
    talents: Map[String, Skill],
    rawBuffs: Map[String, BuffIR],
    rawEvents: Map[String, EventIR],
    achievements: Map[String, Achievement],
    endings: Map[String, Ending]
  )

  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  override def apply(data: ExcelReader.ExcelData, from: ParsedEndings): Either[Seq[String], ParsedSkillsAndTalents] = ???
}
