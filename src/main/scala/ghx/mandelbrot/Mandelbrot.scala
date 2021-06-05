package ghx.mandelbrot

import ghx.mandelbrot.numbers.MutableComplex
import ghx.mandelbrot.ui.MandelbrotSettings

import java.util.concurrent.{CountDownLatch, Executors}
import scala.concurrent.{ExecutionContext, Future}

object Mandelbrot {

  type T = Double

  def calculateByComparison(iterations: Int, compValue: T)(p: (T, T)): Boolean = {
    val z0 = new MutableComplex(0, 0)
    var z = new MutableComplex(0, 0)
    var i = 0
    val c = p.asComplex
    val complex = new MutableComplex(compValue, compValue).module
    while (i < iterations && z.module <= complex) {
      z = z.sq + c
      i = i + 1
    }
    val module = z.module
//    val r = module.isInfinite || module >= complex
    val r = module >= complex

    //    if (p._1 < 0.1 && p._2 <  0.1 && p._1 > -0.1 && p._2 > -0.1) {
    //      print(s"$p -> $z > $complex = $r, ")
    //    }
    !r
  }

  def calculateValues(iterations: Int, compValue: T)(p: (T, T)): MutableComplex = {
    val z0 = new MutableComplex(0, 0)
    var z = new MutableComplex(0, 0)
    var i = 0
    val c = p.asComplex
    val complex = new MutableComplex(compValue, compValue).module
//    while (i < iterations && (!c.r.isInfinite && !c.i.isInfinite)) {
    while (i < iterations) {
      z = z.sq + c
      i = i + 1
    }
    z
  }

  def calculateIterations(iterations: Int, compValue: T)(p: (T, T)): Int = {
    var z = p.asComplex
    var i = 0
    val c = p.asComplex
//    while (i < iterations && (!c.r.isInfinite && !c.i.isInfinite)) {
    while (i < iterations && z.module < compValue) {
      z = z.sq + c
      i = i + 1
    }
    i
  }
  def translate(p: (T, T), t: (T, T)) =
    (p._1.toFloat + t._1, p._2.toFloat + t._2)

  def canvasToPosition(canvasPos: (Int, Int), canvasOffset: (Double, Double), translate: (Double, Double), scale: Double): (Double, Double) =
    (
      scale * (canvasPos._1 - canvasOffset._1) + translate._1,
      scale * (canvasPos._2 - canvasOffset._2) + translate._2
    )

  implicit class TupleToComplex(p: (T, T)) {
    def asComplex = new MutableComplex(p._1, p._2)
  }

  val threads = 16
  val ec = ExecutionContext.fromExecutor(Executors.newWorkStealingPool(threads))


  var p = Array.ofDim[((Int, Int), Int)](0)

  def calculateParalelly(w: Int, h: Int): Array[((Int, Int), Int)] = {
    val i = MandelbrotSettings.iterations.get().toInt
    val c = MandelbrotSettings.compValue.get()
    val _tx = MandelbrotSettings.x.get()
    val _ty = MandelbrotSettings.y.get()
    val s = MandelbrotSettings.scale.get().floatValue()
    val axisRatio = MandelbrotSettings.scale.get() / w
    val dw = w / 2
    val dh = h / 2
//    println(s"Translating by (-$dw, -$dh), scaling by $axisRatio")
//    val timer = InfluxInstantReporter.startTimer("calculations")
    val startTime = System.currentTimeMillis()

    val l = new CountDownLatch(threads)
    val partSize = h / threads

    val cThreads = (0 to threads).map { j =>
      Future{
//        println(s"Doing part ${j}")
        try {
          (0 to w).foreach { x0 =>
            (j to h by threads).foreach { y0 =>
              if (y0 < h) {
                val (x, y) = Mandelbrot.translate(((x0 - dw) * axisRatio, (y0 - dh) * axisRatio), (_tx, _ty))
                val m = Mandelbrot.calculateIterations(i, c)((x, y))
                p(x0 + y0 * w) = (((x0, y0), m))
              }
            }
          }
        } catch {
          case t: Throwable =>
//            println(t.getMessage)
        }
//        println("""l.getCount = """ + l.getCount)
        l.countDown()
      }(ec)
    }
    l.await()
//    println(s"Calculations took ${System.currentTimeMillis() - startTime}")

//    timer.storAndReport(Map(), Map("iterations" -> i, "comparison" -> c))
    p
  }

}
