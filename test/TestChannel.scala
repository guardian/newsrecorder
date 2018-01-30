import org.specs2._
import models.{Channel, NewChannel}

class TestChannel extends mutable.Specification {
  "Test the Channel data model".title

  "Channel" should {
    "be creatable from standard xml spec" in {
      val channelSpec = <channel id="bbc1_hd">
        <display-name>BBC 1 HD</display-name>
        <icon src="http://www.lyngsat-logo.com/icon/tv/bb/bbc1_hd.gif" />
      </channel>

      val channel = NewChannel.fromXmlNode(channelSpec)
      channel.channelId mustEqual "bbc1_hd"
      channel.displayName mustEqual "BBC 1 HD"
      channel.iconUrl mustEqual Some("http://www.lyngsat-logo.com/icon/tv/bb/bbc1_hd.gif")
    }

    "be creatable even without an icon" in {
      val channelSpec = <channel id="bbc1_hd">
        <display-name>BBC 1 HD</display-name>
      </channel>

      val channel = NewChannel.fromXmlNode(channelSpec)
      channel.channelId mustEqual "bbc1_hd"
      channel.displayName mustEqual "BBC 1 HD"
      channel.iconUrl mustEqual None
    }
  }
}
