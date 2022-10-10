package arfies.restart.life.parser.condition

import arfies.restart.life.parser.condition.Statements.*
import arfies.restart.life.parser.exception.SyntaxError
import arfies.restart.life.story.Condition

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.util.Try

/**
 * 解析器
 *
 * Author: sinar
 * 2022/8/17 23:04
 */
final class Parser(sourceCode: String) {
  private val lexer: Lexer = new Lexer(sourceCode)
  private var remainParen: Int = 0

  /**
   * 解析比较断言
   * [属性名]>[数字]
   * [属性名]>=[数字]
   * [属性名]<[数字]
   * [属性名]<=[数字]
   * [属性名]=[数字]
   * [属性名]==[数字]
   * [属性名]!=[数字]
   * [属性名]=!=[数字]
   *
   * @return 解析结果
   */
  private def parseCompare(): Option[Compare] = {
    val next = lexer.lookAhead()
    if (next.tpe == TokenTypes.NAME_OR_VALUE) {
      lexer.nextToken(tpe = TokenTypes.OPERATOR)
        .filter(_.subType.isDefined)
        .map(token => (next.source, token.subType.get))
        .flatMap { case (name, optType) =>
          lexer.nextToken(tpe = TokenTypes.NAME_OR_VALUE)
            .map { case Token(_, lineNum, value, _) =>
              if ((optType != TokenTypes.EQ && optType != TokenTypes.NE) && Try(value.toInt).isFailure) {
                throw SyntaxError(s"比较操作符${optType}后应为数值")(lineNum)
              }
              Compare(name, optType match {
                case TokenTypes.GT => Condition.Opts.Attr.GREAT
                case TokenTypes.LT => Condition.Opts.Attr.LESS
                case TokenTypes.GE => Condition.Opts.Attr.GREAT_EQUAL
                case TokenTypes.LE => Condition.Opts.Attr.LESS_EQUAL
                case TokenTypes.EQ => Condition.Opts.Attr.EQUAL
                case TokenTypes.NE => Condition.Opts.Attr.NOT_EQUAL
              }, value)
            }
        }
    } else None
  }

  /**
   * 解析存在检查断言
   * 拥有属性/标签/天赋/技能/buff/Buff：[名称]
   * 没有属性/标签/天赋/技能/buff/Buff：[名称]
   *
   * @return 解析结果
   */
  private def parseExistCheck(): Option[ExistOrNot] = {
    val next = lexer.lookAhead()
    if (next.tpe == TokenTypes.EXIST || next.tpe == TokenTypes.NOT_EXIST) {
      lexer.nextToken(tpe = TokenTypes.TYPE)
        .map { case Token(_, _, tpe, _) => (next.tpe, tpe) }
        .flatMap { pair =>
          lexer.nextToken(tpe = TokenTypes.COLON)
            .map(_ => pair)
        }
        .flatMap { case (existOrNot, tpe) =>
          lexer.nextToken(tpe = TokenTypes.NAME_OR_VALUE)
            .map { case Token(_, _, name, _) =>
              ExistOrNot(tpe match {
                case "属性" => Condition.Targets.ATTR
                case "标签" => Condition.Targets.TAG
                case "天赋" => Condition.Targets.TALENT
                case "技能" => Condition.Targets.SKILL
                case "buff" | "Buff" => Condition.Targets.BUFF
              }, existOrNot match {
                case TokenTypes.EXIST => Condition.Opts.ExistOrNot.EXIST
                case TokenTypes.NOT_EXIST => Condition.Opts.ExistOrNot.NON_EXIST
              }, name)
            }
        }
    } else None
  }

  /**
   * 解析处于阶段检查断言
   * 处于[人生轨道名]
   * 不在[人生轨道名]
   *
   * @return 解析结果
   */
  private def parseAtCheck(): Option[AtOrHaveExp] = {
    val next = lexer.lookAhead()
    if (next.tpe == TokenTypes.AT || next.tpe == TokenTypes.NOT_AT) {
      lexer.nextToken(tpe = TokenTypes.NAME_OR_VALUE)
        .map { case Token(_, _, name, _) =>
          AtOrHaveExp(next.tpe match {
            case TokenTypes.AT => Condition.Opts.Path.AT
            case TokenTypes.NOT_AT => Condition.Opts.Path.NOT_AT
          }, name)
        }
    } else None
  }

  /**
   * 解析经历检查断言
   * 经历过[事件名]
   * 未经历[事件名]
   *
   * @return 解析结果
   */
  private def parseExpCheck(): Option[Statement] = {
    val next = lexer.lookAhead()
    if (next.tpe == TokenTypes.EXP || next.tpe == TokenTypes.NOT_EXP) {
      lexer.nextToken(tpe = TokenTypes.NAME_OR_VALUE)
        .map { case Token(_, _, name, _) =>
          AtOrHaveExp(next.tpe match {
            case TokenTypes.EXP => Condition.Opts.EventHistory.HAVE_EXP
            case TokenTypes.NOT_EXP => Condition.Opts.EventHistory.NOT_HAVE
          }, name)
        }
    } else None
  }

