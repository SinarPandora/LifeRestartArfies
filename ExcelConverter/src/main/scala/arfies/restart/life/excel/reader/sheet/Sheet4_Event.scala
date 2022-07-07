package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.ir.EventIR
import arfies.restart.life.excel.util.XLSXUtil
import org.apache.poi.ss.usermodel.Row

import scala.util.Try

/**
 * 事件页
 *
 * Author: sinar
 * 2022/7/6 22:28
 */
object Sheet4_Event extends SheetReader[EventIR]("[5]事件列表") {
  /**
   * 读取行
   *
   * @param row 行对象
   * @return 读取结果
   */
  override def readRow(row: Row): Option[Either[String, EventIR]] = {
    // 跳过所有没有名字的行
    XLSXUtil.getCellValueAsStr(row.getCell(0)).map { name =>
      for {
        path <- Right(XLSXUtil.getCellValueAsStr(row.getCell(1)))
        msg <- XLSXUtil.getCellValueOrErr(row.getCell(2), "需设置事件提示语")
        effects <- Right(XLSXUtil.getCellValueAsStr(row.getCell(3)))
        weight <- Try(XLSXUtil.getCellValueAsStr(row.getCell(4)).map(_.toInt).getOrElse(10)) // TODO 配置化
          .toEither.left.map(_ => "事件权重需设置为整数（或不设置）")
        nextEventScope <- Right(XLSXUtil.getCellValueAsStr(row.getCell(5)))
        afterRound <- Try(XLSXUtil.getCellValueAsStr(row.getCell(6)).map(_.toInt))
          .toEither.left.map(_ => "年龄下限需设置为整数（或不设置）") // TODO i18n（v3）
        beforeRound <- Try(XLSXUtil.getCellValueAsStr(row.getCell(7)).map(_.toInt))
          .toEither.left.map(_ => "年龄上限需设置为整数（或不设置）") // TODO i18n（v3）
        includeCond <- Right(XLSXUtil.getCellValueAsStr(row.getCell(8)))
      } yield EventIR(name, msg, weight, afterRound, beforeRound, includeCond, effects, path, nextEventScope, row.getRowNum + 1)
    }
  }
}
