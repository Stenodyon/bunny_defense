
package game_mechanics.tower

import java.io.File
import javax.imageio.ImageIO


object Wall extends TowerType {
    override val name = "Wall"
    override val desc = "Pathfinding debug tower"
    override val tower_graphic =
        ImageIO.read(
            new File(
                getClass().getResource("/towers/wall.png").getPath()))

    override val price = 5
    sell_cost = 0
    override def serialize() : Int = TowerType.WALL
}
