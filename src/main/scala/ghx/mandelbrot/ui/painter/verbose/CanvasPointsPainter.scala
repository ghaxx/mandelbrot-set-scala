package ghx.mandelbrot.ui.painter.verbose

import ghx.mandelbrot.Mandelbrot
import ghx.mandelbrot.ui.MandelbrotSettings
import javafx.scene.canvas.Canvas
import javafx.scene.image.PixelWriter
import javafx.scene.paint.Color
import kamon.Kamon
import kamon.tag.TagSet

import scala.collection.immutable

class CanvasPointsPainter(val canvas: Canvas, val settings: MandelbrotSettings.type) {
  case class PointOnCanvas(x: Int, y: Int)

  case class TranslatedPointCoords(x: Double, y: Double)

  var canvasToReal: Map[PointOnCanvas, TranslatedPointCoords] = _
  var alg: Any = _

  var points: immutable.Seq[PointOnCanvas] = _
  var pointsOffset: (Int, Int) = _

  def mapPoints() = {
    val w = canvas.widthProperty().intValue()
    val h = canvas.heightProperty().intValue()
    pointsOffset = ((w / 2).intValue(), (h / 2).intValue())
    Mandelbrot.p = Array.ofDim[((Int, Int), Int)](w * h)
    points =
      for {
        x <- 0 to w
        y <- 0 to h
      } yield {
        PointOnCanvas(x, y)
      }
    val _tx = MandelbrotSettings.x.get()
    val _ty = MandelbrotSettings.y.get()
    val axisRatio = MandelbrotSettings.scale.get() / canvas.getWidth
    canvasToReal = points.map(x => x -> rescalePoint(x, (_tx, _ty), axisRatio)).toMap
  }

  def getCalculatedColors(): Map[(Int, Int), Color] = {
    import scala.collection.parallel.CollectionConverters._

    val timer = Kamon.timer("calculation-time").withoutTags().start()

    val i = MandelbrotSettings.iterations.get().toInt
    val c = MandelbrotSettings.compValue.get()
    val colors = canvasToReal.par.map {
        case (onCanvas, realPoint) =>
          val m = Mandelbrot.calculateIterations(i, c)((realPoint.x, realPoint.y))
          //        canvas.getGraphicsContext2D.strokeRect(x0, y0, 1, 1)
          val color = new Color(1.0 * m / i, 1.0 * m / i, 1.0 * m / i, 1)
          (onCanvas.x, onCanvas.y) -> color
      }.seq
    timer
      .withTags(TagSet.from(Map(
        "engine" -> "single-thread",
        "iterations" -> i.longValue(),
        "comparison" -> c.longValue(),
        "canvas-height" -> canvas.heightProperty().intValue().longValue(),
        "canvas-width" -> canvas.widthProperty().intValue().longValue()))
      ).stop()
    colors
  }

  def drawOnCanvas(writer: PixelWriter): Unit = {
    import scala.collection.parallel.CollectionConverters._

    val timer = Kamon.timer("calculation-time").withoutTags().start()

    val i = MandelbrotSettings.iterations.get().toInt
    val c = MandelbrotSettings.compValue.get()
    canvasToReal.foreach {
      case (onCanvas, realPoint) =>
        val m = Mandelbrot.calculateIterations(i, c)((realPoint.x, realPoint.y))
        //        canvas.getGraphicsContext2D.strokeRect(x0, y0, 1, 1)
        val color = new Color(1.0 * m / i, 1.0 * m / i, 1.0 * m / i, 1)
        writer.setColor(onCanvas.x, onCanvas.y, color)
    }
    timer
      .withTags(TagSet.from(Map(
        "engine" -> "single-thread",
        "iterations" -> i.longValue(),
        "comparison" -> c.longValue(),
        "canvas-height" -> canvas.heightProperty().intValue().longValue(),
        "canvas-width" -> canvas.widthProperty().intValue().longValue()))
      ).stop()
  }

  private def rescalePoint(pointOnCanvas: PointOnCanvas, translate: (Double, Double), scale: Double): TranslatedPointCoords = {
    TranslatedPointCoords(
      scale * (pointOnCanvas.x - pointsOffset._1) + translate._1,
      scale * (pointOnCanvas.y - pointsOffset._2) + translate._2
    )
  }
}
