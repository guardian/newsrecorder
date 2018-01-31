package models

import java.util.UUID

import scala.xml.{Node, NodeSeq}

object CreditsList {
  def getCredits(xmlNode:NodeSeq):Seq[(String,String)] =
    for(
        node<-xmlNode;
        childNode <- node.child
        if childNode.label!="#PCDATA"
      ) yield childNode.label -> childNode.text

  def fromXmlNode(xmlNode:Node, programmeRef: String, generation: Int): Seq[Credit] = {
    getCredits(xmlNode \ "credits").map(credInfo=>Credit(programmeRef, generation, credInfo._1, credInfo._2))
  }

}

case class Credit(programmeRef: String, generation:Int, role:String, name: String) {

}
