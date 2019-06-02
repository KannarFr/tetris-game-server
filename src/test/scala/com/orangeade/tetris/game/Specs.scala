package com.orangeade.tetris.game

import org.scalatest._

import com.orangeade.tetris.game.model._

class Specs extends FlatSpec with Matchers {
  "A GameEngine" should "run until game over" in {

    val game = GameEngine(Size(9, 20), RandomStoneFactory)

    while (game.isGameRunning) {
      game.moveDown
    }

    println(game.drawBoardOnly)
  }
}
