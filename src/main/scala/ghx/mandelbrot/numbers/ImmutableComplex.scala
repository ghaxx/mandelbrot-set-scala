package ghx.mandelbrot.numbers

class ImmutableComplex(val r: Double, val i: Double) {
  def +(c: ImmutableComplex): ImmutableComplex = {
        new ImmutableComplex(r + c.r, i + c.i)
  }
  def sq: ImmutableComplex = {
      new ImmutableComplex(r * r - i * i, 2 * r * i)
  }

  def >=(c: ImmutableComplex): Boolean = module >= c.module

  //    def module = Math.sqrt(r * r + i * i)
  def module = Math.hypot(r, i)

  override def toString = s"Complex($r, $i)"
}