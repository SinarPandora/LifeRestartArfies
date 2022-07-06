package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.ir.StoryConfigIR
import arfies.restart.life.excel.util.XLSXUtil
import org.apache.poi.ss.usermodel.Row

/**
 * 配置页
 *
 * Author: sinar
 * 2022/7/6 22:25
 */
object Sheet0_Config extends SheetReader[StoryConfigIR]("[1]基本配置", false) {
  /**
   * 读取行
   *
   * @param row 行对象
   * @return 读取结果
   */
  @inline override def readRow(row: Row): Option[Either[String, StoryConfigIR]] = Some {
    XLSXUtil.getCellValueAsStr(row.getCell(0)) match {
      case Some(name) => Right(StoryConfigIR(name, XLSXUtil.getCellValueAsStr(row.getCell(1))))
      case None => Left("检测到没有名字的配置项")
    }
  }
}
