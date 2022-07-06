package arfies.restart.life.excel.util

import me.tongfei.progressbar.{ProgressBarBuilder, ProgressBarStyle}

/**
 * 进度条工具
 *
 * Author: sinar
 * 2022/7/6 22:57
 */
object ProgressBarUtil {
  /**
   * 创建构建器
   *
   * @param taskName 任务名称
   * @return 进度条构建器
   */
  def builder(taskName: String): ProgressBarBuilder = {
    new ProgressBarBuilder()
      .setStyle(ProgressBarStyle.ASCII)
      .setTaskName(taskName)
  }
}
