package models

import java.sql.Date
import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util.UUID
import slick.jdbc.SQLiteProfile.api._

trait CustomConverters {
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

  implicit val uuidColumnType = MappedColumnType.base[UUID,String](
    { data=>data.toString},
    { sql =>UUID.fromString(sql)}
  )

}
