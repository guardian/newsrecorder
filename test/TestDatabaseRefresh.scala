import org.specs2._
import com.google.inject.{Guice, Inject, Injector}
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._
import models.{NewProgramme, ProgrammeTable}
import org.junit.runner.RunWith
import services.DatabaseRefresh
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner
import play.api.Application
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.test.PlaySpecification
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import slick.jdbc.SQLiteProfile.api._

import akka.actor.ActorSystem

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

import org.specs2.mutable._
import org.specs2.specification.Scope
import com.google.inject.{Inject, Module, Guice, Binder}

trait GuiceContext extends Before {
  def module: Module
  def before = Guice.createInjector(module).injectMembers(this)
}

@RunWith(classOf[JUnitRunner])
class TestDatabaseRefresh extends PlaySpecification with HasDatabaseConfigProvider[JdbcProfile] {
  "Test the database refresh logic".title

  private val programmes = TableQuery[ProgrammeTable]

  private val actorSystem = ActorSystem("testDatabaseRefresh")

  trait context extends GuiceContext {
    @Inject var dbConfigProvider : DatabaseConfigProvider = null
    implicit val ee:ExecutionEnv
  }

  private def recreateDbSchema(app: Application) = {
    import dbConfig.driver.api._

    val recreateSchema: DBIO[Unit] = DBIO.seq(
      sqlu"drop schema public cascade",
      sqlu"create schema public"
    )
    Await.ready(dbConfig.db.run(recreateSchema), 5 seconds)
  }

  "getCurrentGeneration" should {
    "return None if there is no pre-exisiting content" in new context {

      val dbRefresh = new DatabaseRefresh(dbConfigProvider,actorSystem)
      dbRefresh.getCurrentGeneration must beNone.await
    }

    "return the latest generation number if there is pre-existing content" in { implicit ee: ExecutionEnv =>
      val dbRefresh = new DatabaseRefresh(dbConfigProvider,actorSystem)

      val progSpec = <programme start="20180129180000 +0000" stop="20180129183000 +0000" channel="south.bbc1.bbc.co.uk">
        <title lang="en">BBC News at Six</title>
        <desc lang="en">The latest national and international news stories from the BBC News team, followed by weather. Also in HD. [S]</desc>
        <category lang="en">news</category>
        <category lang="en">comment</category>
      </programme>
      val programme = NewProgramme.fromXmlNode(progSpec,1)

      val request = DBIO.seq(programmes ++= Seq(programme))
      Await.ready(db.run(request),10.seconds)
      dbRefresh.getCurrentGeneration must beSome(1).await
    }
  }
}
