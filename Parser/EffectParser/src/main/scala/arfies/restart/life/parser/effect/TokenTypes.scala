package arfies.restart.life.parser.effect

/**
 * Token 类型
 * Author: sinar
 * 2022/9/18 16:54
 */
object TokenTypes extends Enumeration {
  type TokenType = Value
  val EOF: TokenType = Value("文件结尾")
  val NAME_OR_VALUE: TokenType = Value("名称或值")
  val TYPE: TokenType = Value("类型")
  val LOST: TokenType = Value("操作符：失去")
  val GET: TokenType = Value("操作符：获得")
  val INTO: TokenType = Value("操作符：转入或进入")
  // Operators Start
  val OPERATOR: TokenType = Value("任意操作符")
  val ADD: TokenType = Value("加号")
  val SUB: TokenType = Value("减号")
  val MUL: TokenType = Value("乘号")
  val DIV: TokenType = Value("除号")
  val SET: TokenType = Value("'设置为'或等于号")
  // Operators End
  val AND: TokenType = Value("连接符：和")
  val OR: TokenType = Value("连接符：或")
  val COLON: TokenType = Value("冒号")
  val ROUND: TokenType = Value("回合")
  val LEFT_PAREN: TokenType = Value("左括号")
  val RIGHT_PAREN: TokenType = Value("右括号")
}
