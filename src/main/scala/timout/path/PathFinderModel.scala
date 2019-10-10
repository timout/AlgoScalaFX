package timout.path

import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.collections.ObservableBuffer

object PathFinderModel {

  val size = 30

  @volatile var counter = 0

  private var finder : Finder = new BreadthFirst()
  private var onDragAction: Owner = OBSTACLE
  private var sleepTime = 10

  val algorithms: ObservableBuffer[String] = ObservableBuffer(
    "Breadth First",
    "Greedy Best First",
    "A*"
  )

  def setAlgorithm(index: Int): Unit = {
    finder = index match {
      case 1 => new GreedyBestFirstSearch()
      case 2 => new AStarSearch()
      case _ => new BreadthFirst()
    }
  }

  val onDragActions: ObservableBuffer[String] = ObservableBuffer("Obstacle", "Empty")

  def setOnDrag(index: Int): Unit = index match {
    case 0 => onDragAction = OBSTACLE
    case _ => onDragAction = EMPTY
  }

  val sleepTimes: ObservableBuffer[String] = ObservableBuffer("0", "10", "20", "30", "40", "50")

  def setSleepTime(index: Int): Unit = {
    sleepTime = index * 10
  }

  private val board = Array.tabulate(size, size)((_, _) => ObjectProperty[Owner](EMPTY))
  private var _startPoint: Point = Point(0,0)
  private var _finishPoint: Point = Point(size-1, size-1)

  // Initialize start and finish points
  board(0)(0)() = START
  board(size-1)(size-1)() = FINISH

  @volatile private var isRunning = false
  val isRunningState = BooleanProperty(false)

  def generateMaze: Array[Array[Int]] = {
    val maze = Array.tabulate(size, size) ( (y, x) => {
      val p = Point(x, y)
      getPointValue(p) match {
        case OBSTACLE => -1
        case _ => 0
      }
    })
    maze
  }

  def sleep(): Unit = Thread.sleep(sleepTime)

  def getPointProperty(p: Point): ObjectProperty[Owner] = board(p.y)(p.x)

  def startFinder(): Unit = {
    setRunning(true)
    new Thread( () => {
      counter = 0
      finder.find()
      setRunning(false)
    }).start()
  }

  def reset(): Unit = {
    setRunning(true)
    points.foreach{ p => board(p.y)(p.x)() = EMPTY }
    _startPoint = Point(0,0)
    _finishPoint = Point(size-1, size-1)
    board(0)(0)() = START
    board(size-1)(size-1)() = FINISH
    setRunning(false)
  }

  val cleanPoint: PartialFunction[Point, Unit] = (p: Point) => board(p.y)(p.x)() match {
    case PATH | CANDIDATE_PATH | FRONTIER_PATH => board(p.y)(p.x)() = EMPTY
  }

  def clean(): Unit = {
    setRunning(true)
    points.collect(cleanPoint)
    setRunning(false)
  }

  def whenStop( f: => Unit ): Unit = if ( ! isRunning ) f

  def setObstacle()(p: Point): Unit = {
    whenStop {
      val cur: Owner = getPointValue(p)
      cur match {
        case OBSTACLE => setPointValue(p, EMPTY)
        case EMPTY => setPointValue(p, OBSTACLE)
        case _ =>
      }
    }
  }

  def setPointDragValue(p: Point): Unit = {
    whenStop {
      val cur: Owner = getPointValue(p)
      cur match {
        case OBSTACLE if onDragAction == EMPTY => setPointValue(p, EMPTY)
        case EMPTY if onDragAction == OBSTACLE => setPointValue(p, OBSTACLE)
        case _ =>
      }
    }
  }

  def setPointByFinder(p: Point, value: Owner): Unit = {
    if ( p != startPoint && p != finishPoint ) {
      board(p.y)(p.x)() = value
      counter += 1
    }
  }

  def setPathCandidate(p: Point): Unit = setPointByFinder(p, CANDIDATE_PATH)

  def setPathFrontier(p: Point): Unit = setPointByFinder(p, FRONTIER_PATH)

  def setPath(p: Point): Unit = setPointByFinder(p, PATH)

  def startPoint: Point = _startPoint

  def startPoint_=(p: Point): Unit = whenStop {
    if (p != finishPoint) {
      setPointValue(_startPoint, EMPTY)
      _startPoint = p
      setPointValue(_startPoint, START)
    }
  }

  def finishPoint: Point = _finishPoint

  def finishPoint_=(p: Point): Unit = whenStop {
    if ( p != startPoint ) {
      setPointValue(_finishPoint, EMPTY)
      _finishPoint = p
      setPointValue(_finishPoint, FINISH)
    }
  }

  private def points = (0 until size).flatMap( y => (0 until size).map(x => Point(x,y) ) )

  private def getPointValue(p: Point) : Owner = board(p.y)(p.x)()

  private def setPointValue(p: Point, value: Owner = EMPTY): Unit = {
    board(p.y)(p.x)() = value
  }

  private def setRunning(r: Boolean): Unit = {
    isRunningState() = r
    isRunning = r
  }

}
