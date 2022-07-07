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
abstract class SheetReader[T](name: String, hasHeader: Boolean = true) {
  /**
   * 读取工作表
   *
   * @param sheet 工作表
   * @return 数据及出现的错误
   */
  final def read(sheet: XSSFSheet): Either[Seq[String], Seq[T]] = {
    val maxRowCount = if (hasHeader) sheet.getPhysicalNumberOfRows - 1 else sheet.getPhysicalNumberOfRows
    if (maxRowCount <= 0) return Left(Seq(s"工作表${name}没有数据！"))
    val rowIter = {
      val iter = sheet.rowIterator()
      if (hasHeader) iter.next() // 如果存在标题，就先跳过一行
      iter
    }
    val errors = ListBuffer[String]()
    val resultSet = ListBuffer[T]()
    ProgressBar
      .wrap(rowIter, ProgressBarUtil
        .builder(s"读取工作表：$name")
        .setInitialMax(maxRowCount))
      .forEachRemaining { row =>
        readRow(row).foreach {
          case Left(error) => errors += s"工作表${name}第${row.getRowNum + 1}行：$error"
          case Right(value) => resultSet += value
        }
      }
    if (errors.nonEmpty) Left(errors.toSeq) else Right(resultSet.toSeq)
  }

  /**
   * 读取行
   *
   * @param row 行对象
   * @return 读取结果
   */
  @inline def readRow(row: Row): Option[Either[String, T]]
}
