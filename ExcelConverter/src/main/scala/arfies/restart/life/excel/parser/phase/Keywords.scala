package arfies.restart.life.excel.parser.phase

import arfies.restart.life.story.{Condition, Effect}

/**
 * 关键词
 *
 * Author: sinar
 * 2022/7/7 23:36
 */
case class Keywords
(
  attrs: Set[String],
  skills: Set[String],
  talents: Set[String],
  buffs: Set[String],
  events: Set[String],
  achievements: Set[String],
  endings: Set[String]
) {
  /**
   * 检查关键词是否存在
   * * 人生路径无法检查
   *
   * @param keyword 关键词
   * @param tpe     类型
   * @return 存在时返回关键词，当不存在时给出对应错误信息
   */
  def exists(keyword: String, tpe: String): Either[String, String] = {
    tpe match {
      case Condition.Targets.ATTR | Condition.Targets.TAG | Effect.Targets.ATTR | Effect.Targets.TAG =>
        if (!attrs.contains(keyword)) return Left(s"属性/标签不存在：$keyword")
      case Condition.Targets.SKILL | Effect.Targets.SKILL =>
        if (!skills.contains(keyword)) return Left(s"技能不存在：$keyword")
      case Condition.Targets.TALENT | Effect.Targets.TALENT =>
        if (!talents.contains(keyword)) return Left(s"天赋不存在：$keyword")
      case Condition.Targets.BUFF | Effect.Targets.BUFF =>
        if (!buffs.contains(keyword)) return Left(s"Buff 不存在：$keyword")
      case Condition.Targets.EVENT_HISTORY | Effect.Targets.EVENT =>
        if (!events.contains(keyword)) return Left(s"事件不存在：$keyword")
      case Effect.Targets.ACHIEVEMENT =>
        if (!achievements.contains(keyword)) return Left(s"成就不存在：$keyword")
      case Effect.Targets.ENDING =>
        if (!endings.contains(keyword)) return Left(s"结局不存在：$keyword")
      case Condition.Targets.PATH | Effect.Targets.PATH => // 什么也不做
    }
    Right(keyword)
  }
}
