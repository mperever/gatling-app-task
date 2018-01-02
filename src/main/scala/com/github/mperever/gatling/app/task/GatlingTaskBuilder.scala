package com.github.mperever.gatling.app.task

import com.typesafe.config.{Config, ConfigRenderOptions}

import io.gatling.app.ConfigOverrides
import io.gatling.core.config.GatlingPropertiesBuilder
import io.gatling.core.ConfigKeys.core
import io.gatling.core.scenario.Simulation

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.util.UUID

object GatlingTaskBuilder {

  private val ROOT_RESULT_DIRECTORY_NAME = "results"
  private val DEFAULT_RESULT_PATH = Paths.get( ".", ROOT_RESULT_DIRECTORY_NAME ).toAbsolutePath.normalize.toString

  private def toJson( config: Config ): String = config.root.render( ConfigRenderOptions.concise.setJson( true ) )

  @throws[IOException]
  private def saveConfigFile( configContent: String, filePath: Path ): Unit = {
    Files.createDirectories( filePath.getParent )
    Files.write( filePath, configContent.getBytes( StandardCharsets.UTF_8 ) )
  }
}

class GatlingTaskBuilder {

  private var _configContent: String = _
  private var _configFileName: String = _
  private var _gatlingOverrides: ConfigOverrides = _

  def this( simulationClass: Class[_ <: Simulation] ) {
    this()
    _gatlingOverrides = new GatlingPropertiesBuilder()
      .simulationClass( simulationClass.getName )
      .resultsDirectory( GatlingTaskBuilder.DEFAULT_RESULT_PATH )
      .noReports
      .mute
      .build
  }

  def this( gatlingOverrides: ConfigOverrides ) {
    this()
    if ( !gatlingOverrides.contains( core.SimulationClass ) ) {
      throw new IllegalArgumentException( "simulationClass is not set in gatling config overrides" )
    }
    _gatlingOverrides = gatlingOverrides.clone()
  }

  def resultsDirectory( resultsDirectory: String ): GatlingTaskBuilder = {
    _gatlingOverrides( core.directory.Results ) = resultsDirectory.asInstanceOf[_]
    this
  }

  /**
    * Sets a configuration to save to a file for following read from the simulation.
    * The <code>config</code> is saved to file in resultsDirectory.
    *
    * <p>
    * The <code>config</code> can be created different ways, for example:
    *   val someConfig: Config = ConfigFactory.parseString("{\"baseUrl\":\"localhost\",\"usersCount\":\"10\"}")
    *
    * <p>
    * The example of read configuration file in Simulation:
    *   val someConfig: Config = ConfigFactory.parseFile(Paths.get(configuration.core.directory.results, fileName).toFile)
    *
    * @param config simulation configuration to save to a file
    * @param fileName file name to save configuration.
    * @return
    */
  def simulationConfig( config: Config, fileName: String ): GatlingTaskBuilder = {
    _configContent = GatlingTaskBuilder.toJson( config )
    _configFileName = fileName
    this
  }

  def build(): GatlingTask = {
    val resultsDirectory: String = _gatlingOverrides( core.directory.Results ).asInstanceOf[String]
    val taskId = System.currentTimeMillis + "_" + UUID.randomUUID.toString.replace( "-", "" )
    val taskResultsDirectory = Paths.get( resultsDirectory, taskId ).toString

    if ( _configContent != null && _configFileName != null ) {
      val configFilePath: Path = Paths.get( taskResultsDirectory.toString, _configFileName )
      GatlingTaskBuilder.saveConfigFile( _configContent, configFilePath )
    }

    _gatlingOverrides( core.directory.Results ) = taskResultsDirectory.asInstanceOf[_]
    new GatlingTask( _gatlingOverrides )
  }
}