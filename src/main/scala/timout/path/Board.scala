package timout.path

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, ChoiceBox}
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight, Text}
import scalafx.scene.{Node, Scene}

object Board extends JFXApp {

  private val model = PathFinderModel

  private val algoChoiceBox = new ChoiceBox[String] {
    prefWidth = 100
    items = model.algorithms
    onAction = () => {
      val i = selectionModel().selectedIndex()
      model.setAlgorithm(i)
    }
  }
  algoChoiceBox.selectionModel().selectFirst()

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

  private val buttonPane = new HBox() {
    padding = Insets(10)
    spacing = 10
    alignment = Pos.CenterLeft
    children = List(algoChoiceBox, findButton, resetButton, cleanButton)
  }

  private val maze = new VBox() {
    children = Seq( /*createTitle(), */ buttonPane, createBackground(), tiles() )
  }

  stage = new PrimaryStage() {
    scene = new Scene(400, 400) {
      title = "Path Finder"
      root = maze
    }
  }

  private def createTitle() = new TilePane {
    snapToPixel = false
    children = List(
      new StackPane {
        style = "-fx-background-color: black"
        children = new Text {
          text = "Path"
          font = Font.font(null, FontWeight.Bold, 12)
          fill = Color.White
          alignmentInParent = Pos.CenterRight
        }
      },
      new Text {
        text = "Finder"
        font = Font.font(null, FontWeight.Bold, 12)
        alignmentInParent = Pos.CenterLeft
      })
    prefTileHeight = 50
    prefTileWidth <== parent.selectDouble("width") / 2
  }

  private def createBackground() = new Region {
    style = "-fx-background-color: radial-gradient(radius 100%, white, gray)"
  }

  private def tiles(): Node = {
    val board = new GridPane()
    for (i <- 0 until  model.size ; j <- 0 until model.size) {
      val p = Point(i, j)
      val square = new BoardSquare(p, model.getPointProperty(p))
      board.add(square, i, j)
    }
    board
  }
}
