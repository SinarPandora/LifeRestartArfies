package arfies.restart.life.excel.ir

/**
 * 有名字的
 *
 * Author: sinar
 * 2022/7/7 23:51
 */
trait Named {
  /**
   * 取出名字
   *
   * @return 名字
   */
  def name: String
}

object Named {
  /**
   * 找出所有重名元素
   *
   * @param items 元素序列
   * @return 重名元素
   */
  def findDuplicate(items: Seq[Named]): Iterable[String] = items.groupBy(_.name).filter(_._2.lengthIs > 1).keys
  /**
   * 将序列转成以名字为键的字典
   *
   * @param items 元素序列
   * @tparam T 实际类型（Named 的子类）
   * @return 字典对象
   */
  def toMap[T <: Named](items: Seq[T]): Map[String, T] = items.map(it => it.name -> it).toMap
}
