package timout.path

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, ChoiceBox, Label, Tooltip}
import scalafx.scene.layout._
import scalafx.scene.{Node, Scene}

object Board extends JFXApp {

  private val model = PathFinderModel

  private val algoChoiceBox = new ChoiceBox[String] {
    prefWidth = 100
    items = model.algorithms
    onAction = () => model.setAlgorithm(selectionModel().selectedIndex())
    tooltip = Tooltip("Finder Algorithm")
  }
  algoChoiceBox.selectionModel().selectFirst()

  private val onDragChoice = new ChoiceBox[String] {
    prefWidth = 100
    items = model.onDragActions
    onAction = () => model.setOnDrag(selectionModel().selectedIndex())
    tooltip = Tooltip("On Mouse Drag Action")
  }
  onDragChoice.selectionModel().selectFirst()

  private val sleepTimeBox = new ChoiceBox[String] {
    prefWidth = 25
    items = model.sleepTimes
    onAction = () => model.setSleepTime(selectionModel().selectedIndex())
    tooltip = Tooltip("Sleep Time")
  }
  sleepTimeBox.selectionModel().select(1)

  private val findButton = new Button() {
    text = "Find"
    onAction = () => PathFinderModel.startFinder()
    disable <== model.isRunningState
  }

  private val resetButton = new Button() {
    text = "Reset"
    onAction = () => PathFinderModel.reset()
    disable <== model.isRunningState
  }

  private val cleanButton = new Button() {
    text = "Clean"
    onAction = () => PathFinderModel.clean()
    disable <== model.isRunningState
  }

  private val confPane = new HBox() {
    padding = Insets(10)
    spacing = 10
    children = List(
      new HBox(new Label("Sleep Time: "), sleepTimeBox),
      new HBox(new Label("On Drag: "), onDragChoice),
    )
  }

  private val buttonPane = new HBox() {
    padding = Insets(10)
    spacing = 10
    alignment = Pos.CenterLeft
    children = List(algoChoiceBox, findButton, resetButton, cleanButton)
  }

  private val maze = new VBox() {
    children = Seq( buttonPane, confPane, createBackground(), tiles() )
  }

  stage = new PrimaryStage() {
    scene = new Scene(500, 500) {
      title = "Path Finder"
      root = maze
    }
  }

  private def createBackground() = new Region {
    style = "-fx-background-color: radial-gradient(radius 100%, white, gray)"
  }

  private def tiles(): Node = {
    val board = new GridPane()
    for (i <- 0 until  model.size ; j <- 0 until model.size) {
      val p = Point(i, j)
      val square = new BoardSquare(p, model.getPointProperty(p), board)
      board.add(square, i, j)
    }
    board
  }
}
