package arfies.restart.life.parser.effect

import arfies.restart.life.parser.effect.TokenTypes.TokenType

/**
 * Token
 *
 * Author: sinar
 * 2022/8/10 21:23
 */
case class Token(tpe: TokenType, lineNum: Int, source: String, subType: Option[TokenType])

object Token {
  def apply(tpe: TokenType, source: String, subType: Option[TokenType] = None)(implicit lineNum: Int): Token =
    new Token(tpe, lineNum, source, subType)
}
