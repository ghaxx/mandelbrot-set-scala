package ghx.mandelbrot

import com.typesafe.config.ConfigFactory
import org.influxdb.dto.Point
import org.influxdb.{InfluxDB, InfluxDBFactory}

import java.util.concurrent.TimeUnit

object InfluxInstantReporter {
  val serverURL = "http://127.0.0.1:8086"
  val username = ""
  val password = ""

  val enabled = ConfigFactory.load().getBoolean("reporting.enabled")

  var influxDB: InfluxDB = null

  def init() = {
    if (enabled) {
      influxDB = InfluxDBFactory.connect(serverURL)
      influxDB.setDatabase("mandelbrot-set-stats")
    }
  }

  def startTimer(name: String): Timer = {
    if (enabled) {
      new InfluxTimer(name, influxDB)
    } else {
      DummyTimer
    }
  }
}

trait Timer {
  def storeAndReport(tags: Map[String, String], fields: Map[String, Double])
}

object DummyTimer extends Timer {
  def storeAndReport(tags: Map[String, String], fields: Map[String, Double]): Unit = {

  }
}

class InfluxTimer(name: String, influxDB: InfluxDB) extends Timer {
  val startMs = System.currentTimeMillis()

  def storeAndReport(tags: Map[String, String], fields: Map[String, Double]) = {
    val builder = Point.measurement(name)
      .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)

    tags.foreach {
      case (key, value) => builder.tag(key, value)
    }
    fields.foreach {
      case (key, value) => builder.addField(key, value)
    }
    builder.addField("duration", System.currentTimeMillis() - startMs)
    influxDB.write(builder
      .build());
  }
}