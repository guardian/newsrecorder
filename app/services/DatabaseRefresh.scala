package services

import javax.inject._

import akka.actor.ActorSystem
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.{Await, Future}
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
  private val programmes = TableQuery[ProgrammeTable]
  private val credits = TableQuery[CreditsTable]

  maybeSetupSchema().onComplete({
    case Success(unit)=>
      Logger.info("Successfully set up schema")
      actorSystem.scheduler.schedule(1.micro, 10.seconds){ doRefresh() }
    case Failure(error)=>
      Logger.warn("Could not set up schema",error)
      actorSystem.scheduler.schedule(1.micro, 10.seconds){ doRefresh() }
  })

  def doRefresh():Unit = {
    val currentGeneration = Await.result(getCurrentGeneration, 5.seconds)

    val updateFutures = try {
      if(currentGeneration.isDefined)
        Future.sequence(readInData("/Users/localhome/workdev/newsrecorder/tv_uk_extractedinfo.xml",currentGeneration.get+1))
      else
        Future.sequence(readInData("/Users/localhome/workdev/newsrecorder/tv_uk_extractedinfo.xml",1))
    } catch {
      case e:Exception=>
        Logger.error("Could not refresh: ",e)
        Future.failed(e)
    }

    Await.result(updateFutures, 3.minutes)
    if(currentGeneration.isDefined) {
      val deleteFuture = deleteProgrammesByGeneration(currentGeneration.get)
      Await.result(deleteFuture, 1.minute)
    }
  }

  def getCurrentGeneration:Future[Option[Int]] = {
    val availableGenerationsFuture = db.run(programmes.distinctOn(_.generation).result).map(_.sortBy(_.generation))

    availableGenerationsFuture.map(_.headOption.map(_.generation))
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

  def readInData(filename:String, generation:Int):Seq[Future[Int]] = {
    Logger.info(s"Reading in listings data from $filename...")
    val xmldoc = XML.loadFile(filename)

    Logger.info("Outputting channels to database")
    val channelsPresent = for(chanNode <- xmldoc \ "channel") yield NewChannel.fromXmlNode(chanNode)
    val channelsUpdate = for(chan <- channelsPresent) yield channels.insertOrUpdate(chan)

    val programmesPresent = for(progNode <- xmldoc \ "programme") yield NewProgramme.fromXmlNode(progNode,generation)

    //val credits = for(progNode <- xmldoc \ "programme") yield CreditsList.fromXmlNode(progNode)

    channelsUpdate.map(db.run(_))
  }

  /* returns a future containing the number of rows deleted */
  def deleteProgrammesByGeneration(generation:Int):Future[Int] = {
    val action = programmes.filter(_.generation===generation).delete
    db.run(action)
  }

}
