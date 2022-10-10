package arfies.restart.life.parser.effect

import arfies.restart.life.parser.effect.Statements.*
import arfies.restart.life.parser.exception.SyntaxError
import arfies.restart.life.story.Effect

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.util.Try

/**
 * 解析器
 *
 * Author: sinar
 * 2022/9/21 19:21
 */
class Parser(sourceCode: String) {
  private val lexer: Lexer = new Lexer(sourceCode)
  private var remainParen: Int = 0

  /**
   * 解析更新语句
   * [属性名]+[数字]
   * [属性名]-[数字]
   * [属性名]*[数字]
   * [属性名]x[数字]
   * [属性名]/[数字]
   * [属性名]设置为[值]
   *
   * @return 解析结果
   */
  private def parseUpdate(): Option[Update] = {
    val next = lexer.lookAhead()
    if (next.tpe == TokenTypes.NAME_OR_VALUE) {
      lexer.nextToken(tpe = TokenTypes.OPERATOR)
        .filter(_.subType.isDefined)
        .map(token => (next.source, token.subType.get))
        .flatMap { case (name, action) =>
          lexer.nextToken(tpe = TokenTypes.NAME_OR_VALUE)
            .map { case Token(_, lineNum, value, _) =>
              if (action != TokenTypes.SET && Try(value.toInt).isFailure) {
                throw SyntaxError(s"算术运算符${action}后应为数值")(lineNum)
              }
              Update(name, action match {
                case TokenTypes.ADD => Effect.Opts.ADD
                case TokenTypes.SUB => Effect.Opts.SUB
                case TokenTypes.MUL => Effect.Opts.MUL
                case TokenTypes.DIV => Effect.Opts.DIV
                case TokenTypes.SET => Effect.Opts.SET
              }, value)
            }
        }
    } else None
  }

  /**
   * 解析得失语句
   * 删除[属性名]
   * 获得天赋: [技能名]
   * 失去天赋: [技能名]
   * 获得技能: [技能名]
   * 失去技能: [技能名]
   * 获得成就: [成就名]
   * 获得buff: [buff名]
   * 失去buff: [buff名]
   * 获得buff: [buff名][n]回合
   * 失去buff: [buff名][n]回合
   *
   * @return 解析结果
   */
  private def parseGetOrLost(): Option[GetOrLost] = {
    val opt = lexer.lookAhead()
    if (opt.tpe == TokenTypes.GET || opt.tpe == TokenTypes.LOST) {
      lexer.nextToken() // 吃掉当前操作符
      implicit val lineNum: Int = opt.lineNum
      val next = lexer.lookAhead()
      next.tpe match {
        case TokenTypes.TYPE =>
          val targetType = next.source
          if (next.source == "结局") {
            throw SyntaxError(s"不可以${opt.source}结局")
          } else if (opt.tpe == TokenTypes.LOST && targetType == "成就") {
            throw SyntaxError("不可以失去成就")
          }
          lexer.nextTokenMustBe(tpe = TokenTypes.COLON)
          val Token(_, _, rawName, _) = lexer.nextTokenMustBe(tpe = TokenTypes.NAME_OR_VALUE)
          var roundOpt: Option[Int] = None
          val name = if (rawName.endsWith("回合")) {
            val raw = rawName.stripSuffix("回合")
            val round = Try {
              raw.reverseIterator
                .takeWhile(c => '0' <= c && c <= '9')
                .mkString.reverse.toInt
            }.getOrElse(throw SyntaxError("回合数应是整数值"))
            roundOpt = Some(round)
            val name = raw.stripSuffix(round.toString)
            if (name.isEmpty) throw SyntaxError("能调整回合数的 Buff 名不能是数字")
            name
          } else rawName
          if (roundOpt.isDefined && targetType != "buff" && targetType != "Buff") {
            throw SyntaxError(s"只有 Buff 能被减少回合，操作目标：$targetType，回合数：${roundOpt.get}")
          }
          Some {
            GetOrLost(
              targetType match {
                case "天赋" => Effect.Targets.TALENT
                case "技能" => Effect.Targets.SKILL
                case "成就" => Effect.Targets.ACHIEVEMENT
                case "Buff" | "buff" => Effect.Targets.BUFF
              }, name, opt.tpe match {
                case TokenTypes.GET => Effect.Opts.ADD
                case TokenTypes.LOST =>
                  if (targetType == "buff" || targetType == "Buff") Effect.Opts.SUB
                  else Effect.Opts.DEL
              }, roundOpt
            )
          }
        case TokenTypes.NAME_OR_VALUE =>
          // 处理删除语句
          val token = lexer.nextToken()
          if (token.tpe == TokenTypes.NAME_OR_VALUE) {
            Some(GetOrLost(Effect.Targets.ATTR, token.source, Effect.Opts.DEL))
          } else None
        case _ => throw SyntaxError("不完整的得失/删除语句")(opt.lineNum)
      }
    } else None
  }

