package scribe.json

import scribe.LogRecord
import scribe.output.{LogOutput, TextOutput}
import scribe.output.format.OutputFormat
import scribe.writer.Writer

package object extendable {

  trait StructuralLoggableEvent[T] {
    def apply(record: LogRecord, additional: Map[String, String]): T
  }

  trait LogRecordJsonEncoder[T] {

    implicit val struct: StructuralLoggableEvent[T]

    def encode(record: LogRecord, additional: Map[String, String]): String
  }
  class ExtendableJsonWriter[T](writer: Writer, additional: Map[String, String])(implicit
    encoder: LogRecordJsonEncoder[T]
  ) extends Writer {
    def write(record: LogRecord, output: LogOutput, outputFormat: OutputFormat): Unit = {
      val jsonString = encoder.encode(record, additional)
      writer.write(record, new TextOutput(jsonString), outputFormat)
    }
  }
}
