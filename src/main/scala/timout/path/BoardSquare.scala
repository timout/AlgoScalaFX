package timout.path

import javafx.scene.{layout => jfxsl}
import scalafx.Includes._
import scalafx.animation.FadeTransition
import scalafx.beans.binding.Bindings
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.{HPos, VPos}
import scalafx.scene.control.MenuItem._
import scalafx.scene.control.{ContextMenu, MenuItem}
import scalafx.scene.effect.{Light, Lighting}
import scalafx.scene.layout.Region
import scalafx.util.Duration

class BoardSquare(val point: Point, _owner: ObjectProperty[Owner]) extends Region {

  private val model = PathFinderModel

  private val empty = "-fx-background-color: green"
  private val obstacle = "-fx-background-color: red"
  private val start = "-fx-background-color: #00FFFF"
  private val finish = "-fx-background-color: blue"
  private val candidate_path = "-fx-background-color: darkgrey"
  private val path = "-fx-background-color: white"

  val owner = new ObjectProperty[Owner](this, "owner", EMPTY)
  owner <== _owner

  private val styleBinding = Bindings.createStringBinding(
    () => owner.value match {
      case PATH => path
      case CANDIDATE_PATH => candidate_path
      case OBSTACLE => obstacle
      case START => start
      case FINISH => finish
      case _ => empty
    },
    owner
  )

  private val highlight = new Region {
    opacity = 0
    style = "-fx-border-width: 3; -fx-border-color: dodgerblue"
  }

  override val delegate: jfxsl.Region = new jfxsl.Region {
    getChildren.add(highlight)

    protected override def layoutChildren(): Unit = {
      layoutInArea(highlight, 0, 0, getWidth, getHeight, getBaselineOffset, HPos.Center, VPos.Center)
    }
  }

  private val highlightTransition = new FadeTransition {
    node = highlight
    duration = Duration(200)
    fromValue = 0
    toValue = 1
  }

  style <== styleBinding

  effect = new Lighting {
    light = new Light.Distant {
      azimuth = -135
      elevation = 30
    }
  }

  prefHeight = 200
  prefWidth = 200

  onMouseEntered = () => {
    //if (ReversiModel.legalMove(x, y).get) {
      highlightTransition.rate() = 1
      highlightTransition.play()
//    if ( BoardSquare.isDragging ) {
//      model.setObstacle(point)
//    }
    //}
  }


  onMouseExited = () => {
    highlightTransition.rate = -1
    highlightTransition.play()
  }

//  onMouseDragged = (e) => {
//    //model.setObstacle(point)
//    println(s"${point} mouse dragged")
//    e.consume()
//  }

//  onMouseDragEntered = (e) => {
//    //model.setObstacle(point)
//    println(s"${point} mouse drag enterd")
//    e.consume()
//  }
//
//  onDragDetected = (e) => {
//    BoardSquare.isDragging = true;
//    println(s"${point} drag detected")
//    //e.consume()
//    this.mouseTransparent() = true
//  }

//  onDragDone = (e) => {
//    BoardSquare.isDragging = false
//    println(s"${point} drag done")
//    e.consume()
//  }
//
//  onMouseDragOver = () => {
//    println(s"${point} mouse drag over")
//    super.onMouseDragOver
//  }
//


  onMouseClicked = (e) => {
    if ( ! BoardSquare.contextMenu.isShowing ) {
      model.setObstacle(point)
//      highlightTransition.rate() = -1
//      highlightTransition.play()
    }
  }

  onContextMenuRequested = e => {
    BoardSquare.currentPoint = this.point
    BoardSquare.contextMenu.show(this, e.getScreenX, e.getScreenY)
  }

//  image.setOnContextMenuRequested(e ->
//    contextMenu.show(image, e.getScreenX(), e.getScreenY()));
}

object BoardSquare {

  private val model = PathFinderModel

  private var _currentPoint: Option[Point] = Option.empty

  var isDragging: Boolean = false

  def currentPoint: Option[Point] = _currentPoint

  def currentPoint_=(p: Point): Unit = {
    _currentPoint = Option(p)
  }

  val contextMenu: ContextMenu = new ContextMenu {
    items ++= Seq(
      new MenuItem("Set Start") {
        onAction = e => BoardSquare.currentPoint.foreach( p => BoardSquare.model.startPoint = p )
      },
      new MenuItem("Set Finish") {
        onAction = e => BoardSquare.currentPoint.foreach( p => BoardSquare.model.finishPoint = p )
      })
  }

}