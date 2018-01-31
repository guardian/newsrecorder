package models

case class ErrorResponse(status:String, error:String, stackTrace:Option[Array[String]]=None)
