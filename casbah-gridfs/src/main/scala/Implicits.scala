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
package gridfs

import scala.language.implicitConversions

trait Implicits {
  implicit def wrapDBFile(in: com.mongodb.gridfs.GridFSDBFile): GridFSDBFile = new GridFSDBFile(in)

  implicit def wrapInFile(in: com.mongodb.gridfs.GridFSInputFile): GridFSInputFile = new GridFSInputFile(in)

  implicit def wrapJodaDBFile(in: com.mongodb.gridfs.GridFSDBFile): JodaGridFSDBFile = new JodaGridFSDBFile(in)

  implicit def wrapJodaInFile(in: com.mongodb.gridfs.GridFSInputFile): JodaGridFSInputFile = new JodaGridFSInputFile(in)
}

object Implicits extends Implicits

object Imports extends Imports

object BaseImports extends BaseImports

object TypeImports extends TypeImports

trait Imports extends BaseImports with TypeImports with Implicits

trait BaseImports {
  val GridFS = com.mongodb.casbah.gridfs.GridFS
  val JodaGridFS = com.mongodb.casbah.gridfs.JodaGridFS
}

trait TypeImports {
  type GridFS = com.mongodb.casbah.gridfs.GridFS
  type GridFSDBFile = com.mongodb.casbah.gridfs.GridFSDBFile
  type GridFSInputFile = com.mongodb.casbah.gridfs.GridFSInputFile
  type GridFSFile = com.mongodb.casbah.gridfs.GridFSFile
  type JodaGridFS = com.mongodb.casbah.gridfs.JodaGridFS
  type JodaGridFSDBFile = com.mongodb.casbah.gridfs.JodaGridFSDBFile
  type JodaGridFSInputFile = com.mongodb.casbah.gridfs.JodaGridFSInputFile
  type JodaGridFSFile = com.mongodb.casbah.gridfs.JodaGridFSFile
}
