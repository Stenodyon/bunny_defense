
package game_mechanics.tower

import collection.mutable.ListBuffer

import game_mechanics._
import game_mechanics.bunny.Bunny
import game_mechanics.path._
import runtime.{Spawner,Controller,TowerDefense}
import game_mechanics.bunny._
import game_mechanics.JPS
import util.Random

class SpawnerTower() extends TowerType
{
    /**
     * The class that defines the methods of all spawners
      */
    val law              = new Random()
    var bunnies_spawning = List(BunnyFactory.NORMAL_BUNNY, BunnyFactory.NORMAL_BUNNY, BunnyFactory.GOLDEN_BUNNY)

    override def attack_from(tower : Tower): () => Boolean = {
        def get_right_type(): Boolean = {
            var new_bunny = BunnyFactory.create(bunnies_spawning.head,0)
            new_bunny.path = new Progress(
                new JPS(tower.pos,
                    new CellPos(TowerDefense.map_panel.map.width,
                        law.nextInt(TowerDefense.map_panel.map.height/2)+
                            TowerDefense.map_panel.map.height/4
                    )
                ).run()
                    match {
                    case None    => throw new Exception()
                    case Some(p) => p
                }
            )
            Controller += new_bunny
            bunnies_spawning = bunnies_spawning.tail ::: List(bunnies_spawning.head)
            return true
        }
        return get_right_type
    }
}
