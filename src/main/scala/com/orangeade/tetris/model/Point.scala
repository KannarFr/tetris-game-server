package com.orangeade.tetris.model

case class Point(val x: Int, val y: Int, val color: String) {
  def moveDown = Point(x, y+1, color)
  def moveLeft = Point(x-1, y, color)
  def moveRight = Point(x+1, y, color)

  def rotateAroundCenterLeft(center: Point) = {
    val diff = this - center
    val rotated = diff.rotateLeft
    center + rotated
  }
  def rotateAroundCenterRight(center: Point) = {
    val diff = this - center
    val rotated = diff.rotateRight
    center + rotated
  }

  private def rotateLeft = rotate(math.Pi / 2)

  private def rotateRight = rotate(-math.Pi / 2)

  private def rotate(angle: Double) = {
    val x = this.x * math.cos(angle) - this.y * math.sin(angle);
    val y = this.x * math.sin(angle) + this.y * math.cos(angle);
    Point(math.round(x).asInstanceOf[Int], math.round(y).asInstanceOf[Int], color)
  }

  def max(other: Point) = Point(math.max(x, other.x), math.max(y, other.y), color)

  def min(other: Point) = Point(math.min(x, other.x), math.min(y, other.y), color)

  def isInFrame(frame: Size) = (0 until frame.width).contains(x) && (0 until frame.height).contains(y)

  def isOnTop: Boolean = y == 0

  def +(other: Point) = Point(x + other.x, y + other.y, color)

  def -(other: Point) = Point(x - other.x, y - other.y, color)

  def *(factor: Int) = Point(x * factor, y * factor, color)

  def /(divisor: Int) = Point(x / divisor, y / divisor, color)

  def canEqual(a: Any) = a.isInstanceOf[Point]

  override def hashCode: Int = 310 * x + y

  override def equals(that: Any): Boolean = {
    that match {
      case that: Point => that.canEqual(this) && this.hashCode == that.hashCode
      case _ => false
    }
  }
}
