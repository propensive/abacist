/*
    Abacist, version [unreleased]. Copyright 2024 Jon Pretty, Propensive OÜ.

    The primary distribution site is: https://propensive.com/

    Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
    file except in compliance with the License. You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied. See the License for the specific language governing permissions
    and limitations under the License.
*/

package abacist

import rudiments.*
import anticipation.*
import quantitative.*
import spectacular.*
import symbolism.*

import scala.quoted.*
import scala.compiletime.*, ops.int.*

object Abacist2:
  opaque type Count[UnitsType <: Tuple] = Long

  object Count:
    erased given [UnitsType <: Tuple] => Underlying[Count[UnitsType], Long] as underlying = ###
    def fromLong[UnitsType <: Tuple](long: Long): Count[UnitsType] = long

    given [UnitsType <: Tuple] => Integral[Count[UnitsType]] as integral = summon[Integral[Long]]

    inline def apply[UnitsType <: Tuple](inline values: Int*): Count[UnitsType] =
      ${Abacist.make[UnitsType]('values)}

    given [UnitsType <: Tuple] => Count[UnitsType] is Addable as plus:
      type Operand = Count[UnitsType]
      type Result = Count[UnitsType]

      def add(left: Count[UnitsType], right: Count[UnitsType]): Count[UnitsType] = left + right

    given [UnitsType <: Tuple] => Count[UnitsType] is Subtractable as minus:
      type Operand = Count[UnitsType]
      type Result = Count[UnitsType]

      def subtract(left: Count[UnitsType], right: Count[UnitsType]): Count[UnitsType] = left - right

    given [UnitsType <: Tuple] => Count[UnitsType] is Multiplicable as times:
      type Operand = Double
      type Result = Count[UnitsType]

      def multiply(left: Count[UnitsType], right: Double): Count[UnitsType] = left.multiply(right)

    given [UnitsType <: Tuple] => Count[UnitsType] is Divisible as divide:
      type Operand = Double
      type Result = Count[UnitsType]

      def divide(left: Count[UnitsType], right: Double): Count[UnitsType] = left.divide(right)

    inline given [UnitsType <: Tuple] => Count[UnitsType] is Showable = summonFrom:
      case names: UnitsNames[UnitsType] => count =>
        val nonzeroComponents = count.components.filter(_(1) != 0)
        val nonzeroUnits = nonzeroComponents.map(_(1).toString.tt).to(List)
        val units = nonzeroUnits.head :: nonzeroUnits.tail.map(names.separator+_)
        units.interleave(names.units().takeRight(nonzeroUnits.length)).mkString.tt

      case _ => count =>
        val nonzeroComponents = count.components.filter(_(1) != 0)
        nonzeroComponents.map { (unit, count) => count.toString+unit }.mkString(" ").tt

  extension [UnitsType <: Tuple](count: Count[UnitsType])
    def longValue: Long = count

  extension [UnitsType <: Tuple](inline count: Count[UnitsType])
    @targetName("negate")
    inline def `unary_-`: Count[UnitsType] = -count

    inline def apply[UnitType[PowerType <: Nat] <: Units[PowerType, ? <: Dimension]]: Int =
      ${Abacist.get[UnitsType, UnitType[1]]('count)}

    transparent inline def quantity: Any =
      ${Abacist.toQuantity[UnitsType]('count)}

    inline def components: ListMap[Text, Long] =
      ${Abacist.describeCount[UnitsType]('count)}

    transparent inline def multiply(inline multiplier: Double): Any =
      ${Abacist.multiplyCount('count, 'multiplier, false)}

    transparent inline def divide(inline multiplier: Double): Any =
      ${Abacist.multiplyCount('count, 'multiplier, true)}

    transparent inline def collapse(length: Int)(using length.type < Tuple.Size[UnitsType] =:= true)
          : Count[Tuple.Drop[UnitsType, length.type]] =

      count