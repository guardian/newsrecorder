package models

import scala.xml.{Node, NodeSeq}

trait XmlHelpers {
  protected def getAttributeString(node:Node, key:String):Option[String] = node.attribute(key).map(_.text)
  protected def getAttributeString(nodeSeq:NodeSeq, key:String):Option[String] = subNodeAsOption(nodeSeq) match {
    case Some(node)=>getAttributeString(node.head,key)
    case None=>None
  }

  protected def subNodeAsOption(nodeSeq:NodeSeq):Option[Seq[Node]] = if(nodeSeq.isEmpty) None else Some(nodeSeq)
}
