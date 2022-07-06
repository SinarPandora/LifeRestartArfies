package arfies.restart.life.excel.util

import org.apache.poi.ss.usermodel.{Cell, DataFormatter}

/**
 * XLSX Util
 *
 * Author: sinar
 * 2022/7/4 22:03
 */
object XLSXUtil {
  val formatter: DataFormatter = new DataFormatter()

  /**
   * 以 Option 的形式获取当前格子的字符串值
   *
   * @param cell 格子
   * @return 格子值
   */
  def getCellValueAsStr(cell: Cell): Option[String] = {
    val value = formatter.formatCellValue(cell)
    if (isStrValueEmpty(value)) None else Some(value.trim)
  }

  /**
   * 取格子值，取不到时使用默认值
   *
   * @param cell   格子
   * @param orElse 默认值
   * @return 格子值
   */
  def getCellValueOrElse(cell: Cell, orElse: String): String =
    getCellValueAsStr(cell).getOrElse(orElse)

  /**
   * 以 Either 的形式获取当前格子的字符串值，当取不到时使用错误信息
   *
   * @param cell         格子
   * @param errorMessage 错误信息
   * @return 格子值
   */
  def getCellValueOrErr(cell: Cell, errorMessage: String): Either[String, String] =
    getCellValueAsStr(cell) match {
      case Some(value) => Right(value)
      case None => Left(errorMessage)
    }

  /**
   * 判断字符串值是否为空
   *
   * @param value 格子值
   * @return 是否为空
   */
  def isStrValueEmpty(value: String): Boolean = value == null || value.isBlank
}