  /**
   * 扫描条件
   *
   * @param lastStat 上一条语句
   * @return 扫描结果
   */
  @tailrec private def parseCheck(lastStat: Option[Statement] = None): Option[Statement] = {
    val token = lexer.lookAhead()
    token.tpe match {
      case TokenTypes.LEFT_PAREN =>
        remainParen += 1
        val next = lexer.nextToken()
        implicit val lineNum: Int = token.lineNum
        if (next.tpe == TokenTypes.RIGHT_PAREN || next.tpe == TokenTypes.EOF) {
          throw SyntaxError("没有内容的括号语句")
        } else if (next.tpe == TokenTypes.AND || next.tpe == TokenTypes.OR) {
          throw SyntaxError("没有意义的组合语句")
        } else parseCheck(lastStat)
      case _ =>
        lastStat.orElse(parseStatement()) match {
          case Some(stat) =>
            val next = lexer.lookAhead()
            next.tpe match {
              case TokenTypes.AND | TokenTypes.OR =>
                val combineStat = parseCombineUntilEnd(stat, next.lineNum)
                if (lexer.lookAhead().tpe != TokenTypes.EOF) {
                  parseCheck(Some(combineStat))
                } else Some(combineStat)
              case TokenTypes.RIGHT_PAREN =>
                remainParen -= 1
                lexer.nextToken() // 扔掉右括号
                if (remainParen == 0 && lexer.lookAhead().tpe == TokenTypes.EOF) Some(stat)
                else parseCheck(Some(stat))
              case TokenTypes.EOF =>
                // 正确的结束
                Some(stat)
              case _ =>
                throw SyntaxError(s"有部分内容无法被解析为语句：${lexer.remainSource}；可能缺少连接词或括号")(next.lineNum)
            }
          case None => throw SyntaxError(s"有部分内容无法被解析为语句：${lexer.remainSource}；可能缺少连接词或括号")(token.lineNum)
        }
    }
  }

  /**
   * 解析并合并组合条件
   *
   * @param first   第一个被找到的条件
   * @param lineNum 当前行数
   * @return 解析结果
   */
  private def parseCombineUntilEnd(first: Statement, lineNum: Int): Combine = {
    val stats = ListBuffer[Statement](first)

    /**
     * 扫描
     *
     * @param combineBy 连接器
     */
    @tailrec def loop(combineBy: Option[TokenTypes.TokenType] = None): TokenTypes.TokenType = {
      val connector = lexer.lookAhead()
      connector.tpe match {
        case keyword@(TokenTypes.AND | TokenTypes.OR) =>
          lexer.nextToken()
          if (combineBy.exists(_ != keyword)) throw SyntaxError("和/或同时出现并且没有添加括号")(connector.lineNum)
          (if (lexer.lookAhead().tpe == TokenTypes.LEFT_PAREN) parseCheck() // 如果存在括号，用全解析
          else parseStatement() /* 如果不存在括号，直接继续解析 */) match {
            case Some(nextStat) =>
              stats += nextStat
              loop(Some(keyword))
            case None => throw SyntaxError("和/或/且 后面没有条件")(connector.lineNum)
          }
        case TokenTypes.EOF | TokenTypes.RIGHT_PAREN =>
          // 结束
          combineBy.getOrElse(throw new IllegalStateException("解析器内部错误：误判了非组合条件"))
        case _ => throw SyntaxError("组合条件没有按照规定结尾")(connector.lineNum)
      }
    }

    val combineBy = loop() match {
      case TokenTypes.AND => Condition.Targets.AND
      case TokenTypes.OR => Condition.Targets.OR
    }

    Combine(combineBy, stats.toSeq)
  }

  /**
   * 解析语句（除组合语句外）
   *
   * @return 解析结果
   */
  private def parseStatement(): Option[Statement] = {
    parseAtCheck()
      .orElse(parseExpCheck())
      .orElse(parseExistCheck())
      .orElse(parseCompare())
  }

  /**
   * 尝试解析
   *
   * @return 解析结果
   */
  @throws[SyntaxError]
  def parse(): Statement = {
    parseCheck() match {
      case Some(result) =>
        val ending = lexer.nextTokenMustBe(tpe = TokenTypes.EOF)
        if (remainParen > 0) throw SyntaxError("未闭合的括号")(ending.lineNum)
        result
      case None => throw SyntaxError("没有找到合法语句")(1)
    }
  }
}

object Parser {
  def apply(sourceCode: String): Parser = new Parser(sourceCode.replaceAll(raw"\s+", ""))
}
