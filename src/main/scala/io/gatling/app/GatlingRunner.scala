package io.gatling.app

import akka.actor.ActorSystem
import ch.qos.logback.classic.LoggerContext
import com.typesafe.scalalogging.StrictLogging

import io.gatling.core.config.GatlingConfiguration

import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.control.NonFatal

/**
  * Object containing entry point of application.
  *
  * The purpose is to get Gatling run result object after execution,
  * instead of execution status code.
  * https://github.com/gatling/gatling/blob/master/gatling-app/src/main/scala/io/gatling/app/Gatling.scala
  *
  * Gatling pull request: https://github.com/gatling/gatling/pull/3405
  *
  */
object GatlingRunner extends StrictLogging {

  def run( overrides: ConfigOverrides ): GatlingRunResult = start( overrides, None )

  private def terminateActorSystem(system: ActorSystem, timeout: FiniteDuration): Unit =
    try {
      val whenTerminated = system.terminate()
      Await.result(whenTerminated, timeout)
    } catch {
      case NonFatal(e) =>
        logger.error("Could not terminate ActorSystem", e)
    }

  private[app] def start(overrides: ConfigOverrides, selectedSimulationClass: SelectedSimulationClass) =
    try {
      logger.trace("Starting")
      val configuration = GatlingConfiguration.load(overrides)
      logger.trace("Configuration loaded")
      // start actor system before creating simulation instance, some components might need it (e.g. shutdown hook)
      val system = ActorSystem("GatlingSystem", GatlingConfiguration.loadActorSystemConfiguration())
      logger.trace("ActorSystem instantiated")
      val runResult =
        try {
          val runner = Runner(system, configuration)
          logger.trace("Runner instantiated")
          runner.run(selectedSimulationClass)
        } catch {
          case e: Throwable =>
            logger.error("Run crashed", e)
            throw e
        } finally {
          terminateActorSystem(system, 5 seconds)
        }
      val statusCode = RunResultProcessor(configuration).processRunResult(runResult)
      GatlingRunResult( statusCode, runResult, null )
    } finally {
      LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext].stop()
    }
}