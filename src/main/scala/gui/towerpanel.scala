
package gui

import swing._
import swing.event._

import java.awt.AlphaComposite
import java.awt.Graphics2D
import java.awt.Font
import java.awt.MouseInfo

import runtime._
import game_mechanics._
import game_mechanics.tower._
import game_mechanics.path.CellPos

/* An info Panel that shows information on the selected tower */

case object SelectedCell extends Event
case object NoSelectedCell extends Event

object TowerInfoPanel {
  val default_size = new CellPos(1024,100)
}

class TowerInfoPanel(parent: Option[TDComponent], gamestate: GuiGameState)
extends TDComponent(parent) {
  import TowerInfoPanel._
  val button_width = 100

  size = default_size
  val xm = size.x
  val ym = size.y
  override def draw(g: Graphics2D): Unit = {
    super.draw(g)

    val windowpos = locationOnScreen
    val mousepos  = MouseInfo.getPointerInfo().getLocation()
    val mousex    = mousepos.x - windowpos.x
    val mousey    = mousepos.y - windowpos.y

    /* Info panel */
    g.setColor( Colors.black )
    g.drawRect( 0, 0, button_width, ym-1 )
    gamestate.selected_cell match {
      case None =>  {}
      case Some(tower) => {
        g.setFont(g.getFont().deriveFont(Font.BOLD))
        g.drawString(tower.tower_type.name,
          button_width + xm / 10,
          ym/5 +5)
        g.drawString("Range :" + tower.range,
          button_width + 10,
          2 * ym / 4 -5)
        g.drawString("Projectile speed :" + tower.fire_speed,
          button_width + 2 * xm / 12,
          2 * ym / 4 -5)
        g.drawString("Damage :" + tower.damage,
          button_width + 10,
          3 * ym / 4 -5)
        g.drawString("Sell price :" + tower.sell_cost,
          button_width + 2 * xm / 12,
          3 * ym / 4 -5)
      }
    }
  }

  /* Sell button */
  val sell_button = new TextButton(Some(this), "Sell Tower") {
    pos = new CellPos( xm-button_width, 5)
    size =  new CellPos(button_width, ym/2 - 10)
    color = Colors.red
    text_color = Colors.black
    enabled = false

    override def action() : Unit = {
      val tower = gamestate.selected_cell.get
      gamestate.sell_tower(tower)
      update_selection(false)
    }
  }

  /* Upgrade Button */
  val upgrade_button = new TextButton(Some(this), "Upgrade Tower") {
    pos = new CellPos( xm - button_width, ym/2-3)
    size = new CellPos( button_width, ym/2 - 10)
    color = Colors.red
    text_color = Colors.black
    enabled = false

    override def action() : Unit = {
      val tower = gamestate.selected_cell.get
      gamestate.upgrade_tower(tower)
    }
  }

  if (!gamestate.multiplayer) {
    /* Fast forward button */
    val ff_button = new TextButton(Some(this), "Fast Forward") {
      pos = new CellPos( 0, 0 )
      size = new CellPos ( button_width, ym )
      color = Colors.blue
      text_color = Colors.black

      override def action() {
        gamestate.acceleration = 4
      }
    }
  }

  def update_selection(selected : Boolean): Unit = {
    if (selected) {
      sell_button.color = Colors.green
      sell_button.enabled = true
      upgrade_button.color = Colors.green
      upgrade_button.enabled = true
    } else {
      gamestate.selected_cell = None
      gamestate.selected_tower = None
      sell_button.color = Colors.red
      sell_button.enabled = false
      upgrade_button.color = Colors.red
      upgrade_button.enabled = false
    }
  }

}

