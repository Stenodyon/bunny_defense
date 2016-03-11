
package gui

import swing._

import javax.swing.ImageIcon

import runtime._
import game_mechanics.{TowerType,Player,MoneyChanged}


/*Creates a Button for the shop. It reacts to money movements and Play buttons*/

object BuyButton {
  val cellsize = 40
  val dimension = new Dimension (cellsize, cellsize)
}

class BuyButton(tower0: Option[TowerType]) extends Button {
  import BuyButton._
  this.background    = Colors.white
  this.focusable     = false
  this.action        = Action("") { Controller.on_build_button(this) }
  if( tower0 != None ) {
    this.icon        = new ImageIcon( tower0.get.tower_graphic )
  }
  this.preferredSize = dimension
  var on_play        = false
  val tower          = tower0

  listenTo(Player,SpawnScheduler)

  reactions += {
    case MoneyChanged => {
      if( tower != None )
        this.enabled = (Player.gold >= tower.get.buy_cost) && !on_play
      if( this.enabled )
        this.background = Colors.white
      else
        this.background = Colors.lightred
    }
/*    case WaveStarted =>
      on_play = true
      enabled = false
      this.background = Colors.lightred
    case WaveEnded =>
      if ( tower != None ) {
        on_play      = false
        this.enabled = (Player.gold >= tower.get.buy_cost)
        if ( this.enabled ) {
          this.background = Colors.white
        }
      } */
    }
}
