package arfies.restart.life.excel.parser

import arfies.restart.life.excel.parser.phase.*
import arfies.restart.life.excel.reader.ExcelReader.ExcelData
import arfies.restart.life.story.Story

/**
 * Excel 解析器
 *
 * Author: sinar
 * 2022/7/4 21:23
 */
object ExcelParser {
  /**
   * 数据解析
   *
   * @param data Excel 数据
   * @return 故事对象
   */
  def parse(data: ExcelData): Either[Seq[String], Story] = {
    for {
      config <- Phase1_ExtractConfig(data)
      configAndKeywords <- Phase2_ExtractKeywords(data, config)
      parsedAchievements <- Phase3_ParseAchievements(data, configAndKeywords)
      parsedEndings <- Phase4_ParseEndings(data, parsedAchievements)
      parsedSkillsAndTalents <- Phase5_ParseSkillAndTalents(data, parsedEndings)
      parsedBuffs <- Phase6_ParseBuffs(data, parsedSkillsAndTalents)
      story <- Phase7_ParseEvents(data, parsedBuffs)
    } yield story
  }
}
