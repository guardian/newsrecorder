package models

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kantan.xpath._           // Basic kantan.xpath types.
import kantan.xpath.implicits._ // Implicit operators and literals.
import scala.xml.{Node, NodeSeq}

object Programme extends XmlHelpers {
  def getCredits(xmlNode:NodeSeq):Option[Map[String,String]] = {
    if(xmlNode.isEmpty) None else {
        Some(Map(
          (for (node <- xmlNode; childNode <- node.child; if childNode.label!="#PCDATA") yield childNode.label -> childNode.text): _*
        ))
    }
  }

  def fromXmlNode(xmlNode:Node):Programme = {
    val formatter:DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z")
    //FIXME: this is really ugly and inefficient. I guess that I should re-write the whole parser to use Kantan instead of scala.xml
    val episodeNum = xmlNode.toString().evalXPath[String](xp"//episode-num[@system='dd_progid']") match {
      case Success(xpResult)=>Some(xpResult)
      case Failure(error)=>
        println(error)
        None
    }

    println(s"got episodeNum $episodeNum")

    new Programme(
      ZonedDateTime.parse(getAttributeString(xmlNode,"start").get, formatter),
      ZonedDateTime.parse(getAttributeString(xmlNode,"stop").get, formatter),
      getAttributeString(xmlNode,"channel").get,
      (xmlNode \ "title").map(_.text).mkString("\n"),
      subNodeAsOption(xmlNode \ "sub-title").map(_.text),
      subNodeAsOption(xmlNode \ "desc").map(_.text),
      subNodeAsOption(xmlNode \ "category").map(_.map(_.text)),
//      subNodeAsOption(xmlNode \ "category") match {
//        case Some(nodeseq)=>Some(nodeseq.map(_.text))
//        case None=>None
//      },
      getCredits(xmlNode \ "credits"),
      episodeNum
    )
  }
}

case class Programme(startTime: ZonedDateTime, endTime: ZonedDateTime, channelId: String, title: String,
                     subTitle: Option[String], description: Option[String], category:Option[Seq[String]],
                     credits: Option[Map[String,String]], episodeId: Option[String]) {

}
