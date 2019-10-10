package timout.path

sealed case class Owner(value: Int)

object OBSTACLE extends Owner(-1)
object EMPTY extends Owner(0)
object START extends Owner(1)
object FINISH extends Owner(2)
object CANDIDATE_PATH extends Owner(3)
object FRONTIER_PATH extends Owner(4)
object PATH extends Owner(5)




