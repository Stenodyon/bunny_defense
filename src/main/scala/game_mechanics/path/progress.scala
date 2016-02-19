
package game_mechanics.path

class Progress(p: Path) {
  /* Index of the next node */
  var i = 1
  /* Distance traveled along the path [current node] -> [next node] */
  var progress = 0.0
  val path = p
  def move( distance: Double ): Unit = {
    if( reached )
      return
    val current = path.at(i-1)
    val next = path.at(i)
    val dist = current distance_to next
    /* If we have reached the next node we reset progress along the line and go to the next one */
    if( (dist - progress) < distance ) {
      progress = 0.0
      i += 1
      move( distance - (dist - progress) )
    }
    else { /* else we just move along the line */
      progress += distance
    }
  }
  def get_position(): Waypoint = {
    if( reached )
      return path.last
    val current = path.at(i-1)
    val next = path.at(i)
    val ratio = progress / ( current distance_to next )
    return current * ( 1 - ratio ) + next * ratio
  }

  def reached(): Boolean = {
    return i >= path.length()
  }
}
