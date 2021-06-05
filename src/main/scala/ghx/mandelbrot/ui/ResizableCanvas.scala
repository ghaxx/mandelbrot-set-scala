package ghx.mandelbrot.ui

import javafx.scene.canvas.Canvas

class ResizableCanvas extends Canvas {
//  widthProperty().addListener((evt: Observable) => draw())
//  heightProperty.addListener((evt: Observable) => draw())
//
//  private def draw(): Unit = {
//    val width = getWidth
//    val height = getHeight
//    val gc = getGraphicsContext2D
//    gc.clearRect(0, 0, width, height)
//    gc.setStroke(Color.RED)
//    gc.strokeLine(0, 0, width, height)
//    gc.strokeLine(0, height, width, 0)
//  }

  override def isResizable = true

  override def prefWidth(height: Double): Double = getWidth

  override def prefHeight(width: Double): Double = getHeight
}
