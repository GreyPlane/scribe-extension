package scribe.json.ext

import scribe.Logging
import scribe.writer.CacheWriter

trait JsonWriterSuite extends Logging {
  val cache = new CacheWriter
}
