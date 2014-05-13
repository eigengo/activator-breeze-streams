package org.eigengo.breezestreams

import java.io.InputStream
import java.util.zip.ZipEntry
import scala.io.{Codec, Source}

object TwentyNewsGroupsTrain {

  import nak.NakContext._
  import nak.data._
  import nak.liblinear.LiblinearConfig

  import java.io.File

  def main(args: Array[String]) {
    def fromLabelled(top: String)(entry: ZipEntry, contents: InputStream): Option[Example[String, String]] = {
      if (entry.isDirectory) None
      else if (!entry.getName.startsWith(top)) None
      else {
        // this is the file name in form of ``20news-bydate-train/alt.atheism/45800``
        val i = entry.getName.indexOf('/')
        val j = entry.getName.lastIndexOf('/')
        val label = entry.getName.substring(i + 1, j)
        val text = Source.fromInputStream(contents)(Codec.ISO8859)

        Some(Example(label, text.mkString, entry.getName))
      }
    }
    // Example stopword set (you should use a more extensive list for actual classifiers).
    val stopwords = Set("the","a","an","of","in","for","by","on")

    // Train
    print("Training... ")
    val zipFile = new ZipArchive(new File(getClass.getResource("/20news-bydate-train.zip").toURI))
    val trainingExamples = zipFile.flatMap(fromLabelled("20news-bydate-train"))
    val config = LiblinearConfig(cost = 5.0, eps = 0.01)
    val featurizer = new BowFeaturizer(stopwords)
    val classifier = trainClassifierHashed(config, featurizer, trainingExamples, 50000)

    saveClassifier(classifier, "20news.classify")
    println("done.")
  }

}
