package arfies.restart.life.condition.parser

/**
 * Token 类型
 * Author: sinar
 * 2022/8/10 21:18
 */
object TokenTypes extends Enumeration {
  type TokenType = Value
  val EOF: TokenType = Value("文件结尾")
  val NAME_OR_VALUE: TokenType = Value("名称或值")
  val TYPE: TokenType = Value("类型")
  val EXIST: TokenType = Value("条件判断符：拥有")
  val NOT_EXIST: TokenType = Value("条件判断符：没有")
  val AT: TokenType = Value("条件判断符：处于")
  val NOT_AT: TokenType = Value("条件判断符：不在")
  val EXP: TokenType = Value("条件判断符：经历过")
  val NOT_EXP: TokenType = Value("条件判断符：未经历")
  val AND: TokenType = Value("连接符：和")
  val OR: TokenType = Value("连接符：或")
  // Operators Start
  val OPERATOR: TokenType = Value("任意操作符")
  val GT: TokenType = Value("大于号")
  val LT: TokenType = Value("小于号")
  val GE: TokenType = Value("大于等于号")
  val LE: TokenType = Value("小于等于号")
  val EQ: TokenType = Value("等于号")
  val NE: TokenType = Value("不等于号")
  // Operators End
  val COLON: TokenType = Value("冒号")
  val LEFT_PAREN: TokenType = Value("左括号")
  val RIGHT_PAREN: TokenType = Value("右括号")
}
