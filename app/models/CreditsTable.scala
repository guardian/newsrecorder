package models

import java.util.UUID
import slick.jdbc.SQLiteProfile.api._

class CreditsTable(tag:Tag) extends Table[Credit](tag,"CREDITS") with CustomConverters {
  val programmes = TableQuery[ProgrammeTable]
  def id = column[Int]("CRED_KEY",O.PrimaryKey,O.AutoInc)
  def programme = column[UUID]("CRED_PROG_ID")
  def generation = column[Int]("GENERATION")
  def role = column[String]("CRED_ROLE")
  def name = column[String]("CRED_NAME")

  def programmeFK = foreignKey("FK_CRED_PROGRAMME",programme, programmes)(_.uuid, onUpdate=ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (programme,generation, role,name) <> (Credit.tupled, Credit.unapply)
}