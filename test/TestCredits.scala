import org.specs2._
import models.{Credit,CreditsList}

class TestCredits extends mutable.Specification {
  "Test the credits model".title

  "Credits" should {
    "be creatable from an XML <programme> block" in {
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

      val credits = CreditsList.fromXmlNode(progSpec, -1)

      credits.length mustEqual 3
      credits.head mustEqual Credit(-1,"actor","Ronnie Rothstein")
      credits(1) mustEqual Credit(-1,"actor","Mara Urshel")
      credits(2) mustEqual Credit(-1,"actor","Randy Fenoli")
    }
  }
}
