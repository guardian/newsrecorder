package models
import scala.xml._

object NewChannel extends XmlHelpers {
  def fromXmlNode(xmlNode:Node):Channel = {
    Channel(xmlNode.attribute("id").map(_.head.text).getOrElse("unknown"),
      (xmlNode \ "display-name").text, getAttributeString(xmlNode \ "icon", "src"))
  }
}

case class Channel(channelId:String, displayName:String, iconUrl: Option[String]) {

}
