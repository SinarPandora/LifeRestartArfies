package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.ir.AchievementIR
import org.apache.poi.ss.usermodel.Row

import scala.collection.mutable.ListBuffer

/**
 * 成就页
 *
 * Author: sinar
 * 2022/7/6 22:30
 */
object Sheet5_Achievement extends SheetReader[AchievementIR]("[6]成就") {
  /**
   * 行扫描
   *
   * @param row       行对象
   * @param resultSet 结果集
   * @param errors    错误
   */
  override def rowScan(row: Row, resultSet: ListBuffer[AchievementIR], errors: ListBuffer[String]): Unit = ???
}
