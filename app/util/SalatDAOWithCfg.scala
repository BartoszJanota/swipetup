package util

/**
 * Created by bj on 06.12.14.
 */

import com.mongodb.casbah.Implicits._
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import com.novus.salat.Context
import com.novus.salat.dao.SalatDAO
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._

abstract class SalatDAOWithCfg[ObjectType <: AnyRef, IDType <: Any](databaseUriConfigPath: String, collectionName: String)
                                                                   (implicit mot: Manifest[ObjectType], mid: Manifest[IDType], ctx: Context)
  extends SalatDAO[ObjectType, IDType](
    collection = {
      val clientURI = MongoClientURI(ConfigFactory.load().as[String](databaseUriConfigPath))
      val dbName  = clientURI.database.getOrElse {
        throw new IllegalArgumentException(s"You must provide database name in connection uri in path $databaseUriConfigPath!")
      }
      import scala.language.reflectiveCalls
      MongoClient(clientURI)(dbName).getCollection(collectionName).asScala
    })(mot, mid, ctx)