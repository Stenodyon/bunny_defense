
package gui

import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import game_mechanics.path.Waypoint
import runtime.{TowerDefense,Controller}

/* Animation superclass */

abstract class Animatable
{
    type Continuation = () => Unit

    var timer  = 1.0
    var continuation : Option[Continuation] = None

    def set_continuation(cont: Continuation): Unit = {
        continuation = Some(cont)
    }

    def on_timer_ran_out(): Unit = {
        Controller -= this
        continuation match {
            case None => ()
            case Some(cont) => cont()
        }
    }

    def update(dt: Double): Unit = {
        timer -= dt
        if( timer <= 0 )
            on_timer_ran_out()
    }

    def draw(g: Graphics2D): Unit
}

/* Creates nice animations when bunnies die */

class GoldAnimation(amount: Int,origin: Waypoint) extends Animatable
{
    var pos    = origin
    val target = origin + new Waypoint(0,-1)

    override def draw(g: Graphics2D): Unit = {
        pos = origin * timer + target * (1 - timer)
        val string = "+" + amount.toString + " Gold"
        g.setColor( Colors.black )
        g.drawString( string,
            pos.x.toFloat * MapPanel.cellsize + 1,
            pos.y.toFloat * MapPanel.cellsize + 1 )
        g.setColor( Colors.yellow )
        g.drawString( string,
            pos.x.toFloat * MapPanel.cellsize,
            pos.y.toFloat * MapPanel.cellsize )
    }
}

class DamageAnimation(amount: Double, origin: Waypoint) extends Animatable
{
    var pos    = origin
    val target = origin + new Waypoint(0,-1)

    override def draw(g: Graphics2D): Unit = {
        return // TODO Make this animation useful
        pos = origin * timer + target * (1 - timer)
        g.setColor( Colors.red )
        g.drawString( amount.toString + " dmg",
            pos.x.toFloat * MapPanel.cellsize,
            pos.y.toFloat * MapPanel.cellsize )
    }
}

/* Creates a moving "wave #" to indicate the wave number */

object WaveAnimation
{
    val background = ImageIO.read(
        new File(
            getClass().getResource("/UI/Button_Dark.png").getPath()))
    val image_origin_x : Int = background.getWidth() / 2
    val image_origin_y : Int = background.getHeight() / 2
}

class WaveAnimation(wave_number: Int) extends Animatable
{
    import WaveAnimation._

    val duration = 2.0
    timer = duration

    val origin = new Waypoint( 0, TowerDefense.map_panel.size.height / 2 )
    val target = origin + new Waypoint( TowerDefense.map_panel.size.width, 0 )

    override def draw(g: Graphics2D): Unit = {
        val interp = Math.pow( timer * 2 / duration - 1, 3 ) / 2 + 0.5
        val pos = origin * interp + target * ( 1 - interp )
        val string = "Wave " + wave_number.toString
        val strwidth = g.getFontMetrics().stringWidth( string )
        g.drawImage( background,
            pos.x.toInt - image_origin_x,
            pos.y.toInt - image_origin_y,
            null )
        g.setColor( Colors.white )
        g.drawString( string,
            pos.x.toFloat - strwidth.toFloat / 2,
            pos.y.toFloat )
    }
}
