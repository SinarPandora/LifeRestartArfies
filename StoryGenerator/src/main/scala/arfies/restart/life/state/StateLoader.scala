package arfies.restart.life.state

/**
 * 状态加载器
 *
 * Author: sinar
 * 2022/6/21 21:38
 */
trait StateLoader {
  /**
   * 加载状态（读档）
   *
   * @return 状态对象
   */
  def load(): GameState

  /**
   * 保存状态（存档）
   *
   * @param state 状态对象
   */
  def save(state: GameState): Unit
}
