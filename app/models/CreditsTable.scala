package models

import slick.jdbc.SQLiteProfile.api._

class CreditsTable(tag:Tag) extends Table[Credit](tag,"CREDITS"){
  def id = column[Int]("CRED_KEY",O.PrimaryKey,O.AutoInc)
  def programme = column[Int]("CRED_PROG_ID")
  def role = column[String]("CRED_ROLE")
  def name = column[String]("CRED_NAME")

  def * = (programme,role,name) <> (Credit.tupled, Credit.unapply)
}