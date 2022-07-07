package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.ir.EndingIR
import arfies.restart.life.excel.util.XLSXUtil
import org.apache.poi.ss.usermodel.Row

import scala.util.Try

/**
 * 结局页
 *
 * Author: sinar
 * 2022/7/6 22:31
 */
object Sheet6_Ending extends SheetReader[EndingIR]("[7]结局") {
  /**
   * 读取行
   *
   * @param row 行对象
   * @return 读取结果
   */
  override def readRow(row: Row): Option[Either[String, EndingIR]] = {
    // 跳过所有没有名字的行
    XLSXUtil.getCellValueAsStr(row.getCell(0)).map { idStr =>
      for {
        id <- Try(idStr.toInt).toEither.left.map(_ => "结局编号必须是整数")
        name <- XLSXUtil.getCellValueOrErr(row.getCell(1), "需提供结局名")
        timing <- Right(XLSXUtil.getCellValueAsStr(row.getCell(2)))
        condition <- Right(XLSXUtil.getCellValueAsStr(row.getCell(3)))
        achievement <- Right(XLSXUtil.getCellValueAsStr(row.getCell(4)))
        msg <- XLSXUtil.getCellValueOrErr(row.getCell(5), "需提供结局提示语")
      } yield EndingIR(id, name, timing, condition, achievement, msg, row.getRowNum + 1)
    }
  }
}
