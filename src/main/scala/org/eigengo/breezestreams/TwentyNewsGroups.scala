package org.eigengo.breezestreams

import nak.core.{FeaturizedClassifier, IndexedClassifier}
import scala.util.{Success, Failure}
import akka.stream.{FlowMaterializer, MaterializerSettings}
import akka.stream.scaladsl.Flow
import akka.actor.ActorSystem
import scala.io.{Codec, Source}

object TwentyNewsGroups {

  import nak.NakContext._

  case class Classified[L, V](label: L, value: V)
  case class Message(from: String, subject: String, text: String)
  object Message {

    def parse(line: String): Message = {
      val headerIndex = line.indexOf("\\n\\n")
      val header = line.substring(0, headerIndex)
      val headerElements = header.split("\\\\n").flatMap { e =>
        val i = e.indexOf(':')
        if (i != -1 && i + 2 < e.length) Some(e.substring(0, i) -> e.substring(i + 2)) else None
      }.toMap

      val text = line.substring(headerIndex + 3)
      Message(headerElements("From"), headerElements("Subject"), text)
    }
  }

  def main(args: Array[String]) {
    val classifier = loadClassifierFromResource[IndexedClassifier[String] with FeaturizedClassifier[String, String]]("/20news.classify")

    implicit val system = ActorSystem("Sys")

    val source = Source.fromURI(getClass.getResource("/20news-test.txt").toURI)(Codec.ISO8859)
    Flow(source.getLines()).
      // transform
      map(Message.parse).
      // group by topic
      map(message => Classified(classifier.predict(message.text), message)).
      foreach(cm => println(cm.label)).
      onComplete(FlowMaterializer(MaterializerSettings())) {
        case Success(_) => system.shutdown()
        case Failure(e) =>
          println("Failure: " + e.getMessage)
          system.shutdown()
    }
  }

}
