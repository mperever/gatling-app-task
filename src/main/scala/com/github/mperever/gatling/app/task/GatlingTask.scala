package com.github.mperever.gatling.app.task

import io.gatling.app.{ConfigOverrides, GatlingRunResult, GatlingRunner, RunResult}
import io.gatling.app.cli.StatusCode
import io.gatling.core.config.GatlingPropertiesBuilder
import io.gatling.core.scenario.Simulation

import java.util.concurrent.Callable

class GatlingTask extends Callable[GatlingRunResult] {

  private final var _gatlingOverrides: ConfigOverrides = _
  def gatlingOverrides: ConfigOverrides = _gatlingOverrides.clone()

  def this( simulationClass: Class[_ <: Simulation] ) {
    this()
    _gatlingOverrides = new GatlingPropertiesBuilder()
      .simulationClass( simulationClass.getName )
      .build
  }

  def this( gatlingOverrides: ConfigOverrides ) {
    this()
    _gatlingOverrides = gatlingOverrides.clone()
  }

  override def call: GatlingRunResult =
    try
      GatlingRunner.run( _gatlingOverrides )
    catch {
      case ex: Exception =>
        GatlingRunResult( StatusCode.InvalidArguments, RunResult( null, hasAssertions = false ), ex )
    }
}