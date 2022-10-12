package scribe.json.fabric

import fabric.rw._
import fabric.io.JsonFormatter
import scribe.json.ext._
import scribe.LogRecord
trait Encoder {
  implicit def fabricLogRecordJsonEncoder[T](implicit
                                             rw: RW[T],
                                             _struct: StructuralLoggableEvent[T]
                                            ): LogRecordJsonEncoder[T] = new LogRecordJsonEncoder[T] {
    implicit val struct: StructuralLoggableEvent[T] = _struct

    def encode(record: LogRecord, additional: Map[String, String]): String =
      JsonFormatter.Compact(struct(record, additional).json.merge(additional.json))
  }
}
