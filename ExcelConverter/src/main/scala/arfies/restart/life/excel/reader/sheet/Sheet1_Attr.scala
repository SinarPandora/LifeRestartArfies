package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.util.XLSXUtil
import arfies.restart.life.player.Player.Attr
import org.apache.poi.ss.usermodel.Row

import scala.util.Try

/**
 * 角色属性页
 *
 * Author: sinar
 * 2022/7/6 22:25
 */
object Sheet1_Attr extends SheetReader[Attr]("[2]角色数值属性") {
  /**
   * 读取行
   *
   * @param row 行对象
   * @return 读取结果
   */
  @inline override def readRow(row: Row): Option[Either[String, Attr]] = {
    // 跳过所有没有名字的行
    XLSXUtil.getCellValueAsStr(row.getCell(0)).map { name =>
      for {
        initMax <- Try(XLSXUtil.getCellValueAsStr(row.getCell(1)).map(_.toInt))
          .toEither.left.map(_ => "最大点数应设置为整数")
        attrType <- XLSXUtil.getCellValueOrErr(row.getCell(2), "需设置属性类型")
        isCustomizable <- Right(XLSXUtil.getCellValueOrElse(row.getCell(3), "否").trim == "是")
        isPinOnHub <- Right(XLSXUtil.getCellValueOrElse(row.getCell(4), "否").trim == "是")
      } yield Attr(name, initMax, attrType, isCustomizable, isPinOnHub)
    }
  }
}
