package arfies.restart.life.parser.effect

import arfies.restart.life.parser.effect.Statements.*
import arfies.restart.life.parser.exception.SyntaxError
import arfies.restart.life.story.Effect
import common.UnitSpec

import scala.util.Try

/**
 * Author: sinar
 * 2022/10/10 21:33
 */
class ParserTest extends UnitSpec {
  "简单的更新语句" should "正确解析" in {
    val parser = Parser("阳寿*50")
    val result = parser.parse().asInstanceOf[Update]
    assert(result.opt == Effect.Opts.MUL)
    assert(result.name == "阳寿")
    assert(result.value == "50")
  }

  "删除语句" should "正确解析" in {
    val parser = new Parser("删除呆毛")
    val result = parser.parse().asInstanceOf[GetOrLost]
    assert(result.round.isEmpty)
    assert(result.opt == Effect.Opts.DEL)
    assert(result.name == "呆毛")
    assert(result.tpe == Effect.Targets.ATTR)
  }

  "简单的获得语句" should "正确解析" in {
    val parser = Parser("获得 Buff：天选之子 10回合")
    val result = parser.parse().asInstanceOf[GetOrLost]
    assert(result.round.contains(10))
    assert(result.opt == Effect.Opts.ADD)
    assert(result.name == "天选之子")
    assert(result.tpe == Effect.Targets.BUFF)
  }

  "简单的失去技能语句" should "正确解析" in {
    val parser = Parser("失去 技能: 空中劈叉")
    val result = parser.parse().asInstanceOf[GetOrLost]
    assert(result.round.isEmpty)
    assert(result.opt == Effect.Opts.DEL)
    assert(result.name == "空中劈叉")
    assert(result.tpe == Effect.Targets.SKILL)
  }

  "带回合数的失去 Buff 语句" should "正确解析" in {
    val parser = Parser("失去 Buff：天选之子 10回合")
    val result = parser.parse().asInstanceOf[GetOrLost]
    assert(result.round.contains(10))
    assert(result.opt == Effect.Opts.SUB)
    assert(result.name == "天选之子")
    assert(result.tpe == Effect.Targets.BUFF)
  }

  "失去非 Buff N 回合" should "报错并给出理由" in {
    val parser = Parser("失去 天赋: 空中劈叉 3 回合")
    val error = Try(parser.parse()).failed.get
    assert(error.isInstanceOf[SyntaxError])
    assert(error.getMessage == "[第1行] 只有 Buff 能被减少回合，操作目标：天赋，回合数：3")
  }

  "失去成就" should "报错并给出理由" in {
    val parser = Parser("失去成就：运气王")
    val error = Try(parser.parse()).failed.get
    assert(error.isInstanceOf[SyntaxError])
    assert(error.getMessage == "[第1行] 不可以失去成就")
  }

  "简单的转入语句" should "正确解析" in {
    val parser = Parser("转入：飞黄腾达")
    val result = parser.parse().asInstanceOf[Into]
    assert(result.tpe == Effect.Targets.PATH)
    assert(result.name == "飞黄腾达")
  }

  "简单的结局" should "正确解析" in {
    val parser = Parser("进入结局：安享晚年")
    val result = parser.parse().asInstanceOf[Into]
    assert(result.tpe == Effect.Targets.ENDING)
    assert(result.name == "安享晚年")
  }

  "简单语句加括号" should "不影响解析结果" in {
    Seq("(霉运/10)", "(霉运/10）", "（霉运/10）") foreach { input =>
      val parser = Parser(input)
      val result = parser.parse().asInstanceOf[Update]
      assert(result.name == "霉运")
      assert(result.opt == Effect.Opts.DIV)
      assert(result.value == "10")
    }
  }

  "组合语句" should "正确解析" in {
    val parser = Parser("记忆力 + 100，且获得天赋：最强大脑")
    val stat = parser.parse().asInstanceOf[Combine]
    assert(stat.tpe == Effect.Targets.AND)
    val first = stat.stats.head.asInstanceOf[Update]
    assert(first.name == "记忆力")
    assert(first.value == "100")
    assert(first.opt == Effect.Opts.ADD)
    val second = stat.stats.last.asInstanceOf[GetOrLost]
    assert(second.tpe == Effect.Targets.TALENT)
    assert(second.opt == Effect.Opts.ADD)
    assert(second.name == "最强大脑")
    assert(second.round.isEmpty)
  }

