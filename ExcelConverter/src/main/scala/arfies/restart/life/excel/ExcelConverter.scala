package arfies.restart.life.excel

import arfies.restart.life.excel.parser.ExcelParser
import arfies.restart.life.excel.reader.ExcelReader
import arfies.restart.life.excel.writer.StorySerializer

import java.io.{BufferedWriter, File, FileWriter}
import scala.util.Using
import scala.util.chaining.*

/**
 * Excel 解析器
 *
 * Author: sinar
 * 2022/7/4 21:21
 */
object ExcelConverter extends App {
  println(
    """
      |    __    ________________   ____  ____________________    ____  ______   __   __   __
      |   / /   /  _/ ____/ ____/  / __ \/ ____/ ___/_  __/   |  / __ \/_  __/  / /  / /  / /
      |  / /    / // /_  / __/    / /_/ / __/  \__ \ / / / /| | / /_/ / / /    / /  / /  / /
      | / /____/ // __/ / /___   / _, _/ /___ ___/ // / / ___ |/ _, _/ / /    /_/  /_/  /_/
      |/_____/___/_/   /_____/  /_/ |_/_____//____//_/ /_/  |_/_/ |_| /_/    (_)  (_)  (_)
      |
      |欢迎使用人生重来器 Excel 解析工具""".stripMargin)
  if (args.lengthIs != 2) {
    println(
      """程序接收两个参数：
        |参数 1：源 Excel 文件路径
        |参数 2：输出 JSON 文件路径
        |指令示例：
        |java -jar excel-cvt.jar foo.xlsx story.json""".stripMargin)
    sys.exit(1)
  } else {
    Using(new BufferedWriter(new FileWriter(new File(args(1))))) { out =>
      args(0)
        .pipe(ExcelReader.read)
        .pipe(ExcelParser.parse)
        .pipe(StorySerializer.serialize(out))
        .pipe(_.flush())
    }
  }
}
