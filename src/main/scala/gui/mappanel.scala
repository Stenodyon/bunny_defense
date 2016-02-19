
package gui

import swing._
import swing.event._

import java.awt.image.BufferedImage
import java.awt.MouseInfo
import java.io.File
import javax.imageio.ImageIO

import runtime.Controller
import game_mechanics.GameMap

object MapPanel
{
  val cellsize = 32
}

/* Represents the map on the screen */
class MapPanel(map0: GameMap) extends Panel {
  import MapPanel._
  val map  = map0
  val rows = map.height
  val cols = map.width

  preferredSize = new Dimension(
    cellsize * cols,
    cellsize * rows )

  listenTo(mouse.clicks)

  reactions += {
    case e: MouseClicked =>
      Controller.on_cell_clicked(
        e.point.x / cellsize,
        e.point.y / cellsize )
  }

  /* Drawing on the map */
  override def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)
    /* Drawing the map */
    for( x <- 0 until cols ) {
      for( y <- 0 until rows ) {
        g.drawImage( map.graphic_map(x)(y),
          x * cellsize,
          y * cellsize, null )
      }
    }
    /* Drawing the bunnies */
    for( bunny <- Controller.bunnies )
    {
      val x = bunny.pos.x * cellsize
      val y = bunny.pos.y * cellsize
      g.drawImage( bunny.graphic, x.toInt, y.toInt, null )
      // Health bar
      val health_ratio = bunny.hp / bunny.initial_hp
      g.setColor( Colors.red )
      g.fillRect( x.toInt, y.toInt - 3, cellsize, 3 )
      g.setColor( Colors.green )
      g.fillRect( x.toInt, y.toInt - 3, (health_ratio * cellsize).toInt, 3 )
      g.setColor( Colors.black )
    }
    /* Drawing the towers */
    for( tower <- Controller.towers )
    {
      val x = tower.pos.x * cellsize
      val y = tower.pos.y * cellsize
      g.drawImage( tower.graphic, x.toInt, y.toInt, null )
    }
    /* Drawing projectiles */
    for( projectile <- Controller.projectiles )
    {
      val x = projectile.pos.x * cellsize
      val y = projectile.pos.y * cellsize
      g.drawImage( projectile.graphic, x.toInt, y.toInt, null )
    }
   for( animation <- Controller.animations )
     animation.draw(g)
    /* Drawing selected tower */
    Controller.selected_tower match {
      case None => {}
      case Some(tower) => {
        val mousepos  = MouseInfo.getPointerInfo().getLocation()
        val windowpos = locationOnScreen
        val snapx     = (mousepos.x - windowpos.x) / cellsize * cellsize
        val snapy     = (mousepos.y - windowpos.y) / cellsize * cellsize
        g.drawRect( snapx, snapy, cellsize, cellsize )
        g.drawImage( tower.graphic, snapx, snapy, null )
        val range   = tower.range * cellsize
        val circlex = snapx + cellsize / 2 - range
        val circley = snapy + cellsize / 2 - range
        g.drawOval( circlex, circley, 2 * range, 2 * range )
      }
    }
  }
}
