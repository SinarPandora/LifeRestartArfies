package arfies.restart.life.excel.util

/**
 * XLSX Util
 *
 * Author: sinar
 * 2022/7/4 22:03
 */
object XLSXUtil {
  /**
   * 判断格子是否为空
   *
   * @param value 格子值
   * @return 是否为空
   */
  def isCellEmpty(value: String): Boolean = value != null && !value.isBlank

  /**
   * 包装格子值为 Option 对象
   *
   * @param value 格子值
   * @return Option 对象
   */
  def wrap(value: String): Option[String] = if (value != null && !value.isBlank) None else Some(value.trim)
}
