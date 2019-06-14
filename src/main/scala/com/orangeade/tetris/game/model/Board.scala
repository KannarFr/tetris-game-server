package com.orangeade.tetris.game.model

import java.time.LocalDateTime

class Board (
  val size: Size,
  val stones: List[Stone],
  val preview: Stone,
  val statistics: Statistics,
  val isGameRunning: Boolean
) {
  def this(size: Size, firstStone: Stone, firstPreview: Stone) {
    this(
      size,
      List[Stone](firstStone.toTopCenter(Point(size.width / 2, 0, ""))),
      firstPreview,
      Statistics(LocalDateTime.now, None, 0),
      true
    )
  }

  val topCenter = Point(size.width / 2, 0, "")

  def points = stones.map(_.points).flatten

  def update(stones: List[Stone]) = new Board(size, stones, preview, statistics, isGameRunning)

  def update(stones: List[Stone], numberOfRowsRemoved: Int, preview: Stone) = {
    if (stones.exists(s => s.doesCollide(this.preview)) || (!stones.isEmpty && stones.head.isOnTop)) {
      new Board(size, stones, preview, statistics.copy(stopTime = Some(LocalDateTime.now)), false)
    } else {
      new Board(
        size,
        preview.toTopCenter(topCenter) :: stones,
        preview,
        statistics.anotherRowHasBeenCompleted(numberOfRowsRemoved),
        isGameRunning
      )
    }
  }

  def forceNewStone(preview: Stone) =
    new Board(
      size,
      preview.toTopCenter(topCenter) :: stones,
      preview,
      statistics,
      isGameRunning
    )
}
