package arfies.restart.life.condition.parser

import arfies.restart.life.exception.SyntaxError
import arfies.restart.life.story.Condition
import common.UnitSpec

import scala.util.Try

/**
 * Author: sinar
 * 2022/9/16 11:18
 */
class ParserTest extends UnitSpec {
  "简单的比较条件语句" should "正确解析" in {
    val parser = new Parser("德望>=30")
    val result = parser.parse().asInstanceOf[Statements.Compare]
    assert(result.opt == Condition.Opts.Attr.GREAT_EQUAL)
    assert(result.name == "德望")
    assert(result.value == "30")
  }

  "简单的存在语句" should "正确解析" in {
    val parser = new Parser("拥有buff：免死金牌")
    val result = parser.parse().asInstanceOf[Statements.ExistOrNot]
    assert(result.opt == Condition.Opts.ExistOrNot.EXIST)
    assert(result.name == "免死金牌")
    assert(result.target == Condition.Targets.BUFF)
  }

  "简单的人生轨道语句" should "正确解析" in {
    val parser = new Parser("处于低谷期")
    val result = parser.parse().asInstanceOf[Statements.AtOrHaveExp]
    assert(result.opt == Condition.Opts.Path.AT)
    assert(result.name == "低谷期")
  }

  "简单的经历语句" should "正确解析" in {
    val parser = new Parser("未经历离别")
    val result = parser.parse().asInstanceOf[Statements.AtOrHaveExp]
    assert(result.opt == Condition.Opts.EventHistory.NOT_HAVE)
    assert(result.name == "离别")
  }

  "简单语句加括号" should "不影响解析结果" in {
    Seq("(未经历离别)", "(未经历离别）", "（未经历离别）") foreach { input =>
      val parser = new Parser(input)
      val result = parser.parse().asInstanceOf[Statements.AtOrHaveExp]
      assert(result.opt == Condition.Opts.EventHistory.NOT_HAVE)
      assert(result.name == "离别")
    }
  }

  "组合语句" should "正确解析" in {
    val parser = new Parser("拥有buff：免死金牌，且未经历离别")
    val result = parser.parse()
    val stat = result.asInstanceOf[Statements.Combine]
    assert(stat.tpe == Condition.Targets.AND)
    val first = stat.stats.head.asInstanceOf[Statements.ExistOrNot]
    assert(first.opt == Condition.Opts.ExistOrNot.EXIST)
    assert(first.name == "免死金牌")
    assert(first.target == Condition.Targets.BUFF)
    val second = stat.stats.last.asInstanceOf[Statements.AtOrHaveExp]
    assert(second.opt == Condition.Opts.EventHistory.NOT_HAVE)
    assert(second.name == "离别")
  }

  "同时存在和/且，并且没加括号" should "报错并给出理由" in {
    val parser = new Parser("拥有buff：免死金牌，或未经历离别，且德望>=30")
    val error = Try(parser.parse()).failed.get
    assert(error.isInstanceOf[SyntaxError])
    assert(error.getMessage == "[第1行] 和/或同时出现并且没有添加括号")
  }

  "同时存在和/且，且加了括号" should "正确解析" in {
    val parser = new Parser("（拥有buff：免死金牌，或未经历离别），且德望<=30")
    val result = parser.parse()
    val stat = result.asInstanceOf[Statements.Combine]
    assert(stat.tpe == Condition.Targets.AND)

    // （拥有buff：免死金牌，或未经历离别）
    val firstGroup = stat.stats.head.asInstanceOf[Statements.Combine]
    assert(firstGroup.tpe == Condition.Targets.OR)
    val firstGroup_First = firstGroup.stats.head.asInstanceOf[Statements.ExistOrNot]
    assert(firstGroup_First.opt == Condition.Opts.ExistOrNot.EXIST)
    assert(firstGroup_First.name == "免死金牌")
    assert(firstGroup_First.target == Condition.Targets.BUFF)
    val firstGroup_Second = firstGroup.stats.last.asInstanceOf[Statements.AtOrHaveExp]
    assert(firstGroup_Second.opt == Condition.Opts.EventHistory.NOT_HAVE)
    assert(firstGroup_Second.name == "离别")

    // 德望<=30
    val second = stat.stats.last.asInstanceOf[Statements.Compare]
    assert(second.opt == Condition.Opts.Attr.LESS_EQUAL)
    assert(second.name == "德望")
    assert(second.value == "30")
  }

  "复杂语句" should "正确解析" in {
    //                                                   c3               c4             c2           c1
    val parser = new Parser("（（（拥有buff：免死金牌，或未经历离别），且德望<=30)，或处于低谷期）")
    val result = parser.parse()
    // （（（拥有buff：免死金牌，或未经历离别），且德望<=30)，或处于低谷期）
    val stat = result.asInstanceOf[Statements.Combine]
    assert(stat.tpe == Condition.Targets.OR)

    // (（拥有buff：免死金牌，或未经历离别），且德望<=30)
    val g1 = stat.stats.head.asInstanceOf[Statements.Combine]
    assert(g1.tpe == Condition.Targets.AND)

    // 德望<=30
    val c2 = g1.stats.last.asInstanceOf[Statements.Compare]
    assert(c2.opt == Condition.Opts.Attr.LESS_EQUAL)
    assert(c2.name == "德望")
    assert(c2.value == "30")

    //（拥有buff：免死金牌，或未经历离别）
    val g2 = g1.stats.head.asInstanceOf[Statements.Combine]
    assert(g2.tpe == Condition.Targets.OR)

    // 拥有buff：免死金牌
    val c3 = g2.stats.head.asInstanceOf[Statements.ExistOrNot]
    assert(c3.opt == Condition.Opts.ExistOrNot.EXIST)
    assert(c3.name == "免死金牌")

    // 未经历离别
    val c4 = g2.stats.last.asInstanceOf[Statements.AtOrHaveExp]
    assert(c4.opt == Condition.Opts.EventHistory.NOT_HAVE)
    assert(c4.name == "离别")

    // 处于低谷期
    val c1 = stat.stats.last.asInstanceOf[Statements.AtOrHaveExp]
    assert(c1.opt == Condition.Opts.Path.AT)
    assert(c1.name == "低谷期")
  }
}
