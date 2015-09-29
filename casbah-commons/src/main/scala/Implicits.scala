/**
 * Copyright (c) 2010 MongoDB, Inc. <http://mongodb.com>
 * Copyright (c) 2009, 2010 Novus Partners, Inc. <http://novus.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/mongodb/casbah
 *
 */

package com.mongodb.casbah
package commons

import scala.language.implicitConversions

trait Implicits {

  import com.mongodb.{ DBObject, BasicDBList }

  /*
   * Placeholder Type Alias
   */
  type JSFunction = String

  class MapWithAsDBObject(underlying: scala.collection.Map[String, Any]) {
    /**
     * Return a Mongo <code>DBObject</code> containing the Map values
     * @return DBObject
     */
    def asDBObject: DBObject = map2MongoDBObject(underlying)
  }

  /**
   * Implicit extension methods for Scala <code>Map[String, Any]</code>
   * to convert to Mongo DBObject instances.
   * Does not currently convert nested values.
   * @param map A map of [String, Any]
   */
  implicit def mapAsDBObject(map: scala.collection.Map[String, Any]): MapWithAsDBObject = new MapWithAsDBObject(map)

  implicit def map2MongoDBObject(map: scala.collection.Map[String, Any]): DBObject =
    MongoDBObject(map.toList)

  implicit def wrapDBObj(in: DBObject): MongoDBObject =
    new MongoDBObject(in)

  implicit def unwrapDBObj(in: MongoDBObject): DBObject = in.underlying

  implicit def wrapDBList(in: BasicDBList): MongoDBList = new MongoDBList(in)

  implicit def unwrapDBList(in: MongoDBList): BasicDBList = in.underlying

  // Ensure that the core Serialization helpers are registered.
  EnsureConversionHelpersRegistration.ensure
}

object EnsureConversionHelpersRegistration {
  lazy val ensure = conversions.scala.RegisterConversionHelpers()
}

object Implicits extends Implicits

object Imports extends Imports

object BaseImports extends BaseImports

object TypeImports extends TypeImports

trait Imports extends BaseImports with TypeImports with Implicits

trait BaseImports {
  val MongoDBObject = com.mongodb.casbah.commons.MongoDBObject
  val DBObject = MongoDBObject
  val MongoDBList = com.mongodb.casbah.commons.MongoDBList
  val DBList = MongoDBList
}

trait TypeImports {
  type MongoDBObject = com.mongodb.casbah.commons.MongoDBObject
  type MongoDBList = com.mongodb.casbah.commons.MongoDBList
  type DBObject = com.mongodb.DBObject
  type BasicDBObject = com.mongodb.BasicDBObject
  type BasicDBList = com.mongodb.BasicDBList
  type ObjectId = org.bson.types.ObjectId
  type BSONTimestamp = org.bson.types.BSONTimestamp
  type DBRef = com.mongodb.DBRef
  type MongoException = com.mongodb.MongoException
}

abstract class ValidBSONType[T]

object ValidBSONType {

  implicit object BasicBSONList extends ValidBSONType[org.bson.types.BasicBSONList]

  implicit object BasicDBList extends ValidBSONType[com.mongodb.BasicDBList]

  implicit object Binary extends ValidBSONType[org.bson.types.Binary]

  implicit object BSONTimestamp extends ValidBSONType[org.bson.types.BSONTimestamp]

  implicit object Code extends ValidBSONType[org.bson.types.Code]

  implicit object CodeWScope extends ValidBSONType[org.bson.types.CodeWScope]

  implicit object ObjectId extends ValidBSONType[org.bson.types.ObjectId]

  implicit object Symbol extends ValidBSONType[org.bson.types.Symbol]

  implicit object BSONObject extends ValidBSONType[org.bson.BSONObject]

  implicit object BasicDBObject extends ValidBSONType[com.mongodb.BasicDBObject]

  implicit object DBObject extends ValidBSONType[com.mongodb.DBObject]

}

/**
 * Nice trick from Miles Sabin using ambiguity in implicit resolution to disallow Nothing
 */
// scalastyle:off
sealed trait NotNothing[A] {
  type B
}

object NotNothing {
  implicit val nothing = new NotNothing[Nothing] {
    type B = Any
  }

  implicit def notNothing[A] = new NotNothing[A] {
    type B = A
  }
}
