package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.ir.SkillIR
import org.apache.poi.ss.usermodel.Row

import scala.collection.mutable.ListBuffer

/**
 * 技能页
 *
 * Author: sinar
 * 2022/7/6 22:27
 */
object Sheet2_Skill extends SheetReader[SkillIR]("[3]技能天赋") {
  /**
   * 行扫描
   *
   * @param row       行对象
   * @param resultSet 结果集
   * @param errors    错误
   */
  override def rowScan(row: Row, resultSet: ListBuffer[SkillIR], errors: ListBuffer[String]): Unit = ???
}
