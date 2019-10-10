package timout.path

import scala.collection.mutable

case class Point(x: Int, y: Int) {
  def +(p: Point) : Point = Point(x + p.x, y + p.y)
}

trait Finder {
  def find(): Boolean
}

trait StepFinder extends Finder {

  val model = PathFinderModel
  
  val steps = Array(Point(0,1), Point(1,0), Point(0,-1), Point(-1,0))

  def inBorderCandidates(p: Point, m: Array[Array[Int]]): Array[Point] =
    steps.map( s => s + p).filter( np => np.x >= 0 && np.y >= 0 && np.x < model.size && np.y < model.size )

  def closestCandidates(p: Point, m: Array[Array[Int]]): Point =
    inBorderCandidates(p, m).filter(np => m(np.y)(np.x) > 0).minBy(np => m(np.y)(np.x))

  def drawPath(m: Array[Array[Int]]): Unit = {
    def points(p: Point): Stream[Point] = p #:: points(closestCandidates(p, m))
    points(model.finishPoint).takeWhile { _ != model.startPoint }.foreach { c =>
      model.setPath(c)
      model.sleep()
    }
  }
}

class BreadthFirst extends StepFinder {

  private def candidates(p: Point, m: Array[Array[Int]]): Array[Point] =
    inBorderCandidates(p, m).filter( np => m(np.y)(np.x) == 0 )

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
        l.foreach{ np =>
          model.setPathFrontier(np)
          maze(np.y)(np.x) = i
          i += 1
        }
        q ++= l
        model.sleep()
      }
    }
    if ( found ) drawPath(maze)
    found
  }
}

class GreedyBestFirstSearch extends StepFinder {

  private def heuristic(p: Point) = Math.abs(p.x - model.finishPoint.x) + Math.abs(p.y - model.finishPoint.y)
  
  private def candidates(p: Point, m: Array[Array[Int]]): Array[Point] =
    inBorderCandidates(p, m).filter( np => m(np.y)(np.x) == 0 )

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
          model.setPathFrontier(np)
          maze(np.y)(np.x) = i
          i += 1
        })
        q ++= l
        model.sleep()
      }
    }
    if ( found ) drawPath(maze)
    found
  }

}

class AStarSearch extends StepFinder {

  private def heuristic(p: Point) = Math.abs(p.x - model.finishPoint.x) + Math.abs(p.y - model.finishPoint.y)

  private def candidates(p: Point, cost: Int, m: Array[Array[Int]]): Array[Point] =
    inBorderCandidates(p, m).filter( np => m(np.y)(np.x) == 0 ||  cost < m(np.y)(np.x))

  def find(): Boolean = {
    val maze = model.generateMaze
    val q = mutable
      .PriorityQueue(model.startPoint)(Ordering.by[Point, Double]( p => heuristic(p)  + maze(p.y)(p.x)).reverse)
    var found = false
    maze(model.startPoint.y)(model.startPoint.x) = 1
    while ( q.nonEmpty && (! found ) ) {
      val p = q.dequeue()
      if ( p == model.finishPoint ) {
        found = true
      } else {
        val newCost = maze(p.y)(p.x) + 1
        model.setPathCandidate(p)
        val l = candidates(p, newCost, maze)
        l.foreach( np => {
          model.setPathFrontier(np)
          maze(np.y)(np.x) = newCost
        })
        q ++= l
        model.sleep()
      }
    }
    if ( found ) drawPath(maze)
    found
  }

}