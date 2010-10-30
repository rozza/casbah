/**
 * Copyright (c) 2010 10gen, Inc. <http://10gen.com>
 * Copyright (c) 2009, 2010 Novus Partners, Inc. <http://novus.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For questions and comments about this product, please see the project page at:
 *
 *     http://github.com/mongodb/casbah
 * 
 */

package com.mongodb.casbah
package query

import com.mongodb.casbah.commons.Imports._

import scala.collection.JavaConversions._

/** 
 * Base Operator class for Bareword Operators.
 * 
 * Bareword operators stand on their own - they lack the requirement for an LValue.
 * 
 * Operator implementations (see SetOp for an example) should partially apply apply with just their operator name.
 * The apply method's type parameter can be used to restrict the valid RValue values at will.  
 * 
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0, 06/17/10
 * @since 1.0
 * @see SetOp
 */
trait BarewordQueryOperator {

  /*
   * TODO - Implicit filtering of 'valid' (aka convertable) types for [A]
   */
  def apply[A](oper: String)(fields: (String, A)*) = { 
    val bldr = MongoDBObject.newBuilder
    for ((k, v) <- fields) bldr += k -> v
    MongoDBObject(oper -> bldr.result.asDBObject)
  }

}


/** 
 * Aggregation object for Bareword Operators.
 * Bareword operators stand on their own - they lack the requirement for an LValue.
 * This mixes them in so they can be pulled down in a single import.
 *
 * Typically, you want to follow the model Implicits does, and mix this in
 * if you want to use it but not import Implicits
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0, 06/17/10
 * @since 1.0
 * @see com.mongodb.casbah.mongodb.Implicits
 */
trait FluidQueryBarewordOps extends SetOp 
                               with UnsetOp
                               with IncOp
                               with ArrayOps


/**
 * Trait to provide the $set (Set) Set method as a bareword operator.
 * 
 * $set ("Foo" -> "bar")
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject  
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0
 */
trait SetOp extends BarewordQueryOperator {
  def $set = apply[Any]("$set")_
}

/**
 * Trait to provide the $unset (UnSet) UnSet method as a bareword operator..
 *
 * $unset "foo"
 *
 * Targets an RValue of String*, where String are field names to be converted to a  DBObject  
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0
 */
trait UnsetOp extends BarewordQueryOperator {
  def $unset(args: String*) = apply("$unset")(args.map(_ -> 1): _*)
}


/** 
 * Trait to provide the $unset (UnSet) UnSet method as a bareword operator..
 *
 *   $inc ("foo" -> 5)
 *
 * Targets an RValue of (String, Numeric)* to be converted to a  DBObject  
 *
 * Due to a bug in the way I implemented type detection this fails if you mix numeric types.  E.g. floats work, but not mixing floats and ints.
 * This can be easily circumvented if you want 'ints' with floats by making your ints floats with .0:
 * 
 *   $inc ("foo" -> 5.0, "bar" -> 1.6)
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0, 06/17/10
 * @since 1.0
 */
trait IncOp extends BarewordQueryOperator {
  def $inc[T : Numeric](args: (String, T)*) = apply[T]("$inc")(args: _*)
}

trait ArrayOps extends PushOp
                  with PushAllOp
                  with AddToSetOp
                  with PopOp
                  with PullOp
                  with PullAllOp

/*
 * Trait to provide the $push (push) method as a bareword operator..
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject  
 *
 * If Field exists but is not an array an error will occurr.
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0
 */
trait PushOp extends BarewordQueryOperator {
  def $push = apply[Any]("$push")_
}

/*
 * Trait to provide the $pushAll (pushAll) method as a bareword operator..
 *
 * Targets an RValue of (String, Array[Any])* to be converted to a  DBObject  
 *
 * RValue MUST Be an array - otherwise use push.
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0
 */
trait PushAllOp extends BarewordQueryOperator {
  //def $pushAll = apply[Array[Any]]("$pushAll")_
  /*def $pushAll(args: (String, Iterable[Any])*): DBObject = apply("$pushAll")(args.map(z => (z._1, z._2.toArray)):_*)
  def $pushAll(args: (String, Product)): DBObject = $pushAll(args._1 -> args._2.productIterator.toIterable)
  def $pushAll(args: (String, Array[Any])): DBObject = $pushAll(args._1 -> args._2.toIterable)*/
    //else if (manifest[A].toString.startsWith("Array[")) // WARNING: HACK! 
  def $pushAll[A <: Any : Manifest](args: (String, A)*): DBObject = 
    if (manifest[A] <:< manifest[Iterable[_]]) 
      apply("$pushAll")(args.map(z => z._1 -> z._2.asInstanceOf[Iterable[_]]):_*)
    else if (manifest[A] <:< manifest[Product]) 
      apply("$pushAll")(args.map(z => z._1 -> z._2.asInstanceOf[Product].productIterator.toIterable): _*)
    else if (manifest[A].erasure.isArray) 
      apply("$pushAll")(args.map(z => z._1 -> z._2.asInstanceOf[Array[_]].toIterable): _*)
    else 
      throw new IllegalArgumentException("$pushAll may only be invoked with a (String, A) where String is the field name and A is an Iterable or Product/Tuple of values (got %s).".format(manifest[A]))
}

/*
 * Trait to provide the $addToSet (addToSet) method as a bareword operator..
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject  
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0
 */
trait AddToSetOp extends BarewordQueryOperator {
  def $addToSet = apply[Any]("$addToSet")_
}

/*
 * Trait to provide the $pop (pop) method as a bareword operator..
 *
 
 *
 * TODO - Support the "unshift" version in which a -1 is specified
 * 
 * If Field exists but is not an array an error will occurr.
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0
 */
trait PopOp extends BarewordQueryOperator {
  def $pop(args: String*) = apply("$pop")(args.map(_ -> 1): _*)
}

/*
 * Trait to provide the $pull (pull) method as a bareword operator..
 *
 * Targets an RValue of (String, Any)* to be converted to a  DBObject  
 *
 * If Field exists but is not an array an error will occurr.
 * 
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0
 */
trait PullOp extends BarewordQueryOperator {
  def $pull = apply[Any]("$pull")_
}

/*
 * Trait to provide the $pullAll (pullAll) method as a bareword operator..
 *
 * Targets an RValue of (String, Array[Any])* to be converted to a  DBObject  
 *
 * RValue MUST Be an array - otherwise use pull.
 *
 *
 * @author Brendan W. McAdams <brendan@10gen.com>
 * @version 1.0
 */
trait PullAllOp extends BarewordQueryOperator {
  def $pullAll = apply[Array[Any]]("$pullAll")_
}


// vim: set ts=2 sw=2 sts=2 et:
