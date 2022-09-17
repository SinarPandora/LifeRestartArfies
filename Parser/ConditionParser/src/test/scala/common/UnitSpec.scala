package common

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should
import org.scalatest.{Inside, Inspectors, OptionValues}

/**
 * Author: sinar
 * 2022/9/16 11:18
 */
trait UnitSpec extends AnyFlatSpec with should.Matchers with
  OptionValues with Inside with Inspectors
