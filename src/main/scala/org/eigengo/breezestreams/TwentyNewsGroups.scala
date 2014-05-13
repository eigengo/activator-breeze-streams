package org.eigengo.breezestreams

import nak.core.{FeaturizedClassifier, IndexedClassifier}
import scala.util.{Success, Failure}
import akka.stream.{FlowMaterializer, MaterializerSettings}
import akka.stream.scaladsl.Flow
import akka.actor.ActorSystem
import scala.io.{Codec, Source}

object TwentyNewsGroups {

  import nak.NakContext._

  case class Message(from: String, subject: String, text: String)
  object Message {

    def apply(line: String): Message = {
      val headerIndex = line.indexOf("\\n\\n")
      val header = line.substring(0, headerIndex)

      val text = line.substring(headerIndex + 1)
      Message("", "", text)
    }
  }

  def main(args: Array[String]) {
    val classifier = loadClassifierFromResource[IndexedClassifier[String] with FeaturizedClassifier[String, String]]("/20news.classify")

    implicit val system = ActorSystem("Sys")

    val source = Source.fromURI(getClass.getResource("/20news-test.txt").toURI)(Codec.ISO8859)
    Flow(source.getLines()).
      // transform
      map(Message.apply).
      // print to console (can also use ``foreach(println)``)
      foreach(message => println(classifier.predict(message.text))).
      onComplete(FlowMaterializer(MaterializerSettings())) {
        case Success(_) => system.shutdown()
        case Failure(e) =>
          println("Failure: " + e.getMessage)
          system.shutdown()
    }
  }

}
