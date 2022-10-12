package scribe.json.ext

import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scribe._
import scribe.json.event._
import scribe.output.EmptyOutput
import scribe.writer.CacheWriter

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import scala.annotation.unused

class JsonWriterSuite extends AnyFlatSpec with Matchers with Inside with BeforeAndAfterEach with Logging {
  val cache = new CacheWriter
  logger.withHandler(writer = cache).replace()

  override def beforeEach(): Unit = {
    cache.clear()
  }

  behavior.of("ExtendableJsonWriter")

  it should "able to log LogstashRecord using circe" in {
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
      event.level shouldBe Level.Info.name
    }
  }

  it should "able to log LogstashRecord using circe with stack_trace" in {
    import io.circe.generic.auto._
    import io.circe.parser._
    import scribe.json.circe._
    val logstashEventWriter = new ExtendableJsonWriter[LogstashRecord](cache, Map("service" -> "test"))
    logger.withHandler(writer = logstashEventWriter).replace()
    val exp = new IllegalStateException("oops")
    logger.error("something wrong", exp)
    val output = cache.output.head.plainText
    val result = parse(output).flatMap(_.as[LogstashRecord])

    inside(result) { case Right(event) =>
      event.stack_trace shouldBe Some(LogRecord.throwable2LogOutput(EmptyOutput, exp).plainText)
      event.level shouldBe Level.Error.name
    }
  }

  it should "able to log LogstashRecord using fabric" in {
    import scribe.json.fabric._
    import fabric.rw._
    import fabric.io._
    @unused
    implicit val offsetDateTimeRW: RW[OffsetDateTime] =
      RW.apply(_.format(ISO_OFFSET_DATE_TIME).json, js => OffsetDateTime.parse(js.asString))
    implicit val rw: RW[LogstashRecord] = ccRW[LogstashRecord]
    val logstashEventWriter = new ExtendableJsonWriter[LogstashRecord](cache, Map("service" -> "test"))
    logger.withHandler(writer = logstashEventWriter).replace()
    logger.info("omg this is crazy")
    val output = cache.output.head.plainText
    val event = JsonParser(output, Format.Json).as[LogstashRecord]
    event.service shouldBe Some("test")
    event.level shouldBe Level.Info.name
  }
}
