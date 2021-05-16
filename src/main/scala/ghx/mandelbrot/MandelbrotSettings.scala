package ghx.mandelbrot

import javafx.beans.property.SimpleDoubleProperty

object MandelbrotSettings {
  var x = new SimpleDoubleProperty(-0.72)
  var y = new SimpleDoubleProperty(0)
  var scale = new SimpleDoubleProperty(3.4)
  var iterations = new SimpleDoubleProperty(70)
  var compValue = new SimpleDoubleProperty(3)
}
