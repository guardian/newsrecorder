package services

import javax.inject._

import akka.actor.ActorSystem
import play.api.Logger

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
      actorSystem.scheduler.schedule(1.micro, 10.minutes){ doRefresh() }
    case Failure(error)=>
      Logger.warn("Could not set up schema",error)
      actorSystem.scheduler.schedule(1.micro, 10.minutes){ doRefresh() }
  })

  def doRefresh():Unit = {
    val currentGeneration = Await.result(getCurrentGeneration, 5.seconds)

    println(s"Current generation is $currentGeneration")
    val updateFutures = try {
      if(currentGeneration.isDefined)
        readInData("/Users/localhome/workdev/newsrecorder/test_schedulesdirect_extracted.xml",currentGeneration.get+1)
      else
        readInData("/Users/localhome/workdev/newsrecorder/test_schedulesdirect_extracted.xml",1)
    } catch {
      case e:Exception=>
        Logger.error("Could not refresh: ",e)
        return
    }

    Logger.info("Done")
    if(currentGeneration.isDefined) {
      Logger.info(s"Deleting old generation info ${currentGeneration.get}")
      val deleteFuture = deleteProgrammesByGeneration(currentGeneration.get)
      Await.result(deleteFuture, 1.minute)
      val deleteCredsFuture = deleteCreditsByGeneration(currentGeneration.get)
      Await.result(deleteCredsFuture, 1.minute)
      Logger.info("Done")
    }
  }

  def getCurrentGeneration:Future[Option[Int]] = {
    val availableGenerationsFuture = db.run(programmes.distinctOn(_.generation).result).map(_.sortBy(_.generation))

    availableGenerationsFuture.map(_.headOption.map(_.generation))
  }

  def maybeSetupSchema():Future[Unit] = {
    Logger.info("Setting up database schema to default db")
    val schemaSetup = DBIO.seq(
      (channels.schema ++ programmes.schema ++ credits.schema).create
    )
    //db is provided by the HasDatabaseConfigProvider trait
    val rtn = db.run(schemaSetup)
    Logger.info("Done")
    rtn
  }

  def readInData(filename:String, generation:Int):Unit = {
    Logger.info(s"Reading in listings data from $filename...")
    val xmldoc = XML.loadFile(filename)

    val timecheckChannelStart = System.currentTimeMillis()/1000

    val channelsPresent = for(chanNode <- xmldoc \ "channel") yield NewChannel.fromXmlNode(chanNode)
    val channelsUpdate = for(chan <- channelsPresent) yield channels.insertOrUpdate(chan)

    Logger.info(s"Outputting data for ${channelsPresent.length} channels to database")

    val programmesAndCredits = for(progNode <- xmldoc \ "programme") yield NewProgramme.fromXmlNodeWithCredits(progNode,generation)

    val programmesPresent = programmesAndCredits.map(_._1)
    val creditsPresent = programmesAndCredits.flatMap(_._2)

    //val credits = for(progNode <- xmldoc \ "programme") yield CreditsList.fromXmlNode(progNode)
    channelsUpdate.map(chan=>Await.result(db.run(chan),5.seconds))

    val timecheckChannelEnd = System.currentTimeMillis()/1000
    Logger.info(s"Done. Channel output took ${timecheckChannelEnd-timecheckChannelStart} seconds")

    Logger.info(s"Outputting data for ${programmesPresent.length} programmes [generation $generation] to database")
    Await.ready(db.run(DBIO.seq(programmes ++= programmesPresent)), 10.minutes)
    val timecheckProgsEnd = System.currentTimeMillis()/1000
    Logger.info(s"Done. Programmes output took ${timecheckProgsEnd-timecheckChannelEnd} seconds")

//    Logger.info(s"Outputting data for ${creditsPresent.length} credits [generation $generation] to database")
//    Await.ready(db.run(DBIO.seq(credits ++= creditsPresent)),10.minutes)
//    val timecheckCreditsEnd = System.currentTimeMillis()/1000
//    Logger.info(s"Done. Credits output took ${timecheckCreditsEnd - timecheckProgsEnd} seconds")
  }

  /* returns a future containing the number of rows deleted */
  def deleteProgrammesByGeneration(generation:Int):Future[Int] = {
    val action = programmes.filter(_.generation===generation).delete
    db.run(action)
  }

  def deleteCreditsByGeneration(generation:Int):Future[Int] = {
    val action = programmes.filter(_.generation===generation).delete
    db.run(action)
  }

}
