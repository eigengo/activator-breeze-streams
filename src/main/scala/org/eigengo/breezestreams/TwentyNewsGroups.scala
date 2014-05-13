package org.eigengo.breezestreams

import nak.core.{FeaturizedClassifier, IndexedClassifier}
import scala.util.{Success, Failure}
import akka.stream.{FlowMaterializer, MaterializerSettings}
import akka.stream.scaladsl.Flow
import akka.actor.ActorSystem
import scala.io.{Codec, Source}

object TwentyNewsGroups {

  import nak.NakContext._

  def main(args: Array[String]) {
    val classifier = loadClassifierFromResource[IndexedClassifier[String] with FeaturizedClassifier[String, String]]("/20news.classify")

    implicit val system = ActorSystem("Sys")

    val source = Source.fromURI(getClass.getResource("/20news-test.txt").toURI)(Codec.ISO8859)
    Flow(source.getLines()).
      // transform
      map(line => line.toUpperCase).
      // print to console (can also use ``foreach(println)``)
      foreach(transformedLine => println(classifier.predict(transformedLine))).
      onComplete(FlowMaterializer(MaterializerSettings())) {
        case Success(_) => system.shutdown()
        case Failure(e) =>
          println("Failure: " + e.getMessage)
          system.shutdown()
    }
  }

}
