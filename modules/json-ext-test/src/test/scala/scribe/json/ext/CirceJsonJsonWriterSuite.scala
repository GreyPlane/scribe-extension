package scribe.json.ext
import io.circe.generic.auto._
import io.circe.parser._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterEach, Inside}
import scribe._
import scribe.json.circe._
import scribe.json.event.{LogstashRecord, _}
import scribe.output.EmptyOutput
class CirceJsonJsonWriterSuite
    extends AnyFlatSpec
    with Matchers
    with Inside
    with BeforeAndAfterEach
    with JsonWriterSuite {

  val logstashRecordWriter = new ExtendableJsonWriter[LogstashRecord](cache, Map("service" -> "test"))

  logger.clearHandlers().withHandler(writer = logstashRecordWriter).replace()
  override def beforeEach(): Unit = {
    cache.clear()
  }

  behavior.of("ExtendableJsonWriter")

  it should "able to log LogstashRecord using circe" in {
    logger.info("omg this is crazy")
    val output = cache.output.head.plainText
    val result = parse(output).flatMap(_.as[LogstashRecord])

    inside(result) { case Right(event) =>
      event.service shouldBe Some("test")
      event.level shouldBe Level.Info.name
    }
  }

  it should "able to log LogstashRecord using circe with stack_trace" in {
    val exp = new IllegalStateException("oops")
    logger.error("something wrong", exp)
    val output = cache.output.head.plainText
    val result = parse(output).flatMap(_.as[LogstashRecord])

    inside(result) { case Right(event) =>
      event.stack_trace shouldBe Some(LogRecord.throwable2LogOutput(EmptyOutput, exp).plainText)
      event.level shouldBe Level.Error.name
    }
  }

}
