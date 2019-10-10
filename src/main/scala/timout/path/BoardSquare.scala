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
import scalafx.scene.layout.{GridPane, Region}
import scalafx.util.Duration

class BoardSquare(val point: Point, _owner: ObjectProperty[Owner], board: GridPane) extends Region {

  private val model = PathFinderModel

  private val empty = "-fx-background-color: green"
  private val obstacle = "-fx-background-color: red"
  private val start = "-fx-background-color: #00FFFF"
  private val finish = "-fx-background-color: blue"
  private val candidate_path = "-fx-background-color: darkgrey"
  private val frontier_path = "-fx-background-color: yellow"
  private val path = "-fx-background-color: white"

  val owner = new ObjectProperty[Owner](this, "owner", EMPTY)
  owner <== _owner

  private val styleBinding = Bindings.createStringBinding(
    () => owner.value match {
      case PATH => path
      case CANDIDATE_PATH => candidate_path
      case FRONTIER_PATH => frontier_path
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
    highlightTransition.rate() = 1
    highlightTransition.play()
  }


  onMouseExited = (e) => {
    highlightTransition.rate = -1
    highlightTransition.play()
  }

  onMouseDragged = (e) => {
    val y = board.getLayoutY
    for ( node <- board.getChildren ) {
        if ( node.getBoundsInParent.contains(e.getSceneX, e.getSceneY - y) ) {
          val c = GridPane.getColumnIndex(node)
          val r = GridPane.getRowIndex(node)
          model.setPointDragValue(Point(c, r))
          //println(s"node over: $r :   $c  $node")
        }
    }
    //println(s"${point} mouse dragged ${ e.getSceneX} : ${e.getSceneY}")
  }

//  onMouseDragEntered = (e) => {
//    //model.setObstacle(point)
//    println(s"${point} mouse drag enterd ${ e.getSceneX} : ${e.getSceneY}")
//    //e.consume()
//  }
//
//  onDragDetected = (e) => {
//    //BoardSquare.isDragging = true;
//    println(s"${point} drag detected ${ e.getSceneX} : ${e.getSceneY}")
//    //e.consume()
//    this.mouseTransparent() = true
//  }
//
//  onDragExited = (e) => {
//    println(s"${point} drag exeted ${ e.getSceneX} : ${e.getSceneY}")
//  }
//
//  onDragDone = (e) => {
//    println(s"${point} drag done ${ e.getSceneX} : ${e.getSceneY}")
//  }
//
//  onDragDropped = (e) => {
//    println(s"${point} drag dropped ${ e.getSceneX} : ${e.getSceneY}")
//  }

//  onMouseDragReleased = (e) => {
//    println(s"${point} drag released ${ e.getSceneX} : ${e.getSceneY}")
//  }

//  onDragDone = (e) => {
//    BoardSquare.isDragging = false
//    println(s"${point} drag done ${ e.getSceneX} : ${e.getSceneY}")
//   // e.consume()
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