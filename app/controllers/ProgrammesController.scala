package controllers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import com.gu.scanamo._
import com.gu.scanamo.syntax._
import io.circe.generic.auto._
import io.circe.syntax._
import akka.actor.ActorSystem
import com.amazonaws.services.dynamodbv2.model.{AmazonDynamoDBException, BatchWriteItemResult}
import io.circe.{Encoder, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import models.{ChannelTable, ErrorResponse, Programme, ProgrammeTable}
import org.sqlite.SQLiteErrorCode
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.SQLiteProfile.api._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProgrammesController @Inject()(protected val dbConfigProvider:DatabaseConfigProvider,
                                     cc:ControllerComponents,
                                     system:ActorSystem,
                                     config:Configuration)
                                    (implicit exec:ExecutionContext)
  extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] with AwsHelpers with models.DynamoFormats
{
  override protected val region = config.get[String]("region")

  implicit val zoneDateTimeEncoder: Encoder[ZonedDateTime] = new Encoder[ZonedDateTime] {
    final def apply(t:ZonedDateTime):Json = Json.fromString(t.format(DateTimeFormatter.ISO_DATE_TIME))
  }

  def programmes = Action.async { implicit request=>
    val programmes = TableQuery[ProgrammeTable]
    val titleQuery = request.queryString.get("title")

    val max = request.queryString.getOrElse("max",Seq("100")).mkString("").toInt

    try {
      val results = titleQuery match {
        case Some(titleQuerySeq) =>
          val contains = titleQuerySeq.head
          db.run(programmes.filter(_.title.like(s"%$contains%")).take(max).result)
        case None =>
          db.run(programmes.take(max).result)
      }
      results.map((programmeList:Seq[Programme])=>Ok(programmeList.asJson.toString))
    } catch {
      case excep:org.sqlite.SQLiteException=>
        Logger.error(s"SQL error ${excep.getResultCode}")
        Future(InternalServerError("SQL error"))
    }

  }

  def programmesByChannel(channelId:String) = Action.async { implicit request=>
    Future(Ok(""))
  }

  def setProgrammeRecording(episodeId:String,channelId:String) = Action.async {
    val client = getClient
    val table = Table[Programme](config.get[String]("recordings-table"))

    val opsFuture = ProgrammeTable.lookup(db,episodeId,channelId).map(programmeList=>{
      val tempInfo = programmeList.map(prog=>s"${prog.uniqueId},${prog.channelId},${prog.episodeId},${prog.startTime}")

      Logger.info(s"Got programme list: ${tempInfo.mkString("\n")}")
      table.putAll(programmeList.toSet)
    })

    val dynamoResultFuture = opsFuture.map(ops => try{
      Right(Scanamo.exec(client)(ops))
    } catch {
      case excep:AmazonDynamoDBException=>
        Left(excep)
    })

    dynamoResultFuture.map({
      case Right(resultList) => Ok(resultList.asJson.toString)
      case Left(excep)=>Logger.error("Could not register recording", excep)
        InternalServerError(ErrorResponse("error",excep.toString,Some(excep.getStackTrace.map(_.toString))).asJson.toString)
    })
  }
}
