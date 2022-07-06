package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.ir.BuffIR
import org.apache.poi.ss.usermodel.Row

import scala.collection.mutable.ListBuffer

/**
 * Buff页
 *
 * Author: sinar
 * 2022/7/6 22:27
 */
object Sheet3_Buff extends SheetReader[BuffIR]("[4]Buff") {
  /**
   * 行扫描
   *
   * @param row       行对象
   * @param resultSet 结果集
   * @param errors    错误
   */
  override def rowScan(row: Row, resultSet: ListBuffer[BuffIR], errors: ListBuffer[String]): Unit = ???
}
