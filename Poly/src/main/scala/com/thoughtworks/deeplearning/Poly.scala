package com.thoughtworks.deeplearning

import com.thoughtworks.deeplearning.Layer.Tape
import com.thoughtworks.deeplearning.Symbolic._
import shapeless.{Lazy, Poly1, Poly2}

/**
  * @author 杨博 (Yang Bo) &lt;pop.atry@gmail.com&gt;
  */
object Poly {

  /**
    * A [[shapeless.Poly1 unary polymorphic function]] that accepts some kind of [[Layer]]s or values able to convert to those kind of layers.
    *
    * @see [[https://github.com/milessabin/shapeless/wiki/Feature-overview:-shapeless-2.0.0#polymorphic-function-values]]
    */
  trait LayerPoly1 extends Poly1 {
    implicit def toLayerCase[Operand, Input <: Tape, OperandData, OperandDelta](
        implicit toLayer: ToLayer.Aux[Operand, Input, OperandData, OperandDelta],
        layerCase: Lazy[Case[Layer.Aux[Input, Tape.Aux[OperandData, OperandDelta]]]]
    ): Case.Aux[Operand, layerCase.value.Result] = {
      at { operand =>
        layerCase.value(toLayer(operand))
      }
    }
  }

  /**
    * A [[shapeless.Poly2 binary polymorphic function]] that accepts some kind of [[Layer]]s or values able to convert to those kind of layers.
    *
    * @see [[https://github.com/milessabin/shapeless/wiki/Feature-overview:-shapeless-2.0.0#polymorphic-function-values]]
    */
  trait LayerPoly2 extends Poly2 {
    implicit def toLayerCase[LeftOperand, RightOperand, Input <: Tape, LeftData, LeftDelta, RightData, RightDelta](
        implicit leftToLayer: ToLayer.Aux[LeftOperand, Input, LeftData, LeftDelta],
        rightToLayer: ToLayer.Aux[RightOperand, Input, RightData, RightDelta],
        layerCase: Lazy[
          Case[Layer.Aux[Input, Tape.Aux[LeftData, LeftDelta]], Layer.Aux[Input, Tape.Aux[RightData, RightDelta]]]]
    ): Case.Aux[LeftOperand, RightOperand, layerCase.value.Result] = {
      at { (left, right) =>
        val leftLayer = leftToLayer(left)
        val rightLayer = rightToLayer(right)
        layerCase.value(leftLayer, rightLayer)
      }
    }
  }

  object MathMethods {
    object - extends LayerPoly2
    object + extends LayerPoly2
    object * extends LayerPoly2
    object / extends LayerPoly2
  }

  implicit final class MathOps[Left](left: Left) {

    def -[Right](right: Right)(implicit methodCase: MathMethods.-.Case[Left, Right]): methodCase.Result =
      MathMethods.-(left, right)

    def +[Right](right: Right)(implicit methodCase: MathMethods.+.Case[Left, Right]): methodCase.Result =
      MathMethods.+(left, right)

    def *[Right](right: Right)(implicit methodCase: MathMethods.*.Case[Left, Right]): methodCase.Result =
      MathMethods.*(left, right)

    def /[Right](right: Right)(implicit methodCase: MathMethods./.Case[Left, Right]): methodCase.Result =
      MathMethods./(left, right)

  }

  object MathFunctions {

    object log extends LayerPoly1
    object exp extends LayerPoly1
    object abs extends LayerPoly1
    object max extends LayerPoly2
    object min extends LayerPoly2

  }

}
