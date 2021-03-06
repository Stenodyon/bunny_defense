
package game_mechanics.tower

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import game_mechanics._
import game_mechanics.bunny._
import runtime.TowerDefense

object SupportSpawnerTower extends SpawnerTower()
{
    override val name = "Support barn"
    override val desc = "Creates bunnies that shield allies"
    bunnies_spawning  = List(BunnyFactory.SHIELD_BUNNY)
    base_range                 = 4
    range                      = 4
    override val fire_speed   = 15.0
    base_damage                = 9
    damage                     = 9
    override val price         = 250
    sell_cost                  = 125
    override val unlock_wave   = 1
    upgrades                   = Some(Spawner_HealthUpgrade)
    override def serialize() : Int = TowerType.SUPPORT_SPAWNER
}
