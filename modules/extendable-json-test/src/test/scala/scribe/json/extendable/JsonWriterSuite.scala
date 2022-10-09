package scribe.json.extendable

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scribe.Logging
import scribe.json.event._
import scribe.writer.CacheWriter

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

class JsonWriterSuite extends AnyFlatSpec with Matchers with Inside with BeforeAndAfterEach with Logging {
  val cache = new CacheWriter
  logger.withHandler(writer = cache).replace()

  override def beforeEach(): Unit = {
    cache.clear()
  }

  behavior.of("ExtendableJsonWriter")

  it should "able to logging LogstashRecord using circe" in {
    import io.circe.generic.auto._
    import io.circe.parser._
    import scribe.json.circe._
    val logstashEventWriter = new ExtendableJsonWriter[LogstashRecord](cache, Map("service" -> "test"))
    logger.withHandler(writer = logstashEventWriter).replace()
    logger.info("omg this is crazy")
    val output = cache.output.head.plainText
    val result = parse(output).flatMap(_.as[LogstashRecord])

    inside(result) { case Right(event) =>
      event.service shouldBe Some("test")
      event.level shouldBe "INFO"
    }
  }

  it should "able to logging LogstashRecord using fabric" in {
    import scribe.json.fabric._
    import fabric.rw._
    import fabric.io._
    implicit val offsetDateTimeRW: RW[OffsetDateTime] =
      RW.apply(_.format(ISO_OFFSET_DATE_TIME).json, js => OffsetDateTime.parse(js.asString))
    implicit val rw: RW[LogstashRecord] = ccRW[LogstashRecord]
    val logstashEventWriter = new ExtendableJsonWriter[LogstashRecord](cache, Map("service" -> "test"))
    logger.withHandler(writer = logstashEventWriter).replace()
    logger.info("omg this is crazy")
    val output = cache.output.head.plainText
    val event = JsonParser(output, Format.Json).as[LogstashRecord]
    event.service shouldBe Some("test")
    event.level shouldBe "INFO"
  }
}
