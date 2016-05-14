
package runtime

import Math.abs

import swing.event._
import swing._

import collection.mutable.{ListBuffer,Queue}
import util.Random
import collection.parallel._

import runtime._
import game_mechanics._
import game_mechanics.path._
import game_mechanics.tower._
import game_mechanics.bunny._
import gui._
import gui.animations._

case object SelectedCell extends Event
case object NoSelectedCell extends Event
case object FastForwOn extends Event
case object FastForwOff extends Event

class GameState extends State with Publisher
{
    /**
     * The main controller.
     * It manages the main loop, the graphics, everything
     */
    val bunnies      = new ListBuffer[Bunny]
    val projectiles  = new ListBuffer[Projectile]
    val towers       = new ListBuffer[Tower]
    val animations   = new ListBuffer[Animatable]
    val updatables   = new ListBuffer[Updatable]
    var wave_counter = 1
    val framerate    = 1.0/60.0 * 1000
    var started      = false
    var dt: Double   = 0.0
    var acceleration = 2
    var is_accelerated = false
    var raining      = false
    /* The tower type selected for construction */
    var selected_tower          : Option[TowerType]     = None
    /* The tower currently selected */
    private var _selected_cell  : Option[Tower]         = None
    var rng          = new Random

    /* GUI */
    val map_panel   = new MapPanel(new GameMap(30,15))
    val build_menu  = new BuildMenu( 4, 4 )
    {
        pos = new CellPos( map_panel.size.x, 0 )
    }
    val info_panel  = new InfoPanel
    val tower_panel = new TowerInfoPanel

    /* Returns a panel containing the in-game gui */
    def make_gui() : TDComponent =
    {
        val play_button = new TextButton(
            StateManager.render_surface,
            0, 0, 100, 100,
            "Play")
        {
            override def action() : Unit = {
                on_play_button(this)
            }
            /*
            listenTo(SpawnScheduler)
            reactions += {
                case WaveEnded =>
                    enabled = true
            }
            text       = "Play"
            background = Colors.green
            preferredSize = new Dimension( 100, 100 )
            focusable = false
            */
            size = new CellPos( 100, 100 )
        }
        return new TDComponent
        {
            children += map_panel
            children += tower_panel
            children += info_panel
            children += build_menu
            children += play_button
        }
    }

    val gui = make_gui()

    listenTo(SpawnScheduler)

    reactions += {
        case WaveEnded =>
            set_accelerated( false )
    }

    /* selected_cell GETTER */
    def selected_cell_=(tower: Option[Tower]): Unit =
    {
        if (tower != None)
            publish(SelectedCell)
        else
            publish(NoSelectedCell)
        _selected_cell = tower
    }

    /* selected_cell SETTER */
    def selected_cell =  _selected_cell

    /* ==================== CALLBACKS ==================== */

    /* Triggered when a map cell is clicked */
    def on_cell_clicked( x:Int, y:Int ): Unit = {
        // Placing a new tower
        val pos = new CellPos(x,y)
        if( selected_tower != None &&
            map_panel.map.valid(pos) )
        {
            if( Player.remove_gold(selected_tower.get.buy_cost) ) {
                this += new Tower( selected_tower.get, pos )
                /* Updates the paths of living bunnies, so they won't conflict
                 * with the new tower. Uses multi-threading to be more efficient */
                var bun_update = bunnies.filter( t => t.path.path.exists(
                    u => u.x == pos.x && u.y == pos.y)).par
                bun_update.tasksupport = new ForkJoinTaskSupport(
                    new scala.concurrent.forkjoin.ForkJoinPool(8))
                val centering = new Waypoint( 0.5, 0.5 )
                for (bunny <- bun_update) {
                    bunny.path.path = new JPS(
                        (bunny.pos + centering).toInt,
                        bunny.bunnyend).run().get
                    bunny.path.reset
                    bunny.bunnyend = bunny.path.last.toInt
                }
            }
            else
                println("Not enough money! Current money = " + Player.gold.toString)
        }
        // Selecting a placed tower
        else if ( selected_tower == None )
        {
            selected_cell = towers.find( _.pos == pos )
        }
        // Building multiple towers
        if ( !TowerDefense.keymap(Key.Shift) ) {
            selected_tower = None
        }
    }


    /* Triggered when the play button is clicked */
    def on_play_button(button : TDButton): Unit = {
        button.enabled = false
        val spawner = new Spawner(wave_counter)
        val spawnschedule = spawner.create()
        SpawnScheduler.set_schedule(spawnschedule)
        val anim = new WaveAnimation(wave_counter)
        anim and_then SpawnScheduler.start
        if( spawner.has_boss )
        {
            val splash_anim = new SplashAnimation()
            splash_anim and_then { () => this += anim }
            this += splash_anim
        } else {
            this += anim
        }
    }

    def set_accelerated(value: Boolean) : Unit = {
        is_accelerated = value
        acceleration = if( is_accelerated ) 5 else 2
    }

    /* Triggered when the fast forward button is clicked */
    def on_fastforward_button(): Unit = {
        set_accelerated( !is_accelerated )
    }

