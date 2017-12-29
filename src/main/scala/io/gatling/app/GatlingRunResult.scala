package io.gatling.app

import io.gatling.app.cli.StatusCode

case class GatlingRunResult( statusCode: StatusCode, runResult: RunResult, executionError: Exception )