package arfies.restart.life.excel.reader.sheet

import arfies.restart.life.excel.ir.SkillIR
import arfies.restart.life.excel.util.XLSXUtil
import org.apache.poi.ss.usermodel.Row

/**
 * 技能页
 *
 * Author: sinar
 * 2022/7/6 22:27
 */
object Sheet2_Skill extends SheetReader[SkillIR]("[3]技能天赋") {
  /**
   * 读取行
   *
   * @param row 行对象
   * @return 读取结果
   */
  @inline override def readRow(row: Row): Option[Either[String, SkillIR]] = {
    // 跳过所有没有名字的行
    // TODO v2 解析属性变化
    XLSXUtil.getCellValueAsStr(row.getCell(0)).map { name =>
      for {
        msg <- Right(XLSXUtil.getCellValueOrElse(row.getCell(1), "无介绍"))
        timing <- XLSXUtil.getCellValueOrErr(row.getCell(2), "需设置技能判定时机")
        condition <- Right(XLSXUtil.getCellValueAsStr(row.getCell(4)))
        effect <- Right(XLSXUtil.getCellValueAsStr(row.getCell(5)))
        isTalent <- Right(XLSXUtil.getCellValueOrElse(row.getCell(6), "否").trim == "是")
      } yield SkillIR(name, msg, effect, timing, condition, isTalent, row.getRowNum + 1)
    }
  }
}
