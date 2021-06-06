package ghx.mandelbrot.ui.painter.verbose

import javafx.beans.property.SimpleObjectProperty

import scala.concurrent.Promise

trait Painter {

  def remapPoints(): Unit
  def drawOnCanvas(): SimpleObjectProperty[Unit]

}
