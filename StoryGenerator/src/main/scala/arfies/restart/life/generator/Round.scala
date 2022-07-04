package arfies.restart.life.generator

import arfies.restart.life.player.Player
import arfies.restart.life.player.Player.Skill
import arfies.restart.life.state.GameState
import arfies.restart.life.story.{Condition, Effect, Event}

import scala.language.implicitConversions
import scala.util.chaining.*

/**
 * 回合
 * "游戏就是状态的变化与表示"
 * 基本规则：
 * 1. 为了避免连续触发，每个回合每个技能/状态/天赋只触发一次
 * 2. 技能/状态/天赋触发属性改变时不能再次触发自己
 *
 * TODO: v2 版本中实现"属性变化时触发"
 * TODO: 立刻触发事件效果
 *
 * Author: sinar
 * 2022/6/21 22:05
 */
class Round(ctx: StoryContext, startState: GameState) {
  private val StoryContext(story, loader, out, random) = ctx
  private val eventScope: Seq[String] = startState.roundEventScope
  // 回合计数不能在回合内被更改
  val roundCount: Int = startState.roundCount

  /**
   * 运行回合
   *
   * @return 变更后的状态
   *         Left：最终状态
   *         Right：中间状态
   */
  def run(): Either[GameState, GameState] = {
    val state1 = startPhase(startState)
    if (state1.ending.nonEmpty) return Left(state1)
    val state2 = mainPhase(state1)
    if (state2.ending.nonEmpty) return Left(state2)
    val finalState = endPhase(state2)
    if (finalState.ending.nonEmpty) Left(finalState) else Right(finalState)
  }

  /**
   * 回合开始阶段
   * 判定顺序：
   * 1. Buff
   * 2. 天赋
   * 3. 技能
   * *. 全体 buff 减 1 回合，清理过期 buff
   *
   * @param state 游戏状态
   * @return 变更后的状态
   */
  private def startPhase(state: GameState): GameState = {
    val activateBuffs: Map[Player.Buff, Option[Int]] = pickBuffs(state, Condition.Timing.BEFORE_ROUND)
    val activateTalentAndSkills: Set[Skill] = pickTalentAndSkill(state, Condition.Timing.BEFORE_ROUND)
    state
      .pipe(buffScan(activateBuffs, _))
      .pipe(skillAndTalentScan(activateTalentAndSkills, _))
  }

  /**
   * 主要阶段
   * 只判定事件
   *
   * @param state 游戏状态
   * @return 变更后的状态
   */
  private def mainPhase(state: GameState): GameState = {
    pickEvent(state) match {
      case Some(event) =>
        performEvent(state, event).getOrElse(state)
      case None =>
        out.print("无事发生")
        state
    }
  }

  /**
   * 回合结束阶段
   * 判定顺序：
   * 1. Buff
   * 2. 天赋
   * 3. 技能
   * 4. 结局
   * *. 自动保存
   *
   * @param state 游戏状态
   * @return 变更后的状态
   */
  private def endPhase(state: GameState): GameState = {
    val activateBuffs: Map[Player.Buff, Option[Int]] = pickBuffs(state, Condition.Timing.AFTER_ROUND)
    val activateTalentAndSkills: Set[Skill] = pickTalentAndSkill(state, Condition.Timing.AFTER_ROUND)
    state
      .pipe(buffScan(activateBuffs, _))
      .pipe(skillAndTalentScan(activateTalentAndSkills, _))
      .pipe(endingScan)
      .tap { state =>
        if (roundCount % 5 == 0) loader.save(state)
      }
  }

  // -------------------------------------------- UTIL METHODS ---------------------------------------------------------

  /**
   * 通用回合内效果触发封装
   *
   * @param effect    效果
   * @param gameState 游戏状态
   * @param tpe       效果类型（用于日志输出）
   * @return 结果
   */
  private def performEffect(effect: Effect, gameState: GameState, tpe: String): Option[GameState] = {
    if (gameState.ending.nonEmpty) return None
    out.debug(s"$tpe 效果开始处理")
    val afterState = effect match {
      case Effect.EventTrigger(name) =>
        // TODO 解析阶段判断事件是否存在
        performEvent(gameState, story.events(name))
      case Effect.AndChanges(effects) =>
        val after = effects.foldLeft(gameState)((state, effect) =>
          performEffect(effect, state, s"$tpe 的子效果开始处理：$effect")
            .getOrElse(state))
        if (after != gameState) Some(after) else None
      case Effect.OrChanges(effects) =>
        val pick = effects(random.nextInt(effects.length))
        performEffect(pick, gameState, s"从$tpe 的效果组中选择了：$pick，开始处理")
      case _ => Effect.perform(gameState, effect, out, story)
    }
    if (afterState.isEmpty) out.debug(s"$tpe 被跳过")
    afterState
  }

  /**
   * 检出 Buff
   *
   * @param gameState 游戏状态
   * @param timing    触发时机
   * @param excludes  排除列表
   * @return Buff 集合
   */
  private def pickBuffs(gameState: GameState, timing: String, excludes: Set[String] = Set.empty): Map[Player.Buff, Option[Int]] = {
    for {
      buffName -> roundOpt <- gameState.player.buffs
      buff <- story.buffs.get(buffName)
      if buff.activeOn.timing == timing && !excludes.contains(buffName)
    } yield buff -> roundOpt
  }

