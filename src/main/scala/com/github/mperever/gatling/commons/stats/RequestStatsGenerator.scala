package com.github.mperever.gatling.commons.stats

import io.gatling.commons.stats._
import io.gatling.core.config.GatlingConfiguration

class RequestStatsGenerator( reader: GeneralStatsSource ) ( implicit configuration: GatlingConfiguration ) {

  final val GlobalRequestName = "Global Information"

  def computeRequestStats( requestName: Option[String] ): RequestStats = {

    val total = reader.requestGeneralStats( requestName, None, None )
    val ok =    reader.requestGeneralStats( requestName, None, Some( OK ) )
    val ko =    reader.requestGeneralStats( requestName, None, Some( KO ) )

    val percentile1 = total.percentile( configuration.charting.indicators.percentile1 )
    val percentile2 = total.percentile( configuration.charting.indicators.percentile2 )
    val percentile3 = total.percentile( configuration.charting.indicators.percentile3 )
    val percentile4 = total.percentile( configuration.charting.indicators.percentile4 )

    val name = if (requestName.isEmpty) GlobalRequestName else requestName.get
    RequestStats( name, total.count, ok.count, ko.count, total.meanRequestsPerSec, percentile1, percentile2, percentile3, percentile4, total.min, total.max, total.mean, total.stdDev )
  }

  def computeAllRequestStats(): List[RequestStats] = {

    computeRequestStats( None ) :: reader.statsPaths.collect {
      case RequestStatsPath( request, _ ) => computeRequestStats( Some( request ) )
    }
  }
}