package com.orangeade.tetris.game.model

import java.util._
import java.text.SimpleDateFormat
import java.time.LocalDateTime

case class Statistics(val startTime: LocalDateTime, val stopTime: Option[LocalDateTime], val rowsCompleted: Long) {
  def anotherRowHasBeenCompleted(numberOfRows: Int) = Statistics(startTime, None, rowsCompleted + numberOfRows)
}
