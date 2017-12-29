package com.github.mperever.gatling.commons.stats

case class RequestStats(
  name:                          String,
  total:                         Long,
  ok:                            Long,
  ko:                            Long,
  meanNumberOfRequestsPerSecond: Double,
  percentiles1:                  Int,
  percentiles2:                  Int,
  percentiles3:                  Int,
  percentiles4:                  Int,
  minResponseTime:               Int,
  maxResponseTime:               Int,
  meanResponseTime:              Int,
  standardDeviation:             Int
)