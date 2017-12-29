package com.github.mperever.gatling.app.task

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.util.UUID

import com.typesafe.config.{Config, ConfigRenderOptions}
import io.gatling.core.config.GatlingPropertiesBuilder
import io.gatling.core.scenario.Simulation

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

class GatlingTaskBuilder( val simulationClass: Class[_ <: Simulation] ) {

  private var resultsDirectory: String = _
  private var dataDirectory: String = _
  private var configContent: String = _
  private var configFileName: String = _

  def resultsDirectory( resultsDirectory: String ): GatlingTaskBuilder = {
    this.resultsDirectory = resultsDirectory
    this
  }

  def dataDirectory( dataDirectory: String ): GatlingTaskBuilder = {
    this.dataDirectory = dataDirectory
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
    this.configContent = GatlingTaskBuilder.toJson( config )
    this.configFileName = fileName
    this
  }

  def build(): GatlingTask = {
    if ( resultsDirectory == null ) resultsDirectory = GatlingTaskBuilder.DEFAULT_RESULT_PATH
    val taskId = System.currentTimeMillis + "_" + UUID.randomUUID.toString.replace( "-", "" )
    val taskResultsDirectory = Paths.get( resultsDirectory, taskId ).toString

    val gatlingProperties = new GatlingPropertiesBuilder()
      .simulationClass( simulationClass.getName )
      .resultsDirectory( taskResultsDirectory )
      .noReports
      .mute
    if ( dataDirectory != null ) gatlingProperties.dataDirectory( dataDirectory )

    if ( configContent != null && configFileName != null ) {
      val configFilePath: Path = Paths.get( taskResultsDirectory.toString, configFileName )
      GatlingTaskBuilder.saveConfigFile( configContent, configFilePath )
    }

    new GatlingTask( gatlingProperties.build )
  }
}