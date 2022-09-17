package arfies.restart.life.condition.parser

import arfies.restart.life.condition.parser.Lexer.*
import arfies.restart.life.condition.parser.Results.Token
import arfies.restart.life.exception.SyntaxError

import scala.util.chaining.scalaUtilChainingOps
import scala.util.matching.Regex

/**
 * 分词器
 *
 * Author: sinar
 * 2022/8/10 21:13
 */
class Lexer(private var _remainSource: String = "") {
  // 简单条件表达式始终为一行
  private implicit val _lineNum: Int = 1
  private var nextTokenCache: Option[Token] = None

  /**
   * 获取当前行数
   *
   * @return 当前行数
   */
  def lineNum: Int = _lineNum

  /**
   * 获取剩余代码
   *
   * @return 剩余代码
   */
  def remainSource: String = _remainSource

  /**
   * 跳过指定长度的源码
   *
   * @param length 长度
   */
  private def moveCursorRight(length: Int): Unit = _remainSource = _remainSource.substring(length)

  /**
   * 扫描下一个名称或值
   *
   * @return 扫描结果
   */
  private def scanNextNameOrValue(): Option[Token] = {
    NAME_PATTERN.findPrefixOf(_remainSource).map { name =>
      moveCursorRight(name.length)
      if (TYPES.contains(name)) Token(TokenTypes.TYPE, name)
      else Token(TokenTypes.NAME_OR_VALUE, name)
    }
  }

  /**
   * 解析下一个文字操作符
   * * 在盲扫时，该方法应优先于扫描名称
   *
   * @return 解析结果
   */
  private def scanTextOperator(): Option[Token] = {
    TEXT_OPERATOR_LENGTH2_PATTERN.findPrefixOf(_remainSource).map { opt =>
      moveCursorRight(2)
      opt match {
        case "拥有" => Token(TokenTypes.EXIST, opt)
        case "没有" => Token(TokenTypes.NOT_EXIST, opt)
        case "处于" => Token(TokenTypes.AT, opt)
        case "不在" => Token(TokenTypes.NOT_AT, opt)
      }
    } orElse TEXT_OPERATOR_LENGTH3_PATTERN.findPrefixOf(_remainSource).map { opt =>
      moveCursorRight(3)
      opt match {
        case "经历过" => Token(TokenTypes.EXP, opt)
        case "未经历" => Token(TokenTypes.NOT_EXP, opt)
      }
    }
  }

  /**
   * 扫描连接符
   *
   * @return 扫描结果
   */
  private def scanLogicalConnector(): Option[Token] = {
    LOGICAL_CONNECTOR_PATTERN.findPrefixMatchOf(_remainSource).map { opt =>
      moveCursorRight(2)
      opt.group("name") match {
        case "和" | "且" => Token(TokenTypes.AND, opt.source.toString)
        case "或" => Token(TokenTypes.OR, opt.source.toString)
      }
    }
  }

  /**
   * 标准化比较操作符为英文标点符号
   *
   * @param input 输入
   * @return 标准化后的字符串
   */
  private def normalizeCompareOperator(input: String): String = {
    input.map {
      case '《' => '<'
      case '》' => '>'
      case '！' => '!'
      case other => other
    }
  }

  /**
   * 扫描比较操作符
   *
   * @param possible 包含比较操作符的字符串
   * @return 扫描结果
   */
  private def scanCompareOperator(possible: String): Option[Token] = {
    COMPARE_OPERATOR_PATTERN.findPrefixOf(possible).map { opt =>
      moveCursorRight(opt.length)
      opt match {
        case ">" => Token(TokenTypes.OPERATOR, opt, subType = Some(TokenTypes.GT))
        case "<" => Token(TokenTypes.OPERATOR, opt, subType = Some(TokenTypes.LT))
        case ">=" => Token(TokenTypes.OPERATOR, opt, subType = Some(TokenTypes.GE))
        case "<=" => Token(TokenTypes.OPERATOR, opt, subType = Some(TokenTypes.LE))
        case "=" | "==" => Token(TokenTypes.OPERATOR, opt, subType = Some(TokenTypes.EQ))
        case "!=" | "=!=" => Token(TokenTypes.OPERATOR, opt, subType = Some(TokenTypes.NE))
      }
    }
  }

