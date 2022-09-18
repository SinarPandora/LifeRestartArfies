package arfies.restart.life.parser.exception

/**
 * 语法错误
 *
 * Author: sinar
 * 2022/8/14 21:22
 */
class SyntaxError(lineNum: Int, message: String, cause: Throwable = null) extends Exception(message, cause)

object SyntaxError {
  def apply(message: String)(implicit lineNum: Int): SyntaxError = new SyntaxError(lineNum, s"[第${lineNum}行] $message")
  def apply(lineNum: Int, message: String): SyntaxError = new SyntaxError(lineNum, s"[第${lineNum}行] $message")
}
