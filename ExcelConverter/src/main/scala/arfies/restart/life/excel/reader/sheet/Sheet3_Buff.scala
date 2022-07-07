package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.ir.BuffIR
import arfies.restart.life.excel.util.XLSXUtil
import org.apache.poi.ss.usermodel.Row

import scala.util.Try

/**
 * Buff页
 *
 * Author: sinar
 * 2022/7/6 22:27
 */
object Sheet3_Buff extends SheetReader[BuffIR]("[4]Buff") {
  /**
   * 读取行
   *
   * @param row 行对象
   * @return 读取结果
   */
  @inline override def readRow(row: Row): Option[Either[String, BuffIR]] = {
    // 跳过所有没有名字的行
    // TODO v2 解析属性变化
    XLSXUtil.getCellValueAsStr(row.getCell(0)).map { name =>
      for {
        msg <- Right(XLSXUtil.getCellValueOrElse(row.getCell(1), name))
        effects <- Right(XLSXUtil.getCellValueAsStr(row.getCell(5)))
        onAddEffects <- Right(XLSXUtil.getCellValueAsStr(row.getCell(6)))
        onLeaveEffects <- Right(XLSXUtil.getCellValueAsStr(row.getCell(7)))
        timing <- XLSXUtil.getCellValueOrErr(row.getCell(2), "需设置 Buff 的触发时机")
        condition <- Right(XLSXUtil.getCellValueAsStr(row.getCell(4)))
        roundCount <- Try(XLSXUtil.getCellValueAsStr(row.getCell(8)).map(_.toInt))
          .toEither.left.map(_ => "初始回合数应设置为整数（或不设置）")
        doubleApplicable <- Right(XLSXUtil.getCellValueOrElse(row.getCell(9), "是").trim == "是")
      } yield BuffIR(name, msg, effects, onAddEffects, onLeaveEffects, timing, condition, roundCount, doubleApplicable, row.getRowNum + 1)
    }
  }
}
