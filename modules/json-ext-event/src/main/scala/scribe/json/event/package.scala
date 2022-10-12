package scribe.json

import scribe._
import scribe.data.MDC
import scribe.json.ext._
import scribe.message.Message

import java.time.{Instant, OffsetDateTime, ZoneId}
package object event {

  // port from https://github.com/outr/scribe/blob/master/logstash/src/main/scala/scribe/logstash/LogstashRecord.scala
  case class LogstashRecord(messages: List[String],
                            service: Option[String],
                            level: String,
                            value: Double,
                            fileName: String,
                            className: String,
                            methodName: Option[String],
                            line: Option[Int],
                            thread: String,
                            `@timestamp`: OffsetDateTime,
                            stack_trace: Option[String],
                            mdc: Map[String, String],
                            data: Map[String, String]
  )

  implicit def logstashRecordStructuralLoggableEvent: StructuralLoggableEvent[LogstashRecord] =
    (record: LogRecord, additional: Map[String, String]) => {
      val (traces, messages) = record.messages
        .partitionMap { case message: Message[_] =>
          if (message.value.isInstanceOf[Throwable]) Left(message.logOutput.plainText)
          else Right(message.logOutput.plainText)
        }
      val timestamp = OffsetDateTime.ofInstant(Instant.ofEpochMilli(record.timeStamp), ZoneId.of("UTC"))
      LogstashRecord(
        messages = messages,
        service = additional.get("service"),
        level = record.level.name,
        value = record.levelValue,
        fileName = record.fileName,
        className = record.className,
        methodName = record.methodName,
        line = record.line,
        thread = record.thread.getName,
        `@timestamp` = timestamp,
        stack_trace = if (traces.isEmpty) None else Some(traces.mkString(lineSeparator)),
        mdc = MDC.map.map { case (key, function) =>
          key -> function().toString
        },
        data = record.data.map { case (key, function) =>
          key -> function().toString
        }
      )
    }
}
