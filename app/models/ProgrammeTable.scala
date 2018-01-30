package models

import java.sql.Date
import java.time.{Instant, ZoneOffset, ZonedDateTime}

import slick.jdbc.SQLiteProfile.api._

/*
case class Programme(startTime: ZonedDateTime, endTime: ZonedDateTime, channelId: String, title: String,
                     subTitle: Option[String], description: Option[String], category:Option[Seq[String]],
                     credits: Option[Map[String,Seq[String]]], episodeId: Option[String]) {

}

 */

class ProgrammeTable(tag:Tag) extends Table[Programme](tag, "PROGRAMME"){
  val channels = TableQuery[ChannelTable]
  implicit val timestampColumnType = MappedColumnType.base[ZonedDateTime, Date](
    { data => new Date(data.toEpochSecond) },
    { sql => ZonedDateTime.ofInstant(Instant.ofEpochSecond(sql.getTime),ZoneOffset.UTC) }
  )

  implicit val seqIntColumnType = MappedColumnType.base[Seq[Int], String](
    { data=> data.map(_.toString).mkString(",")},
    { sql => sql.split(",").map(_.toInt)}
  )

  implicit val seqStringColumnType = MappedColumnType.base[Option[Seq[String]], String](
    { data=> data.getOrElse(Seq()).mkString("|") },
    { sql => if(sql.isEmpty) None else Some(sql.split("|"))}
  )

  def id = column[Int]("PROG_KEY",O.PrimaryKey,O.AutoInc)
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

  def * = (startTime,endTime,channelId,title,subTitle,description,category,episodeId) <> (Programme.tupled, Programme.unapply)
}
