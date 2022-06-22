package arfies.restart.life.story

/**
 * 故事加载器
 *
 * Author: sinar
 * 2022/6/21 22:20
 */
trait StoryLoader {
  /**
   * 加载故事
   *
   * @return 故事对象
   */
  def load(): Story
}
