package ghx.mandelbrot.ui.painter.verbose

import ghx.mandelbrot.Mandelbrot
import ghx.mandelbrot.ui.MandelbrotSettings
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import kamon.Kamon
import kamon.tag.TagSet

import scala.collection.immutable
import scala.concurrent.Promise

class CanvasPointsPainter(val canvas: Canvas, val settings: MandelbrotSettings.type) extends Painter {
  case class PointOnCanvas(x: Int, y: Int)

  case class TranslatedPointCoords(x: Double, y: Double)

  var canvasToReal: Map[PointOnCanvas, TranslatedPointCoords] = _

  var points: immutable.Seq[PointOnCanvas] = _
  var pointsOffset: (Int, Int) = _

  def remapPoints() = {
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

  def drawOnCanvas(): SimpleObjectProperty[Unit] = {
    import scala.collection.parallel.CollectionConverters._

    val timer = Kamon.timer("calculation-time").withoutTags().start()

    val i = MandelbrotSettings.iterations.get().toInt
    val c = MandelbrotSettings.compValue.get()
    val colors = canvasToReal.par.map {
      case (onCanvas, realPoint) =>
        val m = Mandelbrot.calculateIterations(i, c)((realPoint.x, realPoint.y))
        //        canvas.getGraphicsContext2D.strokeRect(x0, y0, 1, 1)
        val color = new Color(1.0 * m / i, 1.0 * m / i, 1.0 * m / i, 1)
        onCanvas -> color
    }
    val p = new SimpleObjectProperty[Unit]
    javafx.application.Platform.runLater(() => {
      colors.seq.foreach {
        case (PointOnCanvas(x, y), color) =>
          canvas.getGraphicsContext2D.getPixelWriter.setColor(x, y, color)
      }
      p.setValue(())
      timer
        .withTags(TagSet.from(Map(
          "engine" -> "parallel-array-verbose",
          "iterations" -> i.longValue(),
          "comparison" -> c.longValue(),
          "canvas-height" -> canvas.heightProperty().intValue().longValue(),
          "canvas-width" -> canvas.widthProperty().intValue().longValue()))
        ).stop()
    })
    p
  }

  private def rescalePoint(pointOnCanvas: PointOnCanvas, translate: (Double, Double), scale: Double): TranslatedPointCoords = {
    TranslatedPointCoords(
      scale * (pointOnCanvas.x - pointsOffset._1) + translate._1,
      scale * (pointOnCanvas.y - pointsOffset._2) + translate._2
    )
  }
}
