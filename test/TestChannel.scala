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

    "only take the first descriptive name" in {
      val channelSpec = <channel id="I101972.json.schedulesdirect.org">
        <display-name>BLAZE</display-name>
        <display-name>BLAZUK</display-name>
        <display-name>63</display-name>
        <icon src="https://s3.amazonaws.com/schedulesdirect/assets/stationLogos/s101972_h3_aa.png" width="360" height="270" />
      </channel>

      val channel = NewChannel.fromXmlNode(channelSpec)
      channel.displayName mustEqual "BLAZE"
    }
  }
}
