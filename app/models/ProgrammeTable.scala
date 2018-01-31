package models

import java.sql.Date
import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util.UUID

import slick.jdbc.SQLiteProfile.api._

/*
case class Programme(startTime: ZonedDateTime, endTime: ZonedDateTime, channelId: String, title: String,
                     subTitle: Option[String], description: Option[String], category:Option[Seq[String]],
                     credits: Option[Map[String,Seq[String]]], episodeId: Option[String]) {

}

 */

class ProgrammeTable(tag:Tag) extends Table[Programme](tag, "PROGRAMME") with CustomConverters {
  val channels = TableQuery[ChannelTable]

  def generation = column[Int]("GENERATION")

  def uuid = column[UUID]("PROG_UUID",O.PrimaryKey)
  def startTime = column[ZonedDateTime]("START")
  def endTime = column[ZonedDateTime]("END")
  def channelId = column[String]("CHANNEL_ID")

  def title = column[String]("TITLE")
  def subTitle = column[Option[String]]("SUB_TITLE")
  def description = column[Option[String]]("DESC")
  def episodeId = column[Option[String]]("EPISODE_ID")
  def category = column[Option[Seq[String]]]("CATEGORY")

  def channelIdKey = foreignKey("FK_PROG_CHANNEL", channelId, channels)(_.id)

  def titleIndex = index("IDX_PROG_TITLE",title)
  def episodeIdIndex = index("IDX_PROG_EPISODEID", episodeId)

  def * = (generation, uuid,startTime,endTime,channelId,title,subTitle,description,category,episodeId) <> (Programme.tupled, Programme.unapply)
}
