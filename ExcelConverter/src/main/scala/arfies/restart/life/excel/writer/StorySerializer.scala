package arfies.restart.life.excel.writer

import arfies.restart.life.story.Story
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.*

import java.io.Writer

/**
 * 故事序列化器
 *
 * Author: sinar
 * 2022/7/4 21:29
 */
object StorySerializer {
  private implicit val fmt: DefaultFormats.type = DefaultFormats

  /**
   * 序列化为 Json
   * （直接调用 Json4s 进行转化）
   *
   * @param out   File Writer
   * @param story 故事对象
   * @return 转换结果
   */
  def serialize(out: Writer)(story: Story): Writer = write(story, out)
}
