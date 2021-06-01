package ghx.mandelbrot

import ghx.mandelbrot.{Mandelbrot, MandelbrotSettings}
import javafx.beans.Observable
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas
import javafx.scene.control.{ProgressIndicator, Slider, TextField, Toggle, ToggleGroup}
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import scala.collection.parallel.CollectionConverters._
//import kamon.Kamon

import scala.collection.immutable

class MandelbrotPanel {

  @FXML
  var canvas: Canvas = _
  @FXML
  var xField: TextField = _
  @FXML
  var xSlider: Slider = _
  @FXML
  var yField: TextField = _
  @FXML
  var ySlider: Slider = _
  @FXML
  var scaleField: TextField = _
  @FXML
  var scaleSlider: Slider = _
  @FXML
  var iterationsField: TextField = _
  @FXML
  var iterationsSlider: Slider = _
  @FXML
  var comparisonField: TextField = _
  @FXML
  var comparisonSlider: Slider = _
  @FXML
  var colorScaleField: TextField = _
  @FXML
  var colorScaleSlider: Slider = _
  @FXML
  var progress: ProgressIndicator = _
  @FXML
  var mode: ToggleGroup = _

  val xProp = new SimpleDoubleProperty(10)
  var points: immutable.Seq[(Int, Int)] = _

  var draw: () => Any = drawWithParArray

  def initialize() = {
    colorScaleSlider.valueProperty.setValue(1000)
    xSlider.valueProperty().bindBidirectional(MandelbrotSettings.x)
    ySlider.valueProperty().bindBidirectional(MandelbrotSettings.y)
    scaleSlider.valueProperty().bindBidirectional(MandelbrotSettings.scale)
    iterationsSlider.valueProperty().bindBidirectional(MandelbrotSettings.iterations)
    comparisonSlider.valueProperty().bindBidirectional(MandelbrotSettings.compValue)

//    xSlider.minProperty().bind(xSlider.valueProperty().subtract(scaleSlider.valueProperty()))
//    xSlider.maxProperty().bind(xSlider.valueProperty().add(scaleSlider.valueProperty()))
//    ySlider.minProperty().bind(ySlider.valueProperty().subtract(scaleSlider.valueProperty()))
//    ySlider.maxProperty().bind(ySlider.valueProperty().add(scaleSlider.valueProperty()))

    DoBindings.bind(xField.textProperty(), xSlider.valueProperty())
    DoBindings.bind(yField.textProperty(), ySlider.valueProperty())
    DoBindings.bind(scaleField.textProperty(), scaleSlider.valueProperty())
    DoBindings.bind(iterationsField.textProperty(), iterationsSlider.valueProperty())
    DoBindings.bind(comparisonField.textProperty(), comparisonSlider.valueProperty())
    DoBindings.bind(colorScaleField.textProperty(), colorScaleSlider.valueProperty())

    xSlider.valueProperty().addListener(new ChangeListener[Number] {
      def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = {
        draw()
      }

    })
    ySlider.valueProperty().addListener(new ChangeListener[Number] {
      def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = {
        draw()
      }

    })
    scaleSlider.valueProperty().addListener(new ChangeListener[Number] {
      def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = {
//        println(s"${xSlider.getValue()} - ${scaleSlider.getValue} = ${xSlider.getValue() - scaleSlider.getValue}")

        xSlider.minProperty().setValue(xSlider.getValue() - scaleSlider.getValue)
        xSlider.maxProperty().setValue(xSlider.getValue() + scaleSlider.getValue())
        ySlider.minProperty().setValue(ySlider.getValue() - scaleSlider.getValue())
        ySlider.maxProperty().setValue(ySlider.getValue() + scaleSlider.getValue())

        draw()
      }

    })
    iterationsSlider.valueProperty().addListener(new ChangeListener[Number] {
      def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = {
        draw()
      }
    })
    colorScaleSlider.valueProperty().addListener(new ChangeListener[Number] {
      def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = {
        draw()
      }
    })
    comparisonSlider.valueProperty().addListener(new ChangeListener[Number] {
      def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = {
        draw()
      }
    })

    mode.selectedToggleProperty().addListener(new ChangeListener[Toggle] {

      def changed(observableValue: ObservableValue[_ <: Toggle], t: Toggle, t1: Toggle): Unit = {
        t1.getUserData match {
          case "parArray" => draw = drawWithParArray
          case "threads" => draw = drawWithExecutionPool
        }
        draw()
      }
    })

    def f = {
//      println("f")
      remapPoints()
      draw()
    }
    canvas.widthProperty().addListener((evt: Observable) => f)
    canvas.heightProperty.addListener((evt: Observable) => f)
  }

  val black = new Color(0, 0, 0, 1)

  def remapPoints() = {
    Mandelbrot.p = Array.ofDim[((Int, Int), Int)](canvas.getWidth.intValue() * canvas.getHeight.intValue())
    points =
      for {
        x <- 0 to canvas.getWidth.intValue()
        y <- 0 to canvas.getHeight.intValue()
      } yield {
        (x, y)
      }
  }

