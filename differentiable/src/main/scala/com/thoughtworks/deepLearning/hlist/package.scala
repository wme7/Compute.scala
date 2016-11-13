package com.thoughtworks.deepLearning
//
//import com.thoughtworks.deepLearning.NeuralNetwork.{NeuralNetwork.Aux, IsNeuralNetwork}
//import hlist.ast._
//import any._
//import com.thoughtworks.deepLearning.any.ast.Identity
//
import com.thoughtworks.deepLearning.Batch.Aux
import com.thoughtworks.deepLearning.NeuralNetwork.Aux
import com.thoughtworks.deepLearning.Type.{DataOf, DeltaOf}
import com.thoughtworks.deepLearning.hlist.ast._

import scala.language.implicitConversions
import scala.language.existentials

/**
  * @author 杨博 (Yang Bo) &lt;pop.atry@gmail.com&gt;
  */
package object hlist {

  /** @template */
  type HList = Type[_ <: shapeless.HList, _ <: shapeless.Coproduct]

  /** @template */
  type HNil = Type[shapeless.HNil, shapeless.CNil]

  /** @template */
  type ::[Head <: Type[_, _], Tail <: HList] =
    Type[shapeless.::[DataOf[Head], DataOf[Tail]], shapeless.:+:[DeltaOf[Head], DeltaOf[Tail]]]

//
//  implicit final class HListOps[TailAst](val tail: TailAst) {
//
//    def ::[Input0 <: Batch,
//           HeadAst,
//           HeadData,
//           HeadDelta,
//           TailData <: shapeless.HList,
//           TailDelta <: shapeless.Coproduct](head: HeadAst)(
//        implicit unapplyHead: IsNeuralNetwork[HeadAst, Input0, HeadData, HeadDelta],
//        unapplyTail: IsNeuralNetwork[TailAst, Input0, TailData, TailDelta]
//    ): NeuralNetwork.Aux[Input0, Batch.Aux[shapeless.::[HeadData, TailData], shapeless.:+:[HeadDelta, TailDelta]]] = {
//      HCons[Input0, HeadData, HeadDelta, TailData, TailDelta](unapplyHead(head), unapplyTail(tail))
//    }
//
//  }
  val HNil: ast.HNil.type = ast.HNil

  implicit def hnilToNeuralNetwork[InputData, InputDelta](implicit inputType: Type[InputData, InputDelta])
    : IsNeuralNetwork.Aux[ast.HNil.type, Batch.Aux[InputData, InputDelta], shapeless.HNil, shapeless.CNil] =
    new IsNeuralNetwork[ast.HNil.type, Batch.Aux[InputData, InputDelta]] {
      override type OutputData = shapeless.HNil
      override type OutputDelta = shapeless.CNil

      override def apply(hnil: ast.HNil.type) = hnil
    }

  final class HListOps[Input <: Batch, TailData <: shapeless.HList, TailDelta <: shapeless.Coproduct](
      tail: NeuralNetwork.Aux[Input, Batch.Aux[TailData, TailDelta]]) {

    def ::[Head](head: Head)(implicit headIsNeuralNetwork: IsNeuralNetwork[Head, Input])
      : NeuralNetwork.Aux[Input,
                          Type[shapeless.::[headIsNeuralNetwork.OutputData, TailData],
                               shapeless.:+:[headIsNeuralNetwork.OutputDelta, TailDelta]]#Batch] = {
      HCons[Input, headIsNeuralNetwork.OutputData, headIsNeuralNetwork.OutputDelta, TailData, TailDelta](
        headIsNeuralNetwork(head),
        tail)
    }

  }

  implicit def toHListOps[From, Input <: Batch, TailData <: shapeless.HList, TailDelta <: shapeless.Coproduct](
      from: From)(
      implicit isNeuralNetwork: IsNeuralNetwork.Aux[From, Input, TailData, TailDelta]
  ): HListOps[Input, TailData, TailDelta] = {
    new HListOps[Input, TailData, TailDelta](isNeuralNetwork(from))
  }

  final class HConsOps[Input <: Batch, HeadData, HeadDelta, TailData <: shapeless.HList,
  TailDelta <: shapeless.Coproduct](
      hcons: NeuralNetwork.Aux[Input,
                               Batch.Aux[shapeless.::[HeadData, TailData], shapeless.:+:[HeadDelta, TailDelta]]]) {
    def head: NeuralNetwork.Aux[Input, Type[HeadData, HeadDelta]#Batch] =
      Head[Input, HeadData, HeadDelta, TailData, TailDelta](hcons)

    def tail: NeuralNetwork.Aux[Input, Type[TailData, TailDelta]#Batch] =
      Tail[Input, HeadData, HeadDelta, TailData, TailDelta](hcons)
  }

  implicit def toHConsOps[From,
                          Input <: Batch,
                          OutputData,
                          OutputDelta,
                          HeadData,
                          HeadDelta,
                          TailData <: shapeless.HList,
                          TailDelta <: shapeless.Coproduct](from: From)(
      implicit isNeuralNetwork: IsNeuralNetwork.Aux[From, Input, OutputData, OutputDelta],
      toHListAst: NeuralNetwork.Aux[Input, Batch.Aux[OutputData, OutputDelta]] <:< NeuralNetwork.Aux[
        Input,
        Batch.Aux[shapeless.::[HeadData, TailData], shapeless.:+:[HeadDelta, TailDelta]]]
  ): HConsOps[Input, HeadData, HeadDelta, TailData, TailDelta] = {
    new HConsOps[Input, HeadData, HeadDelta, TailData, TailDelta](toHListAst(isNeuralNetwork(from)))
  }

}
