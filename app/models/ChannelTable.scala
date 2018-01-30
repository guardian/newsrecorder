package models

import slick.jdbc.SQLiteProfile.api._

class ChannelTable(tag:Tag) extends Table[Channel](tag, "CHANNEL"){
  def id = column[String]("CHAN_ID",O.PrimaryKey)
  def displayName = column[String]("CHAN_DISPLAYNAME")
  def iconUrl = column[Option[String]]("CHAN_ICONURL")

  def * = (id, displayName, iconUrl) <> (Channel.tupled, Channel.unapply)
}
