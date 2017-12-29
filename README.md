Gatling Task Extension
======================

[![Build Status](https://travis-ci.org/mperever/gatling-app-task.png?branch=master)](https://travis-ci.org/mperever/gatling-app-task)

What is it?
-----------

Task extension for [Gatling](https://github.com/gatling/gatling) to create custom launcher/integration for other system.
[Gatling pull request](https://github.com/gatling/gatling/pull/3405)
The purpose is to create task to run Gatling and get run result object after execution instead of execution status code.
Furthermore that extension provides facilities to get requests statistics from simulation log file.

Creating a Gatling Task
-----------------------

Creating a task from Gatling configuration parameters:

```scala
    val gatlingOverrides: ConfigOverrides = ...
    val task: GatlingTask = new GatlingTask(gatlingOverrides)
```

Creating a task from simulation class:

```scala
    val task: GatlingTask = new GatlingTask(classOf[MySimulation])
```

Creating a task using task builder:

```scala
    val someSimulationConfig: Config = ConfigFactory.parseString("{\"baseUrl\":\"localhost\",\"usersCount\":\"10\"}")
    val task: GatlingTask = new GatlingTaskBuilder(classOf[MySimulation])
        .resultsDirectory("custom_result_directory")
        .simulationConfig(someSimulationConfig, "simulationConfig.json")
      .build()
```

Run a Gatling Task
------------------

GatlingTask class implements [Callable](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Callable.html) interface.

```scala
    ...
    val result: GatlingRunResult = task.call()
```

Getting requests statistics from simulation log file
----------------------------------------------------

By default simulation log file is generated automatically after each Gatling run.

```scala
    ...
    val result: GatlingRunResult = task.call()

    val logReader: RunResultFileReader = new RunResultFileReader(result.runResult, task.gatlingOverrides)
    val startTime: Long = logReader.runStart()
    val endTime: Long = logReader.runEnd()
    val requestsStats: List[RequestStats] = logReader.computeRequestsStats()
	val assertionsResults: List[AssertionResult] = logReader.assertionResults()
```