  /**
   * 解析进入/转入语句
   * 转入: [人生轨道名]
   * 进入结局：[结局名]
   *
   * @return 解析结果
   */
  private def parseInto(): Option[Into] = {
    val into = lexer.lookAhead()
    if (into.tpe == TokenTypes.INTO) {
      lexer.nextToken() // 吃掉当前操作符
      implicit val lineNum: Int = into.lineNum
      val next = lexer.nextToken()
      next.tpe match {
        case TokenTypes.TYPE =>
          if (next.source != "结局") throw SyntaxError("只可以'进入'结局")
          lexer.nextTokenMustBe(TokenTypes.COLON)
          val name = lexer.nextTokenMustBe(TokenTypes.NAME_OR_VALUE)
          Some(Into(Effect.Targets.ENDING, name.source))
        case TokenTypes.COLON =>
          val name = lexer.nextTokenMustBe(TokenTypes.NAME_OR_VALUE)
          Some(Into(Effect.Targets.PATH, name.source))
        case _ => throw SyntaxError("不完整的转入/进入语句")
      }
    } else None
  }

  /**
   * 扫描效果
   *
   * @param lastStat 上一条语句
   * @return 扫描结果
   */
  @tailrec private def parseEffect(lastStat: Option[Statement] = None): Option[Statement] = {
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
        } else parseEffect(lastStat)
      case _ =>
        lastStat.orElse(parseStatement()) match {
          case Some(stat) =>
            val next = lexer.lookAhead()
            next.tpe match {
              case TokenTypes.AND | TokenTypes.OR =>
                val combineStat = parseCombineUntilEnd(stat, next.lineNum)
                if (lexer.lookAhead().tpe != TokenTypes.EOF) {
                  parseEffect(Some(combineStat))
                } else Some(combineStat)
              case TokenTypes.RIGHT_PAREN =>
                remainParen -= 1
                lexer.nextToken() // 扔掉右括号
                if (remainParen == 0 && lexer.lookAhead().tpe == TokenTypes.EOF) Some(stat)
                else parseEffect(Some(stat))
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
          (if (lexer.lookAhead().tpe == TokenTypes.LEFT_PAREN) parseEffect() // 如果存在括号，用全解析
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
      case TokenTypes.AND => Effect.Targets.AND
      case TokenTypes.OR => Effect.Targets.OR
    }

    Combine(combineBy, stats.toSeq)
  }

  /**
   * 解析除组合语句外的语句
   *
   * @return 解析结果
   */
  private def parseStatement(): Option[Statement] = {
    parseInto()
      .orElse(parseGetOrLost())
      .orElse(parseUpdate())
  }

  /**
   * 尝试解析
   *
   * @return 解析结果
   */
  @throws[SyntaxError]
  def parse(): Statement = {
    parseEffect() match {
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
