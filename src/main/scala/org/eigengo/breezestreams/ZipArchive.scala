package org.eigengo.breezestreams

import java.util.zip.{ZipEntry, ZipFile}
import java.io.{InputStream, File}

class ZipArchive(file: File) {
  val zipFile = new ZipFile(file, ZipFile.OPEN_READ)

  def flatMap[B](operation: (ZipEntry, InputStream) => Option[B]): List[B] = {
    import scala.collection.JavaConversions._
    val entries = zipFile.entries().toList
    entries.flatMap { entry =>
      val is = zipFile.getInputStream(entry)
      val result = operation(entry, is)
      is.close()

      result
    }
  }

}
