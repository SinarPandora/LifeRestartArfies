package arfies.restart.life.excel.parser.phase

import arfies.restart.life.excel.reader.ExcelReader.ExcelData

/**
 * 解析阶段
 *
 * Author: sinar
 * 2022/7/7 21:34
 */
trait ParserPhase[From, To] {
  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  def apply(data: ExcelData, from: From): Either[Seq[String], To]
}
