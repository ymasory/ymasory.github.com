import java.io._
import scala.io._

object GenerateHtml {

  val MetaFilename = ".flashcardset"
  val UrlPrefix = "http://github.com/ymasory/PublicFlashcards/raw/master/"
  val FlashupExt = "flashup"
  val PdfDir = "bin"
  val PdfExt = "pdf"
  val Sep = "/"
  val FrontsSuffix = "-fronts"
  val BacksSuffix= "-backs"
  val FlashcardsDir = "PublicFlashcards"
  val PartialFile = "flashcards.html.part"

  def main(args: Array[String]) {
    val fileSorter = {(fst: File, snd: File) => fst.getName < snd.getName}
    val pwd = new File(System.getenv().get("ymas") + Sep + FlashcardsDir)
    val subdirs = pwd.listFiles.filter(file => file.isDirectory && file.isHidden == false) sortWith fileSorter
    val metaPred = {(file: File) => new File(file, MetaFilename).exists}
    val (metas, nots) = subdirs partition metaPred
    if (nots.isEmpty == false) {
      System.err println "no meta file found for: "
      nots foreach System.err.println
      exit(1)
    }

    val buf = new StringBuffer
    buf.append("<table id=\"allflashcards\" border=\"1\" cellpadding=\"10\">")
    buf.append("""
<tr> 
<th><b>Deck</b></th> 
<th><b>3"x5"</b></th> 
<th><b>3"x5" Fronts</b></th> 
<th><b>3"x5" Backs</b></th> 
<th><b>Source</b></th> 
</tr>
""".trim)

    for {dir <- metas
         deck <- dir.listFiles sortWith fileSorter
         name = deck.getName
         if name.endsWith("." + FlashupExt)} {
      val lines = Source.fromFile(new File(dir, MetaFilename)).getLines.toList
      val title = lines(0)
      val url = if (lines.length > 1) Some(lines(1)) else None
      val basename = name.split("\\.").head

      buf.append("<tr>")
      val href = url match {
        case Some(str) => " href=\"" + str + "\""
        case None => ""
      }
      buf.append("<td class=\"flashcardfile\"><a" + href + ">" + title + "</a></td>")
      def printLink(in: String) {
        buf.append("<td><a href=\"" + UrlPrefix + dir.getName + Sep + in + "\">link</a></td>")
      }
      printLink(PdfDir + Sep + basename + "." + PdfExt)
      printLink(PdfDir + Sep + basename + FrontsSuffix + "." + PdfExt)
      printLink(PdfDir + Sep + basename + BacksSuffix + "." + PdfExt)
      printLink(basename + "." + FlashupExt)
      buf.append("</tr>")
    }

    buf.append("</table>")

    val tableHtml = buf.toString

    val partialHtml = Source.fromFile(PartialFile).getLines.mkString
    val fullHtml = partialHtml.replaceAll("INSERT TABLE HERE", tableHtml)
    println(fullHtml)
  }
}
