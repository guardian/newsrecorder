package services

import javax.inject._

import akka.actor.ActorSystem
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future
import scala.xml.XML
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@Singleton
class DatabaseRefresh @Inject() (protected val dbConfigProvider: DatabaseConfigProvider,
                                 protected val actorSystem: ActorSystem) extends HasDatabaseConfigProvider[JdbcProfile]{
  private val channels = TableQuery[ChannelTable]

  maybeSetupSchema().onComplete({
    case Success(unit)=>
      Logger.info("Successfully set up schema")
      actorSystem.scheduler.schedule(1.micro, 10.seconds){ doRefresh() }
    case Failure(error)=>
      Logger.warn("Could not set up schema",error)
      actorSystem.scheduler.schedule(1.micro, 10.seconds){ doRefresh() }
  })

  def doRefresh():Future[Unit] = {
    try {
      readInData("/Users/localhome/workdev/newsrecorder/tv_uk_extractedinfo.xml")
    } catch {
      case e:Exception=>
        Logger.error("Could not refresh: ",e)
        Future()
    }
  }

  def maybeSetupSchema():Future[Unit] = {
    Logger.info("Setting up database schema to default db")
    val schemaSetup = DBIO.seq(
      channels.schema.create
    )
    //db is provided by the HasDatabaseConfigProvider trait
    val rtn = db.run(schemaSetup)
    Logger.info("Done")
    rtn
  }

  def readInData(filename:String):Future[Unit] = {
    Logger.info(s"Reading in listings data from $filename...")
    val xmldoc = XML.loadFile(filename)

    Logger.info("Outputting channels to database")
    val channelsPresent = for(chanNode <- xmldoc \ "channel") yield NewChannel.fromXmlNode(chanNode)

    val programmes = for(progNode <- xmldoc \ "programme") yield NewProgramme.fromXmlNode(progNode)

    val populateDb = DBIO.seq(
      channels ++= channelsPresent
    )

    db.run(populateDb)
  }
}
