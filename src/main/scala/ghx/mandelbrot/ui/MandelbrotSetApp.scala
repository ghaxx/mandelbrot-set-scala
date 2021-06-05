package ghx.mandelbrot.ui

import ghx.mandelbrot.ui.monitoring.InfluxInstantReporter
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import kamon.Kamon
//import kamon.Kamon

class MandelbrotSetApp extends Application {

  override def start(stage: Stage): Unit = {
    val root: AnchorPane = FXMLLoader.load(getClass.getResource("MandelbrotPanel.fxml"))
    val scene = new Scene(root)
    stage.setTitle("Mandelbrot Set")
    stage.setScene(scene)
    stage.show()
  }


  def __launch(args: Array[String]) = {
    Application.launch(args: _*)
  }
}

object MandelbrotSetApp extends App {
  var stage: Stage = null
  Kamon.loadModules()
  InfluxInstantReporter.init()
//  Application.launch(classOf[MandelbrotSetApp], args: _*)

  new MandelbrotSetApp().__launch(args: Array[String])
}