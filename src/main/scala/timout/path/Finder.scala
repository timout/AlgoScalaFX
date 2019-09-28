package timout.path

import scala.collection.mutable

case class Point(x: Int, y: Int) {
  def +(p: Point) : Point = Point(x + p.x, y + p.y)
}

trait Finder {
  def find(): Boolean
}

class BreadthFirst extends Finder {

  private val model = PathFinderModel

  private val steps = Array(Point(0,1), Point(1,0), Point(0,-1), Point(-1,0))

  private def candidates(p: Point, m: Array[Array[Int]]): Array[Point] =
    steps.map( s => s + p).filter( np => np.x >= 0 && np.y >= 0 && np.x < model.size && np.y < model.size && m(np.y)(np.x) == 0 )

  private def closestCandidates(p: Point, m: Array[Array[Int]]): Point =
    steps
      .map( s => s + p)
      .filter(np => np.x >= 0 && np.y >= 0 && np.x < model.size && np.y < model.size && m(np.y)(np.x) > 0 )
      .minBy( np => m(np.y)(np.x))

  def find(): Boolean = {
    val maze = model.generateMaze
    val q = mutable.Queue(model.startPoint)
    var found = false
    maze(model.startPoint.y)(model.startPoint.x) = 1
    var i = 2
    while ( q.nonEmpty && (! found ) ) {
      val p = q.dequeue()
      if ( p == model.finishPoint ) {
        found = true
      } else {
        model.setPathCandidate(p)
        val l = candidates(p, maze)
        l.foreach( np => {
          maze(np.y)(np.x) = i
          i += 1
        })
        q ++= l
        model.sleep()
      }
    }
    if ( found ) {
      var c = model.finishPoint
      while ( c != model.startPoint ) {
        model.setPath(c)
        c = closestCandidates(c, maze)
        model.sleep()
      }
    }
    found
  }
}

class HeuristicSearch extends Finder {

  private val model = PathFinderModel

  private val steps = Array(Point(0,1), Point(1,0), Point(0,-1), Point(-1,0))

  private def heuristic(p: Point) = Math.abs(p.x - model.finishPoint.x) + Math.abs(p.y - model.finishPoint.y)


  private def candidates(p: Point, m: Array[Array[Int]]): Array[Point] =
    steps.map( s => s + p)
      .filter( np => np.x >= 0 && np.y >= 0 && np.x < model.size && np.y < model.size && m(np.y)(np.x) == 0 )

  private def closestCandidates(p: Point, m: Array[Array[Int]]): Point =
    steps
      .map( s => s + p)
      .filter(np => np.x >= 0 && np.y >= 0 && np.x < model.size && np.y < model.size && m(np.y)(np.x) > 0 )
      .minBy( np => m(np.y)(np.x))

  def find(): Boolean = {
    val maze = model.generateMaze
    val q = mutable.PriorityQueue(model.startPoint)(Ordering.by[Point, Int]( p => heuristic(p)).reverse)
    var found = false
    maze(model.startPoint.y)(model.startPoint.x) = 1
    var i = 2
    while ( q.nonEmpty && (! found ) ) {
      val p = q.dequeue()
      if ( p == model.finishPoint ) {
        found = true
      } else {
        model.setPathCandidate(p)
        val l = candidates(p, maze)
        l.foreach( np => {
          maze(np.y)(np.x) = i
          i += 1
        })
        q ++= l
        model.sleep()
      }
    }
    if ( found ) {
      var c = model.finishPoint
      while ( c != model.startPoint ) {
        model.setPath(c)
        c = closestCandidates(c, maze)
        model.sleep()
      }
    }
    found
  }

}