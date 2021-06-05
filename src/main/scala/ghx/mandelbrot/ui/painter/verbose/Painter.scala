package ghx.mandelbrot.ui.painter.verbose

trait Painter {

  def remapPoints(): Unit
  def drawOnCanvas(): Unit

}