  /**
   * Buff 扫描
   *
   * @param scope     Buff 范围
   * @param gameState 游戏状态
   * @return 结果
   */
  private def buffScan(scope: Map[Player.Buff, Option[Int]], gameState: GameState): GameState = {
    scope.foldLeft(gameState) {
      case (gameState, (buff, roundCountOpt)) =>
        // 二次校验 Buff 是否存在
        if (gameState.player.buffs.contains(buff.name)) {
          val afterState = buff.effects.foldLeft(gameState)((state, effect) => {
            performEffect(effect, state, s"Buff_${buff.name}")
              .getOrElse(state)
          })
          val player = afterState.player
          roundCountOpt.map(_ - 1) match {
            case Some(remain) =>
              afterState.copy(player = afterState.player.copy(buffs =
                if (remain <= 0) player.buffs - buff.name
                else player.buffs + (buff.name -> Some(remain))
              ))
            case None => afterState
          }
        } else gameState
    }
  }

  /**
   * 检出 技能和天赋
   *
   * @param gameState 游戏状态
   * @param timing    触发时机
   * @param excludes  排除列表
   * @return Buff 集合
   */
  private def pickTalentAndSkill(gameState: GameState, timing: String, excludes: Set[String] = Set.empty): Set[Skill] = {
    (for {
      name <- gameState.player.talents
      talent <- story.talents.get(name)
      if talent.activeOn.timing == timing && !excludes.contains(name)
    } yield talent) ++ (for {
      name <- gameState.player.skills
      skill <- story.skills.get(name)
      if skill.activeOn.timing == timing && !excludes.contains(name)
    } yield skill)
  }

  /**
   * 技能天赋扫描
   *
   * @param scope     技能/天赋范围
   * @param gameState 游戏状态
   * @return 结果
   */
  private def skillAndTalentScan(scope: Set[Skill], gameState: GameState): GameState = {
    scope.foldLeft(gameState) {
      case (gameState, skill) =>
        val stillExist = (if (skill.isTalent) gameState.player.talents else gameState.player.skills).contains(skill.name)
        if (stillExist) {
          skill.effects.foldLeft(gameState)((state, effect) => {
            performEffect(effect, state, s"${if (skill.isTalent) "天赋" else "技能"}_${skill.name}")
              .getOrElse(state)
          })
        } else gameState
    }
  }

  /**
   * 抽取一条可触发的事件
   * 抽取规则：
   * 1. 存在 roundEventScope 时直接抽取
   * 2. 存在 Path 时，从 Path 抽取
   * 3. Path 内都不满足时，从没有 Path 的事件抽取
   * 4. 判断包含规则
   * 5. 判断年龄上下限
   * 6. 应用权重
   * 7. 抽取！
   *
   * @param gameState 游戏状态
   * @return 抽取的事件
   */
  private def pickEvent(gameState: GameState): Option[Event] = {
    val events = story.events
    if (eventScope.nonEmpty) {
      return eventScope
        .map(events)
        .pipe(weightedPickEvent)
    }
    val path = gameState.player.path
    val eventOnPath = events.values
      .filter(_.path.contains(path))
      .toSeq
      .pipe(pickEvent(gameState, _))
    // 若路径上存在事件，直接返回
    eventOnPath.foreach(event => return Some(event))
    events.values.filter(_.path.isEmpty)
      .toSeq
      .pipe(pickEvent(gameState, _))
  }

  /**
   * 抽取一条可触发的事件（子方法）
   * 4. 判断包含规则
   * 5. 判断年龄上下限
   * 6. 应用权重
   *
   * @param gameState 游戏状态
   * @param events    事件列表
   * @return 抽取的事件
   */
  private def pickEvent(gameState: GameState, events: Seq[Event]): Option[Event] = {
    events
      .filter(event => Condition.isMeetCondition(gameState, event.includeCond))
      .pipe(weightedPickEvent)
  }

  /**
   * 加权事件抽取（子方法）
   *
   * @param events 事件列表
   * @return 抽取的事件
   */
  private def weightedPickEvent(events: Seq[Event]): Option[Event] = {
    if (events.isEmpty) None
    else if (events.sizeIs == 1) Some(events.head)
    else {
      var sumWeight = 0
      val weights = events.map { event =>
        sumWeight += event.weight
        event.weight -> event
      }
      var pick = random.nextInt(sumWeight + 1)
      weights.find { case (weight, _) =>
        pick -= weight
        pick <= 0
      }.map(_._2)
    }
  }

  /**
   * 执行事件
   *
   * @param gameState 游戏状态
   * @param event     事件
   * @return 结果
   */
  private def performEvent(gameState: GameState, event: Event): Option[GameState] = {
    if (gameState.ending.nonEmpty) return None
    out.print(event.msg)
    val after = event.effects.foldLeft(gameState)((state, effect) => {
      performEffect(effect, state, s"事件_${event.name}")
        .getOrElse(state)
    }).copy(roundEventScope = event.nextEventScope) // TODO 解析阶段去重
    if (gameState == after) None else Some(after)
  }

  /**
   * 结局扫描
   *
   * @param gameState 游戏状态
   * @return 结果状态
   */
  def endingScan(gameState: GameState): GameState = {
    if (gameState.ending.nonEmpty) return gameState
    story.endings
      .values
      .find { ending =>
        Condition.isMeetCondition(gameState, ending.condition)
      }
      .map(ending => gameState.copy(ending = Some(ending.name)))
      .getOrElse(gameState)
  }
}

object Round {
  /**
   * 构造方法
   * （构造过程中令回合数 + 1）
   *
   * @param ctx   故事上下文
   * @param state 游戏状态
   * @return 回合对象
   */
  def apply(ctx: StoryContext, state: GameState): Round =
    new Round(ctx, state.copy(roundCount = state.roundCount + 1))
}
