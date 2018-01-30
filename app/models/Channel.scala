package models
import scala.xml._

//it's annoying, but if this is named as a companion object then Slick tries to use that for mapping the ChannelTable
//rather than the case class. So it's called something different.
object NewChannel extends XmlHelpers {
  def fromXmlNode(xmlNode:Node):Channel = {
    Channel(xmlNode.attribute("id").map(_.head.text).getOrElse("unknown"),
      (xmlNode \ "display-name").text, getAttributeString(xmlNode \ "icon", "src"))
  }
}

case class Channel(channelId:String, displayName:String, iconUrl: Option[String]) {

}
