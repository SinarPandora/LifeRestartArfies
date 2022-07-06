package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.ir.EventIR
import org.apache.poi.ss.usermodel.Row

import scala.collection.mutable.ListBuffer

/**
 * 事件页
 *
 * Author: sinar
 * 2022/7/6 22:28
 */
object Sheet4_Event extends SheetReader[EventIR]("[5]事件列表") {
  /**
   * 行扫描
   *
   * @param row       行对象
   * @param resultSet 结果集
   * @param errors    错误
   */
  override def rowScan(row: Row, resultSet: ListBuffer[EventIR], errors: ListBuffer[String]): Unit = ???
}
