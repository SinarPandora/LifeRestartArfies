package arfies.restart.life.parser.condition

import arfies.restart.life.parser.condition.TokenTypes.TokenType

/**
 * Token
 *
 * Author: sinar
 * 2022/8/10 21:23
 */
object Results {

  case class Token(tpe: TokenType, lineNum: Int, source: String, subType: Option[TokenType])

  object Token {
    def apply(tpe: TokenType, source: String, subType: Option[TokenType] = None)(implicit lineNum: Int): Token =
      new Token(tpe, lineNum, source, subType)
  }
}
