package arfies.restart.life.excel.parser.phase

import arfies.restart.life.excel.reader.ExcelReader
import arfies.restart.life.story.Story.StoryConfig

import scala.collection.mutable.ListBuffer
import scala.util.chaining.*
import scala.util.{Failure, Success, Try}

/**
 * 第一阶段：提取配置
 *
 * Author: sinar
 * 2022/7/7 21:29
 */
object Phase1_ExtractConfig extends ParserPhase[Unit, StoryConfig] {
  // TODO i18n
  object Keys {
    val AVAILABLE_ATTR_POINTS: String = "默认加点总数"
    val DEFAULT_LIFE_PATH: String = "默认人生轨道"
    val START_ROUND_COUNT: String = "起始年龄"
  }

  import Keys.*


  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  override def apply(data: ExcelReader.ExcelData, from: Unit = ()): Either[Seq[String], StoryConfig] = {
    val errors = ListBuffer[String]()
    val rawConfig: Map[String, (Option[String], Int)] = data.configs
      .map(it => it.key -> (it.value -> it.rowCount)).toMap

    val config = for {
      availableAttrPoints <- extractPositiveIntConfig(AVAILABLE_ATTR_POINTS, rawConfig).pipe(extractResult(_, errors))
      defaultLifePath <- extractTextConfig(DEFAULT_LIFE_PATH, rawConfig).pipe(extractResult(_, errors))
      startRoundCount <- extractPositiveOrZeroIntConfig(DEFAULT_LIFE_PATH, rawConfig).pipe(extractResult(_, errors))
    } yield StoryConfig(availableAttrPoints, defaultLifePath, startRoundCount)

    config.map(Right.apply).getOrElse(Left(errors.toSeq))
  }

  /**
   * 处理提取配置的结果
   *
   * @param result 提取姐
   * @param errors 错误信息列表
   * @return 转换后的提取结果
   */
  private def extractResult[T](result: Either[String, T], errors: ListBuffer[String]): Option[T] =
    result match {
      case Left(value) => errors += value; None
      case Right(value) => Some(value)
    }

  /**
   * 提取文本型配置
   *
   * @param key       配置键名称
   * @param rawConfig 原始配置
   */
  private def extractTextConfig(key: String, rawConfig: Map[String, (Option[String], Int)]): Either[String, String] = {
    rawConfig.get(DEFAULT_LIFE_PATH) match {
      case Some((opt, rowCount)) => opt match {
        case Some(value) => Right(value)
        case None => Left(missingConfig(DEFAULT_LIFE_PATH, rowCount))
      }
      case None => Left(versionNotMatchedErrMsg(DEFAULT_LIFE_PATH))
    }
  }

  /**
   * 提取正整数数值型配置
   *
   * @param key       配置键名称
   * @param rawConfig 原始配置
   */
  private def extractPositiveIntConfig(key: String, rawConfig: Map[String, (Option[String], Int)]): Either[String, Int] = {
    extractIntConfig(key, rawConfig, "请配置为正整数").flatMap {
      case (value, rowCount) => if (value <= 0) Left(wrongConfig(key, "请配置为正整数", rowCount)) else Right(value)
    }
  }

  /**
   * 提取非负整数值型配置
   *
   * @param key       配置键名称
   * @param rawConfig 原始配置
   */
  private def extractPositiveOrZeroIntConfig(key: String, rawConfig: Map[String, (Option[String], Int)]): Either[String, Int] = {
    extractIntConfig(key, rawConfig, "请配置为非负整数").flatMap {
      case (value, rowCount) => if (value < 0) Left(wrongConfig(key, "请配置为非负整数", rowCount)) else Right(value)
    }
  }

  /**
   * 提取文本型配置
   *
   * @param key       配置键名称
   * @param rawConfig 原始配置
   */
  private def extractIntConfig(key: String, rawConfig: Map[String, (Option[String], Int)], defaultHit: String): Either[String, (Int, Int)] = {
    rawConfig.get(key) match {
      case Some((opt, rowCount)) => opt match {
        case Some(value) => Try(value.toInt) match {
          case Failure(_) => Left(wrongConfig(key, defaultHit, rowCount))
          case Success(value) => Right((value, rowCount))
        }
        case None => Left(missingConfig(key, rowCount))
      }
      case None => Left(versionNotMatchedErrMsg(key))
    }
  }

  /**
   * Excel 版本不匹配错误信息
   *
   * @param key 缺失的配置键
   * @return 错误信息
   */
  private def versionNotMatchedErrMsg(key: String): String = s"缺少必要的配置：$key，Excel 版本可能不正确"

  /**
   * 该项未配置
   *
   * @param key      配置键
   * @param rowCount 当前行数
   * @return 错误信息
   */
  private def missingConfig(key: String, rowCount: Int): String = s"[基本配置页：${rowCount}行]请配置$key"

  /**
   * 配置错误
   *
   * @param key      配置键
   * @param message  错误信息
   * @param rowCount 当前行数
   * @return 错误信息
   */
  //noinspection SameParameterValue
  private def wrongConfig(key: String, message: String, rowCount: Int): String = s"[基本配置页：${rowCount}行]${key}配置错误，$message"
}
