package timout.path

import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.collections.ObservableBuffer


object PathFinderModel {

  val size = 30

  val sleepTime = 25

  @volatile var counter = 0

  private var finder : Finder = new BreadthFirst()

  val algorithms: ObservableBuffer[String] = ObservableBuffer(
    "Breadth First",
    "Manhattan Distance"
  )

  def setAlgorithm(index: Int): Unit = {
    finder = index match {
      case 1 => new HeuristicSearch()
      case _ => new BreadthFirst()
    }
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

  def restart(): Unit = {
  }

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
    for (x <- 0 until  size ; y <- 0 until size) {
      board(x)(y)() = EMPTY
    }
    _startPoint = Point(0,0)
    _finishPoint = Point(size-1, size-1)
    board(0)(0)() = START
    board(size-1)(size-1)() = FINISH
    setRunning(false)
  }

  val cleanPoint: PartialFunction[Point, Unit] = {
    case p: Point if board(p.y)(p.x)() == PATH || board(p.y)(p.x)() == CANDIDATE_PATH => board(p.y)(p.x)() = EMPTY
  }

  val cleanPoint1: PartialFunction[Point, Unit] = (p: Point) => board(p.y)(p.x)() match {
    case PATH | CANDIDATE_PATH => board(p.y)(p.x)() = EMPTY
  }

  def clean(): Unit = {
    setRunning(true)
    points.collect(cleanPoint1)
    setRunning(false)
  }

  def setObstacle(p: Point): Unit = {
    if ( ! isRunning ) {
      val cur: Owner = getPointValue(p)
      cur match {
        case OBSTACLE => setPointValue(p, EMPTY)
        case EMPTY => setPointValue(p, OBSTACLE)
        case _ =>
      }
    }
  }

  def setPathCandidate(p: Point): Unit = {
    if ( p != startPoint && p != finishPoint ) {
      board(p.y)(p.x)() = CANDIDATE_PATH
      counter += 1
    }
  }

  def setPath(p: Point): Unit = {
    if ( p != startPoint && p != finishPoint ) {
      board(p.y)(p.x)() = PATH
      counter += 1
    }
  }

  def startPoint: Point = _startPoint

  def startPoint_=(p: Point) {
    if ( ! isRunning && p != finishPoint ) {
      setPointValue(_startPoint, EMPTY)
      _startPoint = p
      setPointValue(_startPoint, START)
    }
  }

  def finishPoint: Point = _finishPoint

  def finishPoint_=(p: Point) {
    if ( ! isRunning && p != startPoint ) {
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

  private def setValue(x: Int, y: Int, value: Int): Unit = {
    board(x)(y)() = Owner(value)
    counter += 1
  }

}
