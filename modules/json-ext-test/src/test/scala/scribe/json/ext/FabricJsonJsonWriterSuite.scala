package scribe.json.ext

import fabric.io._
import fabric.rw._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterEach, Inside}
import scribe._
import scribe.json.event._
import scribe.json.fabric._

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

class FabricJsonJsonWriterSuite
    extends AnyFlatSpec
    with Matchers
    with Inside
    with BeforeAndAfterEach
    with JsonWriterSuite {

  implicit val offsetDateTimeRW: RW[OffsetDateTime] =
    RW.from(_.format(ISO_OFFSET_DATE_TIME).json, js => OffsetDateTime.parse(js.asString))

  implicit val rw: RW[LogstashRecord] = RW.gen[LogstashRecord]

  val logstashRecordWriter = new ExtendableJsonWriter[LogstashRecord](cache, Map("service" -> "test"))

  logger.clearHandlers().withHandler(writer = logstashRecordWriter).replace()

  override def beforeEach(): Unit = {
    cache.clear()
  }

  behavior.of("ExtendableJsonWriter")

  it should "able to log LogstashRecord using fabric" in {

    val logstashEventWriter = new ExtendableJsonWriter[LogstashRecord](cache, Map("service" -> "test"))
    logger.withHandler(writer = logstashEventWriter).replace()
    logger.info("omg this is crazy")
    val output = cache.output.head.plainText
    val event = JsonParser(output, Format.Json).as[LogstashRecord]
    event.service shouldBe Some("test")
    event.level shouldBe Level.Info.name
  }
}
