package models
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import com.gu.scanamo.DynamoFormat

trait DynamoFormats {
  /*this trait provides custom format converters for Scanamo/DynamoDB*/
  import DynamoFormat._

  implicit val zonedDateTimeFormat = coercedXmap[ZonedDateTime, String, IllegalArgumentException](ZonedDateTime.parse)(_.format(DateTimeFormatter.ISO_DATE_TIME))
}
