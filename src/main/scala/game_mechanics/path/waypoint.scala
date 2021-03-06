
package game_mechanics.path

import Math._
import util.Random

object Waypoint {
    val rng = new Random()

    def random() : Waypoint = {
        return new Waypoint( rng.nextDouble(), rng.nextDouble() )
    }

}

class Waypoint(
    var x: Double,
    var y: Double)
extends Serializable {
    /** 2D Point (double) */
    import Waypoint._

    def ==(other: Waypoint): Boolean = {
        return x == other.x && y == other.y
    }

    def !=(other: Waypoint): Boolean = {
        return x != other.x || y != other.y
    }

    def +(other: Waypoint): Waypoint = {
        return new Waypoint( x + other.x, y + other.y )
    }
    def +(other: CellPos): Waypoint = {
        return this + other.toDouble
    }

    def -(other: Waypoint): Waypoint = {
        return new Waypoint( x - other.x, y - other.y )
    }
    def -(other: CellPos): Waypoint = {
        return this - other.toDouble
    }

    def +=(other: Waypoint): Waypoint = {
        x += other.x
        y += other.y
        return this
    }
    def +=(other: CellPos): Waypoint = {
        return this += other.toDouble
    }

    def *(scalar: Double): Waypoint = {
        return new Waypoint( x * scalar, y * scalar )
    }
    def /(scalar: Double): Waypoint = {
        return new Waypoint( x / scalar, y / scalar )
    }
    def &(vect: Waypoint) : Double = {
        return vect.x * x + vect.y * y
    }

    def norm() : Double = {
        return Math.sqrt(Math.pow(this.x,2.0)+Math.pow(this.y,2.0))
    }

    def normalize() : Waypoint = {
        val n = norm
        return new Waypoint( x / n, y / n )
    }

    def distance_to( other: Waypoint ): Double = {
    return math.sqrt(
        math.pow( x - other.x, 2 )
        + math.pow( y - other.y, 2 ) )
    }

    override def toString(): String = {
        return "(" + x.toString + "," + y.toString + ")"
    }

    def toInt(): CellPos = {
        return new CellPos( x.toInt, y.toInt )
    }

    override def clone(): Waypoint = {
        return new Waypoint( x, y )
    }
}

