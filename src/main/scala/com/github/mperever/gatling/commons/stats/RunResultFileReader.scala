package com.github.mperever.gatling.commons.stats

import io.gatling.app.{ConfigOverrides, RunResult}
import io.gatling.charts.stats.LogFileReader
import io.gatling.commons.stats.assertion.{AssertionResult, AssertionValidator}
import io.gatling.core.config.GatlingConfiguration

import scala.collection.immutable.List

/**
  * Class containing entry point to read results of simulation from log file.
  *
  * @author mperever
  */
class RunResultFileReader( runResult: RunResult, overrides: ConfigOverrides ) {

  implicit val config: GatlingConfiguration = GatlingConfiguration.load( overrides )

  private val reader = new LogFileReader( runResult.runId )
  private val statsGenerator = new RequestStatsGenerator( reader )

  def runStart(): Long = {
    reader.runStart
  }

  def runEnd(): Long = {
    reader.runEnd
  }

  def computeRequestsStats(): List[RequestStats] = {
    statsGenerator.computeAllRequestStats()
  }

  def assertionResults(): List[AssertionResult] = {
    AssertionValidator.validateAssertions( reader )
  }
}