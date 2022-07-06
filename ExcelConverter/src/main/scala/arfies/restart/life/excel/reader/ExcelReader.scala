package arfies.restart.life.excel.reader

import arfies.restart.life.excel.ir.*
import arfies.restart.life.excel.reader.sheet.*
import arfies.restart.life.player.Player.Attr
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.io.FileInputStream
import scala.util.{Failure, Success, Using}

/**
 * Excel 读取器
 *
 * Author: sinar
 * 2022/7/4 21:23
 */
object ExcelReader {
  case class ExcelData
  (
    configs: Seq[StoryConfigIR],
    attrs: Seq[Attr],
    skillOrTalent: Seq[SkillIR],
    buffs: Seq[BuffIR],
    events: Seq[EventIR],
    achievements: Seq[AchievementIR],
    endings: Seq[EndingIR]
  )

  /**
   * 读取 Excel 文件
   *
   * @param filepath 文件路径
   * @return Excel 数据或错误信息
   */
  def read(filepath: String): Either[Seq[String], ExcelData] = {
    Using.Manager { use =>
      val fIn = use(new FileInputStream(filepath))
      val wb = use(new XSSFWorkbook(fIn))
      if (wb.getNumberOfSheets < 7) {
        Left(Seq("Excel 格式不正确，有效工作表少于 7 张"))
      } else {
        for {
          configs <- Sheet0_Config.read(wb.getSheetAt(0))
          attrs <- Sheet1_Attr.read(wb.getSheetAt(1))
          skillOrTalent <- Sheet2_Skill.read(wb.getSheetAt(2))
          buffs <- Sheet3_Buff.read(wb.getSheetAt(3))
          events <- Sheet4_Event.read(wb.getSheetAt(4))
          achievements <- Sheet5_Achievement.read(wb.getSheetAt(5))
          endings <- Sheet6_Ending.read(wb.getSheetAt(6))
        } yield ExcelData(configs, attrs, skillOrTalent, buffs, events, achievements, endings)
      }
    } match {
      case Failure(exception) =>
        exception.printStackTrace()
        Left(Seq("读取 Excel 时出错，请检查文件是否为可以打开的 Excel 文件"))
      case Success(value) => value
    }
  }
}
