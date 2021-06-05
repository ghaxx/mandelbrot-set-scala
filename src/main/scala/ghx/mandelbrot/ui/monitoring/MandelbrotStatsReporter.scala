package ghx.mandelbrot.ui.monitoring

trait MandelbrotStatsReporter {
  def init(): Unit
  def startTimer(): StartedTimer

  trait Timer {
    def start(): StartedTimer
  }

  trait StartedTimer {
    def stop(): Unit
  }

}

