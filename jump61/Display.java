
package jump61;

import ucb.gui.TopLevel;
import ucb.gui.LayoutSpec;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Observable;
import java.util.Observer;

import static jump61.Side.*;

/** The GUI controller for jump61.  To require minimal change to textual
 *  interface, we adopt the strategy of converting GUI input (mouse clicks)
 *  into textual commands that are sent to the Game object through a
 *  a Writer.  The Game object need never know where its input is coming from.
 *  A Display is an Observer of Games and Boards so that it is notified when
 *  either changes.
 *  @author Randy Shi
 */
class Display extends TopLevel implements Observer {

    /** A new window with given TITLE displaying GAME, and using COMMANDWRITER
     *  to send commands to the current game. */
    Display(String title, Game game, Writer commandWriter) {
        super(title, true);
        _game = game;
        _board = game.getBoard();
        _commandOut = new PrintWriter(commandWriter);
        _boardWidget = new BoardWidget(game, _commandOut);
        add(_boardWidget, new LayoutSpec("y", 1, "width", 2));
        addMenuButton("Game->New Game", "start");
        addMenuButton("Game->Quit", "quit");
        addMenuRadioButton("Options->Red Manual", "reds",
                isSelected("Options->Red Manual"), "manualRed");
        select("Options->Red Manual", true);
        addMenuRadioButton("Options->Red AI", "reds",
                isSelected("Options->Red AI"), "redAI");
        addMenuRadioButton("Options->Blue Manual", "blues",
                isSelected("Options->Blue Manual"), "manualBlue");
        addMenuRadioButton("Options->Blue AI", "blues",
                isSelected("Options->Blue AI"), "blueAI");
        select("Options->Blue AI", true);
        addMenuButton("Options->Board Size...", "setSize");
        setEnabled(true, "Options->Red Manual", "Options->Blue AI");
        _board.addObserver(this);
        _game.addObserver(this);
        display(true);
    }

    /** Response to "Quit" button click. */
    void quit(String dummy) {
        System.exit(0);
    }

    /** Response to "New Game" button click. */
    void start(String dummy) {
        _game.clear();
        _game.restartGame();
    }

    /** Response to "Red Manual" button click. */
    void manualRed(String dummy) {
        _game.setManual(RED);
    }

    /** Response to "Red AI" button click. */
    void redAI(String dummy) {
        _game.setAuto(RED);
    }

    /** Response to "Blue Manual" button click. */
    void manualBlue(String dummy) {
        _game.setManual(BLUE);
    }

    /** Response to "Blue AI" button click. */
    void blueAI(String dummy) {
        _game.setAuto(BLUE);
    }

    /** Response to "Board Size..." button click. */
    void setSize(String dummy) {
        String size = getTextInput("Enter number of rows and columns (2--10)",
                "Size", "question", "");
        try {
            _game.setSize(Integer.parseInt(size));
        } catch (NumberFormatException e) {
            setSize(dummy);
        }
    }

    @Override
    public void update(Observable obs, Object obj) {
        _boardWidget.update();
        frame.pack();
        _boardWidget.repaint();
    }

    /** The current game that I am controlling. */
    private Game _game;
    /** The board maintained by _game (readonly). */
    private Board _board;
    /** The widget that displays the actual playing board. */
    private BoardWidget _boardWidget;
    /** Writer that sends commands to our game. */
    private PrintWriter _commandOut;
}
