package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.util.ProgressBarUtil
import me.tongfei.progressbar.ProgressBar
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet

import scala.collection.mutable.ListBuffer

/**
 * 工作表读取器
 *
 * Author: sinar
 * 2022/7/6 22:23
 */
abstract class SheetReader[T](name: String) {
  /**
   * 读取工作表
   *
   * @param sheet 工作表
   * @return 数据及出现的错误
   */
  final def read(sheet: XSSFSheet): Either[Seq[String], Seq[T]] = {
    val errors = ListBuffer[String]()
    val resultSet = ListBuffer[T]()
    ProgressBar.wrap(sheet.rowIterator(), ProgressBarUtil.builder(s"读取工作表：$name"))
      .forEachRemaining(rowScan(_, resultSet, errors))
    if (errors.nonEmpty) Left(errors.toSeq) else Right(resultSet.toSeq)
  }

  /**
   * 行扫描
   *
   * @param row       行对象
   * @param resultSet 结果集
   * @param errors    错误
   */
  @inline def rowScan(row: Row, resultSet: ListBuffer[T], errors: ListBuffer[String]): Unit
}
