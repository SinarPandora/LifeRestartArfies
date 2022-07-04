package arfies.restart.life.generator

import arfies.restart.life.output.GameOutput
import arfies.restart.life.state.StateLoader
import arfies.restart.life.story.Story

import scala.util.Random

/**
 * 故事上下文
 *
 * Author: sinar
 * 2022/6/21 22:06
 */
case class StoryContext
(
  story: Story, // 故事对象
  loader: StateLoader, // 状态加载器
  out: GameOutput, // 游戏输出
  random: Random // 游戏内随机，内部维护状态，不需保存
)
