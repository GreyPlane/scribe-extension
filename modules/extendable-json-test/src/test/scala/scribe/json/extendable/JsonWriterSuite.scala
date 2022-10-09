package scribe.json.extendable

import io.circe.generic.auto._
import io.circe.parser._
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scribe.Logging
import scribe.json.circe._
import scribe.json.event._
import scribe.writer.CacheWriter

class JsonWriterSuite extends AnyFlatSpec with Matchers with Inside with BeforeAndAfterEach with Logging {
  val cache = new CacheWriter
  logger.withHandler(writer = cache).replace()

  override def beforeEach(): Unit = {
    cache.clear()
  }

  "ExtendableJsonWriter" should "able to logging LogstashRecord using circe" in {
    val logstashEventWriter = new ExtendableJsonWriter[LogstashRecord](cache, Map("service" -> "test"))
    logger.withHandler(writer = logstashEventWriter).replace()
    logger.info("omg this is crazy")
    val output = cache.output.head.plainText
    val json = parse(output).flatMap(_.as[LogstashRecord])

    inside(json) { case Right(event) =>
      event.service shouldBe Some("test")
      event.level shouldBe "INFO"
    }
  }
}
