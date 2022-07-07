package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.ir.AchievementIR
import arfies.restart.life.excel.util.XLSXUtil
import org.apache.poi.ss.usermodel.Row

/**
 * 成就页
 *
 * Author: sinar
 * 2022/7/6 22:30
 */
object Sheet5_Achievement extends SheetReader[AchievementIR]("[6]成就") {
  /**
   * 读取行
   *
   * @param row 行对象
   * @return 读取结果
   */
  override def readRow(row: Row): Option[Either[String, AchievementIR]] = {
    // 跳过所有没有名字的行
    XLSXUtil.getCellValueAsStr(row.getCell(0)).map { name =>
      Right(AchievementIR(
        name = name,
        msg = XLSXUtil.getCellValueOrElse(row.getCell(1), "你获得了一个成就"), // TODO 配置化
        timing = XLSXUtil.getCellValueAsStr(row.getCell(2)),
        condition = XLSXUtil.getCellValueAsStr(row.getCell(3)),
        rowCount = row.getRowNum + 1
      ))
    }
  }
}
