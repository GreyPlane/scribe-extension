package scribe.json

import io.circe.Encoder
import scribe.LogRecord
import scribe.json.extendable._
package object circe {
  implicit def circeLogRecordJsonEncoder[T](implicit
    encoder: Encoder[T],
    _struct: StructuralLoggableEvent[T]
  ): LogRecordJsonEncoder[T] =
    new LogRecordJsonEncoder[T] {
      implicit val struct: StructuralLoggableEvent[T] = _struct

      def encode(record: LogRecord, additional: Map[String, String]): String =
        encoder(struct(record, additional)).noSpaces

    }
}