  def drawWithParArray(): Any = {
//    println("draw1")
    progress.setVisible(true)
    javafx.application.Platform.runLater(() => {
      val i = MandelbrotSettings.iterations.get().toInt
      val c = MandelbrotSettings.compValue.get()
      val _tx = MandelbrotSettings.x.get()
      val _ty = MandelbrotSettings.y.get()
      val axisRatio = MandelbrotSettings.scale.get() / canvas.getWidth
      val dw = canvas.getWidth / 2
      val dh = canvas.getHeight / 2
      val timer = InfluxInstantReporter.startTimer("calculations")
      val matchingPoints = points.par.map { p =>
        val (x0, y0) = p
        val (x, y) = Mandelbrot.canvasToPosition((x0, y0), (dw, dh), (_tx, _ty), axisRatio)
        val m = Mandelbrot.calculateIterations(i, c)((x, y))
        ((x0, y0), m)
      }.toArray
//      println(s"Calculations took ${System.currentTimeMillis() - startTime}")
      timer.storeAndReport(Map("engine" -> "parallel-array"), Map("iterations" -> i, "comparison" -> c, "canvas-height" -> dh, "canvas-width" -> dw))
      matchingPoints.foreach {
        case ((x0, y0), m) =>
          //        canvas.getGraphicsContext2D.strokeRect(x0, y0, 1, 1)
          val color = new Color(1.0 * m / i, 1.0 * m / i, 1.0 * m / i, 1)
          canvas.getGraphicsContext2D.getPixelWriter.setColor(x0, y0, color)
      }
//      Kamon.counter("created-sets").withTag("engine", "parallel array").increment()

      progress.setVisible(false)
    })
  }

  def drawWithExecutionPool(): Any = {
//    println("draw4")
    progress.setVisible(true)
    javafx.application.Platform.runLater(() => {
      val i = MandelbrotSettings.iterations.get().toInt
      val timer = InfluxInstantReporter.startTimer("calculations")
      Mandelbrot.calculateParalelly(canvas.getWidth.intValue(), canvas.getHeight.intValue()).foreach {
        case ((x0, y0), m) =>
          //        canvas.getGraphicsContext2D.strokeRect(x0, y0, 1, 1)
          val color = new Color(1.0 * m / i, 1.0 * m / i, 1.0 * m / i, 1)
          canvas.getGraphicsContext2D.getPixelWriter.setColor(x0, y0, color)
      }
//      timer.storeAndReport(Map("engine" -> "parallel-array"), Map("iterations" -> i, "comparison" -> c, "canvas-height" -> dh, "canvas-width" -> dw))
//      Kamon.counter("created-sets").withTag("engine", "execution pool").increment()

      progress.setVisible(false)
    })
  }

  def draw2() = {
    progress.setVisible(true)
    javafx.application.Platform.runLater(() => {
      val i = MandelbrotSettings.iterations.get().toInt
      val c = MandelbrotSettings.compValue.get()
      val _tx = MandelbrotSettings.x.get()
      val _ty = MandelbrotSettings.y.get()
      val s = MandelbrotSettings.scale.get().floatValue()
      val axisRatio = MandelbrotSettings.scale.get() / canvas.getWidth
      val dw = canvas.getWidth / 2
      val dh = canvas.getHeight / 2
//      println(s"Translating by (-$dw, -$dh), scaling by $axisRatio")
      var max = 0.0
      val matchingPoints = points.par.map { p =>
        val (x0, y0) = p
        val (x, y) = Mandelbrot.translate(((x0 - dw) * axisRatio, (y0 - dh) * axisRatio), (_tx, _ty))
        val m = Mandelbrot.calculateValues(i, c)((x, y)).module
        if (m > max) max = Math.min(m, c)
        ((x0, y0), m)
      }.toArray
      matchingPoints.foreach {
        case ((x0, y0), m) =>
          //        canvas.getGraphicsContext2D.strokeRect(x0, y0, 1, 1)
          val color = new Color(Math.min(1.0, 1.0 * m / max), Math.min(1.0, 1.0 * m / max), Math.min(1.0, 1.0 * m / max), 1)
          canvas.getGraphicsContext2D.getPixelWriter.setColor(x0, y0, color)
      }

      progress.setVisible(false)
    })
  }

  def clear() = {
    canvas.getGraphicsContext2D.clearRect(0, 0, canvas.getWidth.intValue(), canvas.getHeight.intValue())
    canvas.getGraphicsContext2D.setStroke(black)
    canvas.getGraphicsContext2D.setLineWidth(2)
    canvas.getGraphicsContext2D.strokeRect(1, 1, canvas.getWidth.intValue() - 1, canvas.getHeight.intValue() - 1)
    canvas.getGraphicsContext2D.setStroke(new Color(0.4, 0.3, 0.3, 1))
    canvas.getGraphicsContext2D.setLineWidth(1)
    canvas.getGraphicsContext2D.strokeLine(0, canvas.getHeight.intValue() / 2, canvas.getWidth.intValue(), canvas.getHeight.intValue() / 2)
    canvas.getGraphicsContext2D.setStroke(black)
    canvas.getGraphicsContext2D.setLineWidth(1)
//    println("Clearing")
  }


}
