package omnibus.identifier

import io.jvm.uuid.UUID
import org.scalatest.{ EitherValues, Tag }
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
//import play.api.libs.json.{ Json => PJson, _ }
import io.circe.{ Json => CJson, _ }
import io.circe.syntax._
import org.slf4j.LoggerFactory

import scala.util.Random

class IdSpec extends AnyWordSpec with Matchers with EitherValues {
  private val log = LoggerFactory.getLogger( classOf[IdSpec] )

  case class Foo( id: Foo#TID, f: String ) {
    type TID = Foo.identifying.TID
  }

  object Foo {
    def nextId: Foo#TID = identifying.next
    implicit val identifying = Identifying.byShortUuid[Foo]
  }

  case class Bar( id: Id[Bar], b: Double )

  object Bar {
    type TID = identifying.TID
    def nextId: TID = identifying.next
    implicit val identifying = Identifying.byLong[Bar]
  }

  case class Zoo( id: Zoo.TID, animal: String )

  object Zoo {
    type TID = identifying.TID
    def nextId: TID = identifying.next
    implicit val identifying = Identifying.bySnowflake[Zoo]()
  }

  case class Moo( id: Moo.TID, name: String )

  object Moo {
    type TID = identifying.TID
    def nextId: TID = identifying.next

    implicit val identifying = Identifying.byUuid[Moo]
  }

  object WIP extends Tag( "wip" )

