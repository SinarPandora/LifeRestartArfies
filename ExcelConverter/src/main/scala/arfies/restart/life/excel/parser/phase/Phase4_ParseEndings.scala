package arfies.restart.life.excel.parser.phase

import arfies.restart.life.excel.ir.EndingIR
import arfies.restart.life.excel.parser.phase.PhaseResults.{ParsedAchievements, ParsedEndings}
import arfies.restart.life.excel.reader.ExcelReader
import arfies.restart.life.excel.reader.story.{ConditionReader, TimingReader}
import arfies.restart.life.story.Ending

import scala.collection.mutable.ListBuffer

/**
 * 阶段 4：解析结局
 *
 * Author: sinar
 * 2022/7/7 23:15
 */
object Phase4_ParseEndings extends ParserPhase[ParsedAchievements, ParsedEndings] {
  /**
   * 添加结局到解析结果
   *
   * @param target  上次解析结果
   * @param endings 解析后的结局
   * @return 带有结局的解析结果
   */
  private def appendEndings(target: ParsedAchievements, endings: Map[String, Ending]): ParsedEndings =
    ParsedEndings(
      config = target.config, attrs = target.attrs, rawSkills = target.rawSkills,
      rawTalents = target.rawTalents, rawBuffs = target.rawBuffs, rawEvents = target.rawEvents,
      achievements = target.achievements, endings = endings, keywords = target.keywords,
    )

  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  override def apply(data: ExcelReader.ExcelData, from: ParsedAchievements): Either[Seq[String], ParsedEndings] = {
    val endings = data.endings
    val errors = ListBuffer[String]()
    val success = ListBuffer[Ending]()
    endings.foreach {
      case EndingIR(id, name, timing, condition, achievement, msg, rowCount) =>
        if (condition.isDefined) {
          if (timing.isEmpty) errors += s"[结局 第${rowCount}行] 缺少条件触发的时机"
          else {
            (for {
              timing <- TimingReader.read(timing.get)
              condition <- ConditionReader.read(condition.get, timing, from.keywords)
              ending = Ending(id, name, Some(condition), achievement, msg)
            } yield ending) match {
              case Left(error) => errors += s"[结局 第${rowCount}行] $error"
              case Right(ending) => success += ending
            }
          }
        } else {
          if (timing.nonEmpty) errors += s"[结局 第${rowCount}行] 缺少该时机下的条件"
          else success += Ending(id, name, None, achievement, msg)
        }
    }
    if (errors.nonEmpty) Left(errors.toSeq)
    else Right(appendEndings(from, success.map(it => it.name -> it).toMap))
  }
}
