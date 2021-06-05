package ghx.mandelbrot.ui.painter.verbose

import ghx.mandelbrot.Mandelbrot
import ghx.mandelbrot.ui.MandelbrotSettings
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import kamon.Kamon
import kamon.tag.TagSet

import scala.collection.immutable

class CanvasQuickerPointsPainter(val canvas: Canvas, val settings: MandelbrotSettings.type) extends Painter {
  var points: immutable.Seq[(Int, Int)] = _
  var canvasToReal: Seq[((Int, Int), (Double, Double))] = _
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
        (x, y)
      }
    val _tx = MandelbrotSettings.x.get()
    val _ty = MandelbrotSettings.y.get()
    val axisRatio = MandelbrotSettings.scale.get() / canvas.getWidth
    canvasToReal = points.map(x => x -> rescalePoint(x, (_tx, _ty), axisRatio))
  }

  def drawOnCanvas(): Unit = {
    import scala.collection.parallel.CollectionConverters._

    val timer = Kamon.timer("calculation-time").withoutTags().start()

    val i = MandelbrotSettings.iterations.get().toInt
    val c = MandelbrotSettings.compValue.get()
    val colors = canvasToReal.par.map {
      case (onCanvas, realPoint) =>
        val m = Mandelbrot.calculateIterations(i, c)((realPoint._1, realPoint._2))
        //        canvas.getGraphicsContext2D.strokeRect(x0, y0, 1, 1)
        val color = new Color(1.0 * m / i, 1.0 * m / i, 1.0 * m / i, 1)
        onCanvas -> color
    }
    javafx.application.Platform.runLater(() => {
      colors.seq.foreach {
        case ((x, y), color) =>
          canvas.getGraphicsContext2D.getPixelWriter.setColor(x, y, color)
      }
      timer
        .withTags(TagSet.from(Map(
          "engine" -> "parallel-array-quicker",
          "iterations" -> i.longValue(),
          "comparison" -> c.longValue(),
          "canvas-height" -> canvas.heightProperty().intValue().longValue(),
          "canvas-width" -> canvas.widthProperty().intValue().longValue()))
        ).stop()
    })
  }

  private def rescalePoint(pointOnCanvas: (Int, Int), translate: (Double, Double), scale: Double): (Double, Double) = {
    (
      scale * (pointOnCanvas._1 - pointsOffset._1) + translate._1,
      scale * (pointOnCanvas._2 - pointsOffset._2) + translate._2
    )
  }
}
