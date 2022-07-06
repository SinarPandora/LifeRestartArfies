package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.ir.EndingIR
import org.apache.poi.ss.usermodel.Row

import scala.collection.mutable.ListBuffer

/**
 * 结局页
 *
 * Author: sinar
 * 2022/7/6 22:31
 */
object Sheet6_Ending extends SheetReader[EndingIR]("[7]结局") {
  /**
   * 行扫描
   *
   * @param row       行对象
   * @param resultSet 结果集
   * @param errors    错误
   */
  override def rowScan(row: Row, resultSet: ListBuffer[EndingIR], errors: ListBuffer[String]): Unit = ???
}
