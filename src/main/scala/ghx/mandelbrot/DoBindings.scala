package ghx.mandelbrot

import javafx.beans.binding.Bindings
import javafx.beans.property.{DoubleProperty, StringProperty}
import javafx.util.StringConverter

object DoBindings {
  private[mandelbrot] def bind(s: StringProperty, v: DoubleProperty): Unit = {
    Bindings.bindBidirectional(s, v, new StringConverter[Number]() {
      override def fromString(s: String): Number = { //                        System.out.println("from string " + s);
        try  s.toDouble
        catch {
          case t: NumberFormatException => 0.0
        }
      }

      override def toString(n: Number): String = { //                        System.out.println("from number " + n);
        n match {
          case d: java.lang.Double if d.isInfinite => "Infinity"
          case x => x.toString
        }
      }
    })
  }
}
