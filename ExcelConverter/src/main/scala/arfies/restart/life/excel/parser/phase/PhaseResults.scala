package arfies.restart.life.excel.parser.phase

import arfies.restart.life.achievement.Achievement
import arfies.restart.life.story.Story.StoryConfig
import arfies.restart.life.player.Player.*
import arfies.restart.life.excel.ir.*
import arfies.restart.life.story.Ending

/**
 * 解析阶段结果
 *
 * Author: sinar
 * 2022/10/23 17:09
 */
object PhaseResults {
  case class ConfigAndKeywords
  (
    config: StoryConfig,
    attrs: Map[String, Attr],
    rawSkills: Map[String, SkillIR],
    rawTalents: Map[String, SkillIR],
    rawBuffs: Map[String, BuffIR],
    rawEvents: Map[String, EventIR],
    rawAchievements: Map[String, AchievementIR],
    rawEndings: Map[String, EndingIR],
    keywords: Keywords
  )

  case class ParsedAchievements
  (
    config: StoryConfig,
    attrs: Map[String, Attr],
    rawSkills: Map[String, SkillIR],
    rawTalents: Map[String, SkillIR],
    rawBuffs: Map[String, BuffIR],
    rawEvents: Map[String, EventIR],
    achievements: Map[String, Achievement],
    rawEndings: Map[String, EndingIR],
    keywords: Keywords
  )

  case class ParsedEndings
  (
    config: StoryConfig,
    attrs: Map[String, Attr],
    rawSkills: Map[String, SkillIR],
    rawTalents: Map[String, SkillIR],
    rawBuffs: Map[String, BuffIR],
    rawEvents: Map[String, EventIR],
    achievements: Map[String, Achievement],
    endings: Map[String, Ending],
    keywords: Keywords
  )

  case class ParsedSkillsAndTalents
  (
    config: StoryConfig,
    attrs: Map[String, Attr],
    skills: Map[String, Skill],
    talents: Map[String, Skill],
    rawBuffs: Map[String, BuffIR],
    rawEvents: Map[String, EventIR],
    achievements: Map[String, Achievement],
    endings: Map[String, Ending],
    keywords: Keywords
  )

  case class ParsedBuffs
  (
    config: StoryConfig,
    attrs: Map[String, Attr],
    skills: Map[String, Skill],
    talents: Map[String, Skill],
    buffs: Map[String, Buff],
    rawEvents: Map[String, EventIR],
    achievements: Map[String, Achievement],
    endings: Map[String, Ending],
    keywords: Keywords
  )


}
