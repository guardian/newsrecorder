package models

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kantan.xpath._           // Basic kantan.xpath types.
import kantan.xpath.implicits._ // Implicit operators and literals.
import scala.xml.{Node, NodeSeq}

object NewProgramme extends XmlHelpers {
  def getCredits(xmlNode:NodeSeq):Option[Map[String,Seq[String]]] = {
    if(xmlNode.isEmpty) None else {
      val creditsList = for(
        node<-xmlNode;
        childNode <- node.child
        if childNode.label!="#PCDATA"
      ) yield childNode.label -> childNode.text

      val initialMap:Map[String,Seq[String]] = Map()

      val result = creditsList.foldLeft(initialMap)((acc:Map[String,Seq[String]],entry:(String,String))=>
        if(acc.contains(entry._1)){
          acc + (entry._1 -> (acc(entry._1) ++ Seq(entry._2)).asInstanceOf[Seq[String]])
        } else {
          acc + (entry._1 -> Seq(entry._2))
        }
      )
      Some(result)
    }
  }

  def fromXmlNode(xmlNode:Node):Programme = {
    val formatter:DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z")
    //FIXME: this is really ugly and inefficient. I guess that I should re-write the whole parser to use Kantan instead of scala.xml
    val nodeString = xmlNode.toString()

    val episodeNum:Option[String] = nodeString.evalXPath[String](xp"//episode-num[@system='dd_progid']") match {
      case Success(xpResult)=>Some(xpResult.toString)
      case Failure(error)=>
        println(error)
        None
    }

    Programme(
      ZonedDateTime.parse(getAttributeString(xmlNode,"start").get, formatter),
      ZonedDateTime.parse(getAttributeString(xmlNode,"stop").get, formatter),
      getAttributeString(xmlNode,"channel").get,
      (xmlNode \ "title").map(_.text).mkString("\n"),
      subNodeAsOption(xmlNode \ "sub-title").map(_.text),
      subNodeAsOption(xmlNode \ "desc").map(_.text),
      subNodeAsOption(xmlNode \ "category").map(_.map(_.text)),
      episodeNum
    )
    //getCredits(xmlNode \ "credits"),
  }
}

case class Programme(startTime: ZonedDateTime, endTime: ZonedDateTime, channelId: String, title: String,
                     subTitle: Option[String], description: Option[String], category:Option[Seq[String]],
                     episodeId: Option[String]) {

  def getCredits:Map[String,Seq[String]] = {
    Map()
  }

}