  "同时存在和/且，并且没加括号" should "报错并给出理由" in {
    val parser = Parser("记忆力 + 100，且获得天赋：最强大脑，或获得技能：过目不忘")
    val error = Try(parser.parse()).failed.get
    assert(error.isInstanceOf[SyntaxError])
    assert(error.getMessage == "[第1行] 和/或同时出现并且没有添加括号")
  }

  "同时存在和/且，且加了括号" should "正确解析" in {
    val parser = Parser("记忆力 + 100，且（获得天赋：最强大脑，或获得技能：过目不忘）")
    val result = parser.parse().asInstanceOf[Combine]
    assert(result.tpe == Effect.Targets.AND)
    assert(result.stats.lengthIs == 2)

    // 记忆力 + 100
    val first = result.stats.head.asInstanceOf[Update]
    assert(first.name == "记忆力")
    assert(first.value == "100")
    assert(first.opt == Effect.Opts.ADD)

    //（获得天赋：最强大脑，或获得技能：过目不忘）
    val secondGroup = result.stats.last.asInstanceOf[Combine]
    assert(secondGroup.tpe == Effect.Targets.OR)
    assert(secondGroup.stats.lengthIs == 2)

    // 获得天赋：最强大脑
    val secondGroup_First = secondGroup.stats.head.asInstanceOf[GetOrLost]
    assert(secondGroup_First.tpe == Effect.Targets.TALENT)
    assert(secondGroup_First.name == "最强大脑")
    assert(secondGroup_First.round.isEmpty)
    assert(secondGroup_First.opt == Effect.Opts.ADD)

    // 获得技能：过目不忘
    val secondGroup_Second = secondGroup.stats.last.asInstanceOf[GetOrLost]
    assert(secondGroup_Second.tpe == Effect.Targets.SKILL)
    assert(secondGroup_Second.name == "过目不忘")
    assert(secondGroup_Second.round.isEmpty)
    assert(secondGroup_Second.opt == Effect.Opts.ADD)
  }

  "复杂内容" should "正确解析" in {
    //                            c1                 c2               c3                c4                  c5
    val parser = Parser("（（（智力 +100，且获得Buff：沉默20 回合, 且记忆力*2），且获得 buff: 沉默智者)，或转入：演说家）")
    val result = parser.parse().asInstanceOf[Combine]
    assert(result.tpe == Effect.Targets.OR)
    assert(result.stats.lengthIs == 2)

    // 或转入：演说家
    val c5 = result.stats.last.asInstanceOf[Into]
    assert(c5.tpe == Effect.Targets.PATH)
    assert(c5.name == "演说家")

    // （（智力 +100，且获得Buff：沉默20 回合, 且记忆力*2），且获得 buff: 沉默智者)
    val g1 = result.stats.head.asInstanceOf[Combine]
    assert(g1.tpe == Effect.Targets.AND)
    assert(g1.stats.lengthIs == 2)

    // 且获得 buff: 沉默智者
    val c4 = g1.stats.last.asInstanceOf[GetOrLost]
    assert(c4.tpe == Effect.Targets.BUFF)
    assert(c4.name == "沉默智者")
    assert(c4.opt == Effect.Opts.ADD)
    assert(c4.round.isEmpty)

    // （智力 +100，且获得Buff：沉默20 回合, 且记忆力*2）
    val g2 = g1.stats.head.asInstanceOf[Combine]
    assert(g2.tpe == Effect.Targets.AND)
    assert(g2.stats.lengthIs == 3)

    // 智力 +100
    val c1 = g2.stats.head.asInstanceOf[Update]
    assert(c1.name == "智力")
    assert(c1.value == "100")
    assert(c1.opt == Effect.Opts.ADD)

    // 且获得Buff：沉默20 回合
    val c2 = g2.stats(1).asInstanceOf[GetOrLost]
    assert(c2.tpe == Effect.Targets.BUFF)
    assert(c2.name == "沉默")
    assert(c2.opt == Effect.Opts.ADD)
    assert(c2.round.contains(20))

    // 且记忆力*2
    val c3 = g2.stats.last.asInstanceOf[Update]
    assert(c3.name == "记忆力")
    assert(c3.opt == Effect.Opts.MUL)
    assert(c3.value == "2")
  }
}