  /**
   * 盲扫下一个 Token
   *
   * @return 扫描结果
   */
  private def scanNextToken(): Option[Token] = {
    if (_remainSource.isBlank) Some(Token(TokenTypes.EOF, _remainSource))
    else {
      _remainSource.head match {
        case c@(':' | '：') =>
          moveCursorRight(1)
          Some(Token(TokenTypes.COLON, c.toString))
        case c@('(' | '（') =>
          moveCursorRight(1)
          Some(Token(TokenTypes.LEFT_PAREN, c.toString))
        case c@(')' | '）') =>
          moveCursorRight(1)
          Some(Token(TokenTypes.RIGHT_PAREN, c.toString))
        case c if COMPARE_OPERATOR_POSSIBLE.contains(c) =>
          _remainSource
            .take(3)
            .pipe(normalizeCompareOperator)
            .pipe(scanCompareOperator)
        case ',' | '，' =>
          scanLogicalConnector()
        case _ =>
          scanTextOperator() orElse scanNextNameOrValue()
      }
    }
  }

  /**
   * 扫描下一个 Token，并只在是指定类型时返回结果
   * 消耗上一个 Token
   * *当 Token 非预期，并且是强制模型时，抛出异常
   *
   * @param tpe 必须是该 Token 类型
   * @return 扫描结果
   */
  def nextToken(tpe: TokenTypes.TokenType): Option[Token] = {
    scanNextToken() match {
      case next@Some(token) =>
        this.nextTokenCache = None
        if (token.tpe == tpe) next
        else None
      case None => throw SyntaxError(s"语法错误！未知的 Token 类型，剩余代码：${_remainSource}")
    }
  }

  /**
   * 扫描下一个 Token，并只在是指定类型时返回结果
   * 消耗上一个 Token
   * *当 Token 非预期，并且是强制模型时，抛出异常
   *
   * @param tpe 必须是该 Token 类型
   * @return 扫描结果
   */
  def nextTokenMustBe(tpe: TokenTypes.TokenType): Token = {
    scanNextToken() match {
      case Some(token) =>
        this.nextTokenCache = None
        if (token.tpe == tpe) token
        else throw SyntaxError(s"语法错误！期望值：$tpe，实际值：${token.tpe}")
      case None => throw SyntaxError(s"语法错误！未知的 Token 类型，剩余代码：${_remainSource}")
    }
  }

  /**
   * 获取下一个 Token
   * 优先消耗 lookAhead 查看过的 Token
   *
   * @return 下一个 Token
   */
  def nextToken(): Token = {
    nextTokenCache match {
      case Some(token) =>
        this.nextTokenCache = None
        token
      case None => scanNextToken().getOrElse(throw SyntaxError(s"语法错误！未知的 Token 类型，剩余代码：${_remainSource}"))
    }
  }

  /**
   * 向前看一个 Token
   * 获取都是同一个 Token，直到 Token 被消耗
   *
   * @return 下一个 Token
   */
  def lookAhead(): Token = {
    nextTokenCache match {
      case Some(token) => token
      case None => scanNextToken() match {
        case nextToken@Some(token) =>
          this.nextTokenCache = nextToken
          token
        case None => throw new IllegalStateException("解析器内部错误：EOF 后未终止扫描")
      }
    }
  }
}

object Lexer {
  private val NAME_PATTERN: Regex = raw"^[_\w\u4e00-\u9fa5]+".r
  private val TYPES: Set[String] = Set("属性", "标签", "天赋", "技能", "buff", "Buff")
  private val COMPARE_OPERATOR_POSSIBLE: Set[Char] = Set('>', '<', '=', '!', '》', '《', '！')
  private val COMPARE_OPERATOR_PATTERN: Regex = raw"^(>=|<=|=!=|!=|==|=|<|>)".r
  private val TEXT_OPERATOR_LENGTH2_PATTERN: Regex = raw"^(拥有|没有|处于|不在)".r
  private val TEXT_OPERATOR_LENGTH3_PATTERN: Regex = raw"^(经历过|未经历)".r
  private val LOGICAL_CONNECTOR_PATTERN: Regex = raw"^[,，](?<name>[和或且])".r
}

