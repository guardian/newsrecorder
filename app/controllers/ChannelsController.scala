package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.mvc.{AbstractController, ControllerComponents}
import io.circe.generic.auto._
import io.circe.syntax._
import models.ChannelTable
import org.sqlite.SQLiteErrorCode
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.SQLiteProfile.api._
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ChannelsController @Inject()(protected val dbConfigProvider:DatabaseConfigProvider, cc: ControllerComponents, actorSystem: ActorSystem)
                                  (implicit exec: ExecutionContext)
  extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile]{

  def allChannels = Action.async {
    val channels = TableQuery[ChannelTable]

    try {
      val results = db.run(channels.result)

      results.map(channelList => Ok(channelList.asJson.toString))

    } catch {
      case excep:org.sqlite.SQLiteException=>
        if(excep.getResultCode==SQLiteErrorCode.SQLITE_BUSY){
          Future(RequestTimeout("Database cache busy, please try again"))
        } else {
          Logger.error("Could not list channels",excep)
          Future(InternalServerError(excep.toString))
        }
    }
  }

  def channelById(channelId:String) = Action.async { implicit request=>
    val channels = TableQuery[ChannelTable]

    try {
      val results = db.run(channels.filter(_.id===channelId).result)

      results.map(channelList => Ok(channelList.asJson.toString))
    } catch {
      case excep:org.sqlite.SQLiteException=>
        if(excep.getResultCode== SQLiteErrorCode.SQLITE_BUSY){
          Future(RequestTimeout("Database cache busy, please try again"))
        } else {
          Logger.error("Could not list channels",excep)
          Future(InternalServerError(excep.toString))
        }
    }
  }
}
