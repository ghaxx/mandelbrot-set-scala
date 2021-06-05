package ghx.mandelbrot.numbers

class MutableComplex(var r: Double, var i: Double) {
  def +(c: MutableComplex): MutableComplex = {
    r += c.r
    i += c.i
    this
  }
  def sq: MutableComplex = {
    val nr = r * r - i * i
    val ni = 2 * r * i
    r = nr
    i = ni
    this
  }

  def >=(c: MutableComplex): Boolean = module >= c.module

  def module = Math.hypot(r, i)

  override def toString = s"Complex($r, $i)"
}