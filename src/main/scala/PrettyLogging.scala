import Console._
import org.apache.logging.log4j.LogManager


 trait PrettyLogging {
  /*
    Fun times with console output!
  */
  private val logger = LogManager.getLogger(classOf[PrettyLogging])
  def prettyLogger(msg: String): Unit = logger.info(msg)
  def boxedLogger(msg: String): Unit = prettyLogger(box(msg))

  private def c: String => String => String = (color: String) => (s: String) => s"$color$s${RESET}"

  def yellow: String => String = c(YELLOW)
  def magenta: String => String = c(MAGENTA)

  def box(s: String, color: String = GREEN, boxChar: Char = '='): String = {
    val maxLineLength = s
      // replace all color control characters to avoid accidental incorrect lengths
      .replaceAll("\\u001B\\[\\d+m", "")
      // find the max line length!
      .split("\n")
      .max
      .length

    // Make a line of `boxChar` the length of the maximum line length
    val line = s"$color${"".padTo(maxLineLength + 2, boxChar)}${RESET}"

    // Put it all back together, inserting leading space to "indent" provided content.
    s"""$line
       |${s.split("\n").map(" " + _).mkString("\n")}
       |$line""".stripMargin
  }
}
