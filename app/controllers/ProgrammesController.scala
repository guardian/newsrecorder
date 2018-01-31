package controllers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.generic.semiauto._
import akka.actor.ActorSystem
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult
import io.circe.{Encoder, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import models.{ChannelTable, Programme, ProgrammeTable}
import org.sqlite.SQLiteErrorCode
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.SQLiteProfile.api._
import play.api.{Configuration, Logger}
import com.gu.scanamo._
import com.gu.scanamo.syntax._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProgrammesController @Inject()(protected val dbConfigProvider:DatabaseConfigProvider,
                                     cc:ControllerComponents,
                                     system:ActorSystem,
                                     config:Configuration)
                                    (implicit exec:ExecutionContext)
  extends AbstractController(cc) with HasDatabaseConfigProvider[JdbcProfile] with AwsHelpers
{
  override protected val region = config.get[String]("region")

  implicit val zoneDateTimeEncoder: Encoder[ZonedDateTime] = new Encoder[ZonedDateTime] {
    final def apply(t:ZonedDateTime):Json = Json.fromString(t.format(DateTimeFormatter.ISO_DATE_TIME))
  }

  //implicit val programmeEncoder: Encoder[Programme] = deriveEncoder[Programme]


  def programmes = Action.async { implicit request=>
    val programmes = TableQuery[ProgrammeTable]
    val titleQuery = request.queryString.get("title")

    val max = request.queryString.getOrElse("max",Seq("100")).mkString("").toInt

    val results = titleQuery match {
      case Some(titleQuerySeq)=>
        val contains=titleQuerySeq.head
        db.run(programmes.filter(_.title.like(s"%$contains%")).take(max).result)
      case None=>
        db.run(programmes.take(max).result)
    }

    results.map((programmeList:Seq[Programme])=>Ok(programmeList.asJson.toString))
  }

  def programmesByChannel(channelId:String) = Action.async { implicit request=>
    Future(Ok(""))
  }

  def setProgrammeRecording(episodeId:String,channelId:String) = Action.async {
    val client = getClient
    val table = Table[Programme](config.get[String]("recordings-table"))

    val opsFuture = ProgrammeTable.lookup(db,episodeId,channelId).map(programmeList=>table.putAll(programmeList.toSet))
    val dynamoResultFuture = opsFuture.map(ops=>Scanamo.exec(client)(ops))

    dynamoResultFuture.map(resultList=>Ok(resultList.asJson.toString))
  }
}
