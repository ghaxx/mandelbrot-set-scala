package ghx.mandelbrot

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class WTest extends AnyFreeSpec with Matchers {

  "It should calculate m" in {
    (1 to 10).foreach { i =>
      val m = Mandelbrot.calculateByComparison(i, 1000)((1, 0))
      println(m)
    }
  }

}
