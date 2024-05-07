package omnibus.identifier

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.jvm.uuid._
import org.scalatest.{ EitherValues, Tag }
import org.slf4j.LoggerFactory
import io.circe.parser
import io.circe.syntax._

class ShortUUIDSpec extends AnyFlatSpec with Matchers with EitherValues {
  val log = LoggerFactory.getLogger( classOf[ShortUUIDSpec] )

  object WIP extends Tag( "wip" )

  "A ShortUUID" should "create a nil" in {
    ShortUUID.toUUID( ShortUUID.zero ) shouldBe UUID( 0L, 0L )
  }

  it should "represent uuid without loss" in {
    val expectedUuid = UUID.random
    val short = ShortUUID.fromUUID( expectedUuid )
    val replayedUuid = ShortUUID.toUUID( short )
    expectedUuid shouldBe replayedUuid
  }

  it should "convert from uuid implicitly" in {
    val uuid = UUID.random
    val expected = ShortUUID.fromUUID( uuid )
    val actual: ShortUUID = uuid
    expected shouldBe actual
  }

  it should "convert to uuid implicitly" in {
    val expected = UUID.random
    val short = ShortUUID.fromUUID( expected )
    val actual: UUID = short
    expected shouldBe actual
  }

  it should "create unique short uuids" in {
    val first = ShortUUID()
    val second = ShortUUID()
    first should not be second
  }

  it should "serde Circe Json" taggedAs WIP in {
    val sidValue = ShortUUID()
    log.debug( s"sidValue = ${sidValue}" )

    val sidJson = sidValue.asJson
    log.debug( s"sidJson = ${sidJson}" )

    val actual = parser.parse( sidJson.noSpaces ).flatMap( _.as[ShortUUID] )
    log.debug( s"ShortUUID Circe deser actual = ${actual}" )
    actual.value shouldBe sidValue
  }

}