  "An Id" should {
    "summons Aux" in {
      implicit val fid = Foo.identifying.next
      val Id( shortFid ) = fid
      shortFid shouldBe a[ShortUUID]

      implicit val bid = Bar.nextId
      bid.value.getClass shouldBe classOf[Long] //todo: better handle primitive boxing
    }

    "create Id of varying types" in {
      val suid = ShortUUID()
      val fid: Id[Foo] = Id of suid
      fid shouldBe a[Id[_]]
      fid.toString shouldBe s"FooId(${suid})"
      fid.value shouldBe a[ShortUUID]
      fid.value shouldBe suid

      fid.value shouldBe suid
      suid shouldBe fid.value

      val bid: Id[Bar] = Id of 13L
      bid shouldBe a[Id[_]]
      bid.toString shouldBe "BarId(13)"
      bid.value.getClass shouldBe classOf[java.lang.Long]
      bid.value shouldBe 13L

      val zuid = Zoo.nextId.value
      val zid: Id[Zoo] = Id of zuid
      zid shouldBe a[Id[_]]
      zid.toString shouldBe s"ZooId(${zuid})"
      zid.value shouldBe a[String]
      zid.value shouldBe zuid

      zid.value shouldBe zuid
      zuid shouldBe zid.value
    }

    "unwrap composites to simple" in {
      val suid = ShortUUID()
      val ofid = Id.of[Option[Foo], ShortUUID]( suid )
      "val id: Id[Foo] = ofid" should compile
      ofid.toString shouldBe s"FooId(${suid})"
      val id: Id[Foo] = ofid
      id shouldBe a[Id[_]]
      id.toString shouldBe s"FooId(${suid})"
      id.value shouldBe a[ShortUUID]
      id.value shouldBe suid
    }

    "invalid id type should fail" in {
      "val fid: Id[Foo] = Id of 17L" shouldNot compile
    }

    "create Id from strings" in {
      val fid = ShortUUID()
      val frep = fid.toString

      val f: Id[Foo] = Id fromString frep
      f shouldBe a[Id[_]]
      f.toString shouldBe s"FooId(${fid})"
      f.value shouldBe a[ShortUUID]
      f.value shouldBe fid

      val bid = 17L
      val brep = bid.toString
      val b: Id[Bar] = Id fromString brep
      b shouldBe a[Id[_]]
      b.toString shouldBe s"BarId(${bid})"
      b.value.getClass shouldBe classOf[java.lang.Long]
      b.value shouldBe bid

      val zid = Zoo.nextId.value
      val zrep = zid.toString

      val z: Id[Zoo] = Id fromString zrep
      z shouldBe a[Id[_]]
      z.toString shouldBe s"ZooId(${zid})"
      z.value shouldBe a[String]
      z.value shouldBe zid
    }

    "invalid id rep type should fail" in {
      val fid = 17L
      val frep = fid.toString
      an[IllegalArgumentException] should be thrownBy Id.fromString[Foo, ShortUUID]( frep )
    }

    "custom labeling can override class label" in {
      implicit val fooLabeling = Labeling.custom[Foo]( "SpecialFoo" )

      val suid = ShortUUID()
      val fid = Id.of[Foo, ShortUUID]( suid )
      fid.toString shouldBe s"SpecialFooId(${suid})"

      implicit val barLabeling = new EmptyLabeling[Bar]
      val bid = Id.of[Bar, Long]( 17L )
      bid.toString shouldBe "17"
    }

    "extract id value from Id" in {
      val expected = ShortUUID()
      val fid: Id[Foo] = Id of expected
      val Id( actual ) = fid
      actual shouldBe expected
      actual shouldBe a[ShortUUID]
    }

    "support conversion to another entity basis" in {
      val fid: Id.Aux[Foo, ShortUUID] = Foo.nextId
      "val bid = fid.as[Bar]" shouldNot compile
      implicit val barShortIdentifying = Foo.identifying.as[Bar]
      "val bid = fid.as[Bar]" should compile
      val bid: Id.Aux[Bar, ShortUUID] = fid.as[Bar]
      fid.label shouldBe "Foo"
      bid.label shouldBe "Bar"
      bid.value shouldBe fid.value
      bid.toString shouldBe s"BarId(${fid.value})"
    }

    "be serializable" in {
      import java.io._
      val bytes = new ByteArrayOutputStream
      val out = new ObjectOutputStream( bytes )

      val fid = ShortUUID()
      val expected: Id.Aux[Foo, ShortUUID] = Id of fid

      out.writeObject( expected )
      out.flush()

      val in = new ObjectInputStream( new ByteArrayInputStream( bytes.toByteArray ) )
      val actual = in.readObject().asInstanceOf[Id.Aux[Foo, Long]]
      import scala.reflect.ClassTag

      val actualClassTag: ClassTag[Id.Aux[Foo, Long]] = ClassTag( actual.getClass )
      log.debug( s"actual[${actual}] type = ${actualClassTag}" )

      actual shouldBe expected
      actual.value shouldBe fid
      actual.label shouldBe "Foo"
      actual.toString shouldBe s"FooId(${fid})"
    }

    "pretty id is serializable" in {
      import java.io._
      val bytes = new ByteArrayOutputStream
      val out = new ObjectOutputStream( bytes )

      val zid = Zoo.nextId.value
      val expected: Id.Aux[Zoo, String] = Id of zid

      out.writeObject( expected )
      out.flush()

      val in = new ObjectInputStream( new ByteArrayInputStream( bytes.toByteArray ) )
      val actual = in.readObject().asInstanceOf[Id.Aux[Zoo, String]]
      import scala.reflect.ClassTag

      val actualClassTag: ClassTag[Id.Aux[Zoo, String]] = ClassTag( actual.getClass )
      log.debug( s"actual[${actual}] type = ${actualClassTag}" )

      actual shouldBe expected
      actual.value shouldBe zid
      actual.label shouldBe "Zoo"
      actual.toString shouldBe s"ZooId(${zid})"
    }

    "encode Id[ShortUUID] to/from Circe JSON" in {
      val short: Id.Aux[Foo, ShortUUID] = Foo.nextId
      val shortJson = short.asJson
      shortJson shouldBe short.value.asJson
      val fromShort = parser
        .parse( shortJson.noSpaces )
        .flatMap( _.as[Id.Aux[Foo, ShortUUID]] )
        .right
        .value
      fromShort shouldBe short
    }

    "encode Id[Long] to/from Circe JSON" in {
      val long: Id.Aux[Bar, Long] = Bar.nextId
      val longJson = long.asJson
      longJson shouldBe long.value.asJson
      val fromLong = parser
        .parse( longJson.noSpaces )
        .flatMap( _.as[Id.Aux[Bar, Long]] )
        .right
        .value
      fromLong shouldBe long
    }

    "encode Id[Snowflake] to/from Circe JSON" in {
      val snow: Id.Aux[Zoo, String] = Zoo.nextId
      val snowJson = snow.asJson
      snowJson shouldBe snow.value.asJson
      val fromSnow = parser
        .parse( snowJson.noSpaces )
        .flatMap( _.as[Id.Aux[Zoo, String]] )
        .right
        .value
      fromSnow shouldBe snow
    }

    "encode Id[UUID] to/from Circe JSON" in {
      val uuid: Id.Aux[Moo, UUID] = Moo.nextId
      val uuidJson = uuid.asJson
      uuidJson shouldBe uuid.value.asJson
      val fromUuid = parser
        .parse( uuidJson.noSpaces )
        .flatMap( _.as[Id.Aux[Moo, UUID]] )
        .right
        .value
      fromUuid shouldBe uuid
    }

    "decode ShortUUID Id from Circe Json" taggedAs WIP in {
      val sidValue = ShortUUID()
      log.debug( s"sidValue = ${sidValue}" )

      val sidJson = sidValue.asJson
      log.debug( s"sidJson = ${sidJson}" )

      parser.parse( sidJson.noSpaces ).flatMap( _.as[ShortUUID] ).right.value shouldBe sidValue

      val actualSID =
        parser.parse( sidJson.noSpaces ).flatMap( _.as[Id.Aux[Foo, ShortUUID]] ).right.value
      log.debug( s"actualSID = ${actualSID}" )

      actualSID.value shouldBe sidValue
      actualSID.label shouldBe "Foo"
    }

    "decode Long Id from Circe Json" in {
      val lidValue = Random.nextLong()
      log.debug( s"lidValue = ${lidValue}" )

      val lidJson = CJson fromLong lidValue
      log.debug( s"lidJson = ${lidJson}" )

      parser.parse( lidJson.noSpaces ).flatMap( _.as[Long] ).right.value shouldBe lidValue

      val actualLID =
        parser.parse( lidJson.noSpaces ).flatMap( _.as[Id.Aux[Bar, Long]] ).right.value
      log.debug( s"actualLID = ${actualLID}" )

      actualLID.value shouldBe lidValue
      actualLID.label shouldBe "Bar"
    }

    "decode Snowflake ID from Circe Json" in {
      val zidValue = Zoo.nextId.value
      log.debug( s"zidValue = ${zidValue}" )

      val zidJson = CJson fromString zidValue
      log.debug( s"zidJson = ${zidJson}" )

      parser.parse( zidJson.noSpaces ).flatMap( _.as[String] ).right.value shouldBe zidValue

      val actualZID =
        parser.parse( zidJson.noSpaces ).flatMap( _.as[Id.Aux[Zoo, String]] ).right.value
      log.debug( s"actualZID = ${actualZID}" )

      actualZID.value shouldBe zidValue
      actualZID.label shouldBe "Zoo"
    }

    "decode UUID ID from Circe Json" in {
      val mooValue = UUID.random
      log.debug( s"mooValue = ${mooValue}" )

      val mooJson = mooValue.asJson
      log.debug( s"mooJson = ${mooJson}" )

      parser.parse( mooJson.noSpaces ).flatMap( _.as[UUID] ).right.value shouldBe mooValue

      val actualMOO =
        parser.parse( mooJson.noSpaces ).flatMap( _.as[Id.Aux[Moo, UUID]] ).right.value
      log.debug( s"actualMOO = ${actualMOO}" )

      actualMOO.value shouldBe mooValue
      actualMOO.label shouldBe "Moo"
    }

//    "write to PlayJson" taggedAs WIP in {
//      val fid: Id.Aux[Foo, ShortUUID] = Foo.nextId
//      val bid: Id.Aux[Bar, Long] = Bar.nextId
//      val zid: Id.Aux[Zoo, String] = Zoo.nextId
//
//      PJson.toJson( fid ) shouldBe JsString( fid.value.toString )
//      PJson.toJson( bid ) shouldBe JsString( bid.value.toString )
//      PJson.toJson( zid ) shouldBe JsString( zid.value.toString )
//
//      val fid2: Id[Foo] = fid
//      PJson.toJson( fid2 ) shouldBe JsString( fid.value.toString )
//
//      val bid2: Id[Bar] = bid
//      PJson.toJson( bid2 ) shouldBe JsString( bid.value.toString )
//
//      val zid2: Id[Zoo] = zid
//      PJson.toJson( zid2 ) shouldBe JsString( zid.value.toString )
//    }

//    "read from play Json" taggedAs WIP in {
//      val sidValue = ShortUUID()
//      val lidValue = Random.nextLong()
//      val zidValue = Zoo.nextId.value
//
//      val fidJson = PJson toJson sidValue.toString
//      val lidJson = PJson toJson lidValue
//      val zidJson = PJson toJson zidValue
//
//      parser.parse( fidJson.toString ).flatMap( _.as[ShortUUID] ).right.get shouldBe sidValue
//      parser.parse( lidJson.toString ).flatMap( _.as[Long] ).right.get shouldBe lidValue
//      parser.parse( zidJson.toString ).flatMap( _.as[String] ).right.get shouldBe zidValue
//    }
  }
}
