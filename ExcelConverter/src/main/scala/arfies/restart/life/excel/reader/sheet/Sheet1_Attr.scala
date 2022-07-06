package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.player.Player.Attr
import org.apache.poi.ss.usermodel.Row

import scala.collection.mutable.ListBuffer

/**
 * 角色属性页
 *
 * Author: sinar
 * 2022/7/6 22:25
 */
object Sheet1_Attr extends SheetReader[Attr]("[2]角色数值属性") {
  /**
   * 行扫描
   *
   * @param row       行对象
   * @param resultSet 结果集
   * @param errors    错误
   */
  override def rowScan(row: Row, resultSet: ListBuffer[Attr], errors: ListBuffer[String]): Unit = ???
}
