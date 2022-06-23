package arfies.restart.life.story

import arfies.restart.life.achievement.Achievement
import arfies.restart.life.player.Player.{Attr, Buff, Skill}
import arfies.restart.life.story.Story.StoryConfig

/**
 * 故事
 *
 * Author: sinar
 * 2022/6/21 23:22
 */
case class Story
(
  attrs: Map[String, Attr],
  buffs: Map[String, Buff],
  talents: Map[String, Skill],
  skills: Map[String, Skill],
  achievements: Map[String, Achievement],
  events: Seq[Event],
  endings: Seq[Ending],
  config: StoryConfig
)

object Story {
  case class StoryConfig(availableAttrPoints: Int, defaultLifePath: String)
}
