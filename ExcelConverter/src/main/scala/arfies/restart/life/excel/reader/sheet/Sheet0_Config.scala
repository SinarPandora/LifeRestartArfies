package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.ir.StoryConfigIR
import org.apache.poi.ss.usermodel.Row

import scala.collection.mutable.ListBuffer

/**
 * 配置页
 *
 * Author: sinar
 * 2022/7/6 22:25
 */
object Sheet0_Config extends SheetReader[StoryConfigIR]("[1]基本配置") {
  /**
   * 行扫描
   *
   * @param row       行对象
   * @param resultSet 结果集
   * @param errors    错误
   */
  override def rowScan(row: Row, resultSet: ListBuffer[StoryConfigIR], errors: ListBuffer[String]): Unit = ???
}
