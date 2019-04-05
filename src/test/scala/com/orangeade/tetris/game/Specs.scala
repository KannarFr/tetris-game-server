package com.orangeade.tetris.game

import org.scalatest._

import com.orangeade.tetris.model._

class Specs extends FlatSpec with Matchers {
  "A Board" should "pop values in last-in-first-out order" in {

    val game = GameEngine(Size(9, 20), RandomStoneFactory)

    while (game.isGameRunning) {
      game.moveDown
    }

    println(game.drawBoardOnly)
  }
}