    def upgrade_tower(): Unit = {
       this.selected_cell.get.upgrades match
       {
           case None            => {}
           case Some(upgrade)   => {
               if (Player.remove_gold(upgrade.cost)) {
                   upgrade.effect(selected_cell.get)
                   selected_cell.get.sell_cost += ((0.8 * upgrade.cost).toInt)
               }
               else {
                   println("Not enough money, you noob !")
               }
           }
       }
    }

    def scroll(dt: Double): Unit = {
        val scroll_speed = 128
        /* Handling input */
        if( TowerDefense.keymap(Key.J) )
        {
            val scroll_distance = Math.min(
                map_panel.rows * MapPanel.cellsize -
                    map_panel.size.y,
                map_panel.viewpos.y + dt * scroll_speed )
            map_panel.viewpos =
                new Waypoint(0, scroll_distance)
        }
        if( TowerDefense.keymap(Key.K) )
        {
            val scroll_distance = Math.max( 0,
                map_panel.viewpos.y - dt * scroll_speed )
            map_panel.viewpos =
                new Waypoint(0, scroll_distance)
        }
        if( TowerDefense.keymap(Key.H) )
        {
            val scroll_distance = Math.max( 0,
                map_panel.viewpos.x - dt * scroll_speed )
            map_panel.viewpos =
                new Waypoint(scroll_distance, 0)
        }
        if( TowerDefense.keymap(Key.L) )
        {
            val scroll_distance = Math.min(
                map_panel.cols * MapPanel.cellsize -
                    map_panel.size.x,
                map_panel.viewpos.x + dt * scroll_speed )
            map_panel.viewpos =
                new Waypoint(scroll_distance, 0)
        }
    }

    /* ==================== MAIN LOOP ==================== */

    /* Update the game for dt time */
    def step(dt: Double): Unit = {
        scroll(dt)
        /* Update animations */
        animations.foreach( _.update(dt) )
        /* Update misc items */
        updatables.foreach( _.update(dt) )
        /* Update projectiles */
        projectiles.foreach( _.update(dt) )
        /* Update towers */

        /* Reinitialize the damage and range of all towers */
        towers.foreach (x => x.damage = x.base_damage)
        towers.foreach (x => x.range = x.base_range)
        /* Apply all tower effects to towers*/
        towers.foreach( tower =>
            towers.foreach( x => if( (x.pos - tower.pos).norm <= tower.range )
                { tower.allied_effect(x)})
        )
        towers.foreach( _.update(dt) )
        /* Update bunnies */

         /* Reinitialize the speed and shield of all bunnies */
        bunnies.foreach (x => x.speed = x.base_speed)
        bunnies.foreach (x => x.shield = x.base_shield)
        /* Apply all tower effects to bunnies*/
        towers.foreach( tower =>
            bunnies.foreach( x => if( (x.pos - tower.pos).norm <= tower.range )
                { tower.enemy_effect(x) })
        )
        bunnies.foreach( bunny =>
                bunnies.foreach( x => if( (x.pos - bunny.pos).norm <= bunny.effect_range )
                { bunny.allied_effect(x) })
        )
        bunnies.foreach( _.update(dt) )
        /* Spawn in new bunnies */
        SpawnScheduler.update(dt)
        /* Random chance of rain */
        if( rng.nextDouble < (dt / 200) && !raining )
        {
            raining = true
            val anim = if(rng.nextDouble < 0.5)
                new ThunderstormAnimation( 30 + rng.nextDouble * 120 )
            else
                new RainAnimation( 30 + rng.nextDouble * 120 )
            anim and_then { () => this.raining = false }
            this += anim
        }
        if ( TowerDefense.keymap(Key.Escape)) {
            selected_tower = None
            selected_cell  = None
        }
        /* If player loses all health */
        if (Player.hp <= 0) {
            //Dialog.showMessage( map_panel, "Game Over" )
            TowerDefense.quit()
        }
    }

    override def update(dt: Double) : Unit = {
        for( i <- 1 to acceleration )
            step(dt)
    }

    override def render(g: Graphics2D) : Unit = {
        gui.draw(g)
    }

    /* ==================== COLLECTION-LIKE BEHAVIOR ==================== */

    /* BUNNIES */

    def +=(bunny: Bunny): Unit = {
        bunnies += bunny
    }

    def -=(bunny: Bunny): Unit = {
        bunnies -= bunny
    }

    /* PROJECTILES */

    def +=(projectile: Projectile): Unit = {
        projectiles += projectile
    }

    def -=(projectile: Projectile): Unit = {
        projectiles -= projectile
    }

    /* TOWERS */

    def +=(tower: Tower): Unit = {
        /*
        towers.foreach( x => if( (x.pos - tower.pos).norm <= tower.range ) { tower.allied_effect(x)})
        bunnies.foreach( x => if((x.pos - tower.pos).norm <= tower.range) {tower.enemy_effect(x)})
         */
        towers += tower
        tower.towertype.amount += 1
        map_panel.map += tower
    }

    def -=(tower: Tower): Unit = {
        towers -= tower
        tower.towertype.amount -= 1
        map_panel.map -= tower
    }

    /* ANIMATIONS */

    def +=(animation: Animatable): Unit = {
        animations += animation
    }

    def -=(animation: Animatable): Unit = {
        animations -= animation
    }

    /* MISC ITEMS */
    def +=(updatable: Updatable): Unit = {
        updatables += updatable
    }

    def -=(updatable: Updatable): Unit = {
        updatables -= updatable
    }
}