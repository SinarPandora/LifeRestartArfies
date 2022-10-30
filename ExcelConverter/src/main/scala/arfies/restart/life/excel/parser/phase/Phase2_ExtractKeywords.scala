package arfies.restart.life.excel.parser.phase

import arfies.restart.life.excel.ir.*
import arfies.restart.life.excel.parser.phase.PhaseResults.ConfigAndKeywords
import arfies.restart.life.excel.reader.ExcelReader
import arfies.restart.life.story.Story.StoryConfig

/**
 * 阶段 2：提取关键词
 *
 * Author: sinar
 * 2022/7/7 22:40
 */
object Phase2_ExtractKeywords extends ParserPhase[StoryConfig, ConfigAndKeywords] {
  object AttrType {
    val NUM: String = "数值"
    val TAG: String = "标签"
  }

  /**
   * 执行
   *
   * @param data Excel 数据
   * @param from 传入数据
   * @return 传出数据
   */
  override def apply(data: ExcelReader.ExcelData, from: StoryConfig): Either[Seq[String], ConfigAndKeywords] = data match {
    case ExcelReader.ExcelData(_, attrSeq, skillOrTalentSeq, buffSeq, eventSeq, achievementSeq, endingSeq) =>
      val attrs = attrSeq.map(it => it.name -> it).toMap
      if (attrs.sizeIs != attrSeq.size) {
        return Left(Seq(s"[角色数值属性页] 存在重名标签或属性：${attrSeq.groupBy(_.name).filter(_._2.lengthIs > 1).keys.mkString("\n")}"))
      }
      val rawSkills = Named.toMap(skillOrTalentSeq.filterNot(_.isTalent))
      val rawTalents = Named.toMap(skillOrTalentSeq.filter(_.isTalent))
      if (skillOrTalentSeq.sizeIs != (rawSkills.size + rawTalents.size)) {
        return Left(Seq(s"[技能天赋页] 存在重名技能或天赋：${findDuplicate(skillOrTalentSeq)}"))
      }
      val rawBuffs = Named.toMap(buffSeq)
      if (rawBuffs.sizeIs != buffSeq.size) {
        return Left(Seq(s"[Buff页] 存在重名Buff：${findDuplicate(buffSeq)}"))
      }
      val rawEvents = Named.toMap(eventSeq)
      if (rawEvents.sizeIs != eventSeq.size) {
        return Left(Seq(s"[事件列表页] 存在重名事件：${findDuplicate(eventSeq)}"))
      }
      val rawAchievements = Named.toMap(achievementSeq)
      if (rawAchievements.sizeIs != achievementSeq.size) {
        return Left(Seq(s"[成就页] 存在重名成就：${findDuplicate(achievementSeq)}"))
      }
      val rawEndings = Named.toMap(endingSeq)
      if (rawEndings.sizeIs != endingSeq.size) {
        return Left(Seq(s"[结局页] 存在重名结局：${findDuplicate(endingSeq)}"))
      }
      Right {
        ConfigAndKeywords(
          config = from,
          attrs = attrs,
          rawSkills = rawSkills,
          rawTalents = rawTalents,
          rawBuffs = rawBuffs,
          rawEvents = rawEvents,
          rawAchievements = rawAchievements,
          rawEndings = rawEndings,
          keywords = Keywords(
            attrs = attrs.filter(_._2.attrType == AttrType.NUM).keySet,
            tags = attrs.filter(_._2.attrType == AttrType.TAG).keySet,
            skills = rawSkills.keySet,
            talents = rawTalents.keySet,
            buffs = rawBuffs.keySet,
            events = rawEvents.keySet,
            achievements = rawAchievements.keySet,
            endings = rawEndings.keySet,
          )
        )
      }
  }

  /**
   * 找出所有重名元素
   *
   * @param items 元素序列
   * @return 重名元素，一行一个，字符串形式
   */
  def findDuplicate(items: Seq[Named]): String = Named.findDuplicate(items).mkString("\n")
}
