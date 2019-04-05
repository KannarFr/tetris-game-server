package com.orangeade.tetris.model

import java.util.Calendar

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
      Statistics(Calendar.getInstance().getTime(), 0),
      true
    )
  }

  val topCenter = Point(size.width / 2, 0, "")

  def points = stones.map(_.points).flatten

  def update(stones: List[Stone]) = new Board(size, stones, preview, statistics, isGameRunning)

  def update(stones: List[Stone], numberOfRowsRemoved: Int, preview: Stone) = {
    if (stones.exists(s => s.doesCollide(this.preview)) || (!stones.isEmpty && stones.head.isOnTop)) {
      new Board(size, stones, preview, statistics, false)
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

  def draw = {
    val previewSize = Size(5, 5)
    val preview = this.preview.toTopCenter(Point(previewSize.width / 2, 0, ""))
    if (isGameRunning) {
      drawBoardOnlyInternal(size, points) + "\n" +
      drawBoardOnlyInternal(previewSize, preview.points) + "\n" +
      statistics.draw
    } else {
      drawBoardOnlyInternal(size, points) +
      drawBoardOnlyInternal(previewSize, preview.points) + "\n" +
      "GAME OVER\n" +
      statistics.draw
    }
  }

  def drawBoardOnly = drawBoardOnlyInternal(size, points)

  private def drawBoardOnlyInternal(size: Size, points: List[Point]) = {
    var drawing = ""
    drawing += "-" * (size.width * 7 + 2) + "\n"
    Range(0, size.height).map { indexLine =>
      drawing += "|"
      val pointsInLine = points.filter(p => p.y == indexLine)
      Range(0, size.width).map { indexColumn =>
        if (pointsInLine.exists(p => p.x == indexColumn)) {
          drawing += pointsInLine.find(p => p.x == indexColumn).get.color
        } else {
          drawing += "       "
        }
      }
      drawing += "|\n"
    }
    drawing += "-" * (size.width * 7 + 2) + "\n"
    drawing
  }
}
