
package game_mechanics.bunny

import java.io.File
import javax.imageio.ImageIO

/* Fast "Bunny" */
class FlyingSquirrel(player_id: Int) extends Bunny
{
    override val player      = player_id
    override val bunny_graphic =
        ImageIO.read(new File(
            getClass().getResource("/mobs/Flying_Squirrel.png").getPath()))
    initial_hp               = 5.0
    base_shield              = 0.0
    shield                   = 0.0
    base_speed               = 5.0
    speed                    = 5.0
    override val price       = 150
}
