package arfies.restart.life.excel.parser.phase

import arfies.restart.life.excel.parser.phase.Phase6_ParseBuffs.ParsedBuffs
import arfies.restart.life.excel.reader.ExcelReader
import arfies.restart.life.story.Story

/**
 * 阶段 5：解析技能和天赋
 *
 * Author: sinar
 * 2022/7/7 23:15
 */
object Phase7_ParseEvents extends ParserPhase[ParsedBuffs, Story] {
  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  override def apply(data: ExcelReader.ExcelData, from: ParsedBuffs): Either[Seq[String], Story] = ???
}
