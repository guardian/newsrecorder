import java.time.{ZoneId, ZoneOffset, ZonedDateTime}

import org.specs2._
import models.{NewProgramme, Programme}

class TestProgramme extends mutable.Specification {
  "Test the programme data model".title

  "Programme" should {
    "be creatable from standard XML spec" in {
      val progSpec = <programme start="20180129180000 +0000" stop="20180129183000 +0000" channel="south.bbc1.bbc.co.uk">
        <title lang="en">BBC News at Six</title>
        <desc lang="en">The latest national and international news stories from the BBC News team, followed by weather. Also in HD. [S]</desc>
        <category lang="en">news</category>
        <category lang="en">comment</category>
      </programme>

      val programme = NewProgramme.fromXmlNode(progSpec,1)
      programme.channelId mustEqual "south.bbc1.bbc.co.uk"
      programme.category must beSome(Seq("news", "comment"))
      //programme.credits must beNone
      programme.startTime mustEqual ZonedDateTime.of(2018,1,29,18,0,0,0,ZoneOffset.UTC)
      programme.endTime mustEqual ZonedDateTime.of(2018,1,29,18,30,0,0,ZoneOffset.UTC)
      programme.title mustEqual "BBC News at Six"
      programme.description must beSome("The latest national and international news stories from the BBC News team, followed by weather. Also in HD. [S]")
      //programme.credits must beNone
    }

    "support extended information from schedules direct" in {
      val progSpec = <programme start="20180202160000 +0000" stop="20180202163000 +0000" channel="I102471.json.schedulesdirect.org">
        <title>Say Yes to the Dress</title>
        <sub-title>Requests Like None of the Rest</sub-title>
        <desc>Amanda is looking for a dress with tons of embellishment; Tamara is looking for a gown to make a statement at her destination wedding; Stephanie comes into her fitting still not convinced that she's bought the right dress.</desc>
        <credits>
          <actor>Ronnie Rothstein</actor>
          <actor>Mara Urshel</actor>
          <actor>Randy Fenoli</actor>
        </credits>
        <category>Reality</category>
        <category>Fashion</category>
        <category>Series</category>
        <category>series</category>
        <length units="minutes">22</length>
        <episode-num system="xmltv_ns">8 . 18/19 . </episode-num>
        <episode-num system="dd_progid">EP014051470152</episode-num>
        <audio>
          <stereo>stereo</stereo>
        </audio>
        <previously-shown start="20130301" />
        <subtitles type="teletext" />
        <rating system="Canadian Parental Rating">
          <value>PG</value>
        </rating>
        <rating system="USA Parental Rating">
          <value>TVPG</value>
        </rating>
      </programme>

      val programme = NewProgramme.fromXmlNode(progSpec,1)
      programme.channelId mustEqual "I102471.json.schedulesdirect.org"
      programme.title mustEqual "Say Yes to the Dress"
      programme.subTitle must beSome("Requests Like None of the Rest")
      //programme.credits.get must havePair("actor"->Seq("Ronnie Rothstein","Mara Urshel","Randy Fenoli"))
      programme.category must beSome(Seq("Reality","Fashion","Series","series"))
      programme.episodeId must beSome("EP014051470152")
    }
  }
}
