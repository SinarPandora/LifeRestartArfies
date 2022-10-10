package arfies.restart.life.parser.effect

import arfies.restart.life.parser.effect.Lexer.*
import arfies.restart.life.parser.exception.SyntaxError

import scala.util.matching.Regex

/**
 * 分词器
 *
 * Author: sinar
 * 2022/9/18 16:51
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
    TEXT_OPERATOR_PATTERN.findPrefixOf(_remainSource).map { opt =>
      moveCursorRight(2)
      opt match {
        case "获得" => Token(TokenTypes.GET, opt)
        case "删除" | "失去" => Token(TokenTypes.LOST, opt)
        case "转入" | "进入" => Token(TokenTypes.INTO, opt)
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
   * 盲扫下一个 Token
   *
   * @return 扫描结果
   */
  private def scanNextToken(): Option[Token] = {
    if (_remainSource.isBlank) Some(Token(TokenTypes.EOF, _remainSource))
    else {
      _remainSource.head match {
        case c@'+' =>
          moveCursorRight(1)
          Some(Token(TokenTypes.ADD, c.toString))
        case c@('-' | '-' | '—') =>
          moveCursorRight(1)
          Some(Token(TokenTypes.SUB, c.toString))
        case c@('x' | 'X' | '*') =>
          moveCursorRight(1)
          Some(Token(TokenTypes.MUL, c.toString))
        case c@('/' | '÷') =>
          moveCursorRight(1)
          Some(Token(TokenTypes.DIV, c.toString))
        case c@(':' | '：') =>
          moveCursorRight(1)
          Some(Token(TokenTypes.COLON, c.toString))
        case c@('(' | '（') =>
          moveCursorRight(1)
          Some(Token(TokenTypes.LEFT_PAREN, c.toString))
        case c@(')' | '）') =>
          moveCursorRight(1)
          Some(Token(TokenTypes.RIGHT_PAREN, c.toString))
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
   * *当 Token 非预期，抛出异常
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
  private val TYPES: Set[String] = Set("天赋", "技能", "buff", "Buff", "成就", "结局")
  private val TEXT_OPERATOR_PATTERN: Regex = raw"^(删除|获得|失去|转入|进入)".r
  private val LOGICAL_CONNECTOR_PATTERN: Regex = raw"^[,，](?<name>[和或且])".r
}
