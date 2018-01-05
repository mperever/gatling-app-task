package com.github.mperever.gatling.http

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.{Appender, FileAppender}

import java.util.UUID

import org.slf4j.LoggerFactory

/**
  * Represents object to create and attach an appender to the gatling HTTP requests and responses.
  *
  * @author mperever
  */
object GatlingHttpLoggers {

  private val LOGGER_CONTEXT = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
  private val HTTP_AHC_LOGGER = LOGGER_CONTEXT.getLogger( "io.gatling.http.ahc" )
  private val HTTP_RESPONSE_LOGGER = LOGGER_CONTEXT.getLogger( "io.gatling.http.response" )

  private val APPENDER_PATTERN_ENCODER = new PatternLayoutEncoder
  APPENDER_PATTERN_ENCODER.setContext( LOGGER_CONTEXT )
  APPENDER_PATTERN_ENCODER.setPattern( "%logger{35} - %msg%n" )
  APPENDER_PATTERN_ENCODER.start()

  /**
    * Attaches the rolling file appender for the gatling http requests and responses.
    *
    * @param logFile log file name
    */
  def addFileAppender( logFile: String ): Unit = {
    val appenderName = UUID.randomUUID.toString.replace( "-", "" )
    val logFileAppender = new FileAppender[ILoggingEvent]
    logFileAppender.setName( appenderName )
    logFileAppender.setContext( LOGGER_CONTEXT )
    logFileAppender.setFile( logFile )
    logFileAppender.setEncoder( APPENDER_PATTERN_ENCODER )
    logFileAppender.start()

    addAppender( logFileAppender )
  }

  /**
    * Attach an appender. If the appender is already in the list in won't be added again.
    *
    * @param newAppender the appender to attach
    */
  def addAppender( newAppender: Appender[ILoggingEvent] ): Unit = {
    HTTP_AHC_LOGGER.addAppender( newAppender )
    HTTP_RESPONSE_LOGGER.addAppender( newAppender )
  }
}