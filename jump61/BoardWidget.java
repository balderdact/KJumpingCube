
package jump61;

import ucb.gui.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;

import static jump61.Side.*;

/** A GUI component that displays a Jump61 board, and converts mouse clicks
 *  on that board to commands that are sent to the current Game.
 *  @author Randy Shi
 */
class BoardWidget extends Pad {

    /** Used to place spots in the center. */
    private static final int CENTER_SPOT = 27;
    /** Used to place spots in corners. */
    private static final int CORNER_ONE = 14;
    /** Used to place spots in corners. */
    private static final int CORNER_TWO = 40;
    /** Length of the side of one square in pixels. */
    private static final int SQUARE_SIZE = 50;
    /** Width and height of a spot. */
    private static final int SPOT_DIM = 8;
    /** Minimum separation of center of a spot from a side of a square. */
    private static final int SPOT_MARGIN = 10;
    /** Width of the bars separating squares in pixels. */
    private static final int SEPARATOR_SIZE = 3;
    /** Width of square plus one separator. */
    private static final int SQUARE_SEP = SQUARE_SIZE + SEPARATOR_SIZE;

    /** Colors of various parts of the displayed board. */
    private static final Color
        NEUTRAL = Color.WHITE,
        SEPARATOR_COLOR = Color.BLACK,
        SPOT_COLOR = Color.BLACK,
        RED_TINT = new Color(255, 200, 200),
        BLUE_TINT = new Color(200, 200, 255);

    /** A new BoardWidget that monitors and displays GAME and its Board, and
     *  converts mouse clicks to commands to COMMANDWRITER. */
    BoardWidget(Game game, PrintWriter commandWriter) {
        _game = game;
        _board = _bufferedBoard = game.getBoard();
        _side = _board.size() * SQUARE_SEP + SEPARATOR_SIZE;
        setPreferredSize(_side, _side);
        setMouseHandler("click", this, "doClick");
        _commandOut = commandWriter;
        if (!(_game.getPlayer(RED) instanceof AI)) {
            playersTurn = true;
        } else {
            playersTurn = false;
        }
    }

    /* .update and .paintComponent are synchronized because they are called
     *  by three different threads (the main thread, the thread that
     *  responds to events, and the display thread.  We don't want the
     *  saved copy of our Board to change while it is being displayed. */

    /** Update my display depending on any changes to my Board.  Here, we
     *  save a copy of the current Board (so that we can deal with changes
     *  to it only when we are ready for them), and resize the Widget if the
     *  size of the Board should change. */
    synchronized void update() {
        Player currentPlayer = _game.getPlayer(_board.whoseMove());
        if (currentPlayer instanceof AI && !playersTurn) {
            currentPlayer.makeMove();
            playersTurn = true;
        }
        _bufferedBoard = new MutableBoard(_board);
        int side0 = _side;
        _side = _board.size() * SQUARE_SEP + SEPARATOR_SIZE;
        if (side0 != _side) {
            setPreferredSize(_side, _side);
        }
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(SEPARATOR_COLOR);
        for (int k = 0; k <= _side; k += SQUARE_SEP) {
            g.fillRect(0, k, _side, SEPARATOR_SIZE);
            g.fillRect(k, 0, SEPARATOR_SIZE, _side);
        }
        for (int i = 1; i <= _board.size(); i++) {
            for (int j = 1; j <= _board.size(); j++) {
                displaySpots(g, i, j);
            }
        }
    }

    /** Fills a square at R and C with its appropriate color
     *  and displays it on G. */
    private void fillSquareColor(Graphics2D g, int r, int c) {
        int n = _board.sqNum(r, c), i = SQUARE_SEP;
        if (_board.get(n).getSide() == WHITE) {
            g.setColor(NEUTRAL);
        } else if (_board.get(n).getSide() == RED) {
            g.setColor(RED_TINT);
        } else {
            g.setColor(BLUE_TINT);
        }
        g.fillRect(SEPARATOR_SIZE + i * (r - 1),
                SEPARATOR_SIZE + i * (c - 1),
                SQUARE_SIZE, SQUARE_SIZE);
    }

    /** Color and display the spots on the square at row R and column C
     *  on G.  (Used by paintComponent). */
    private void displaySpots(Graphics2D g, int r, int c) {
        fillSquareColor(g, r, c);
        switch (_board.get(_board.sqNum(r, c)).getSpots()) {
        case 1:
            spot(g, CENTER_SPOT + SQUARE_SEP * (r - 1),
                    CENTER_SPOT + SQUARE_SEP * (c - 1));
            break;
        case 2:
            spot(g, CORNER_ONE + SQUARE_SEP * (r - 1),
                    CORNER_ONE + SQUARE_SEP * (c - 1));
            spot(g, CORNER_TWO + SQUARE_SEP * (r - 1),
                    CORNER_TWO + SQUARE_SEP * (c - 1));
            break;
        case 3:
            spot(g, CORNER_ONE + SQUARE_SEP * (r - 1),
                    CORNER_ONE + SQUARE_SEP * (c - 1));
            spot(g, CENTER_SPOT + SQUARE_SEP * (r - 1),
                    CENTER_SPOT + SQUARE_SEP * (c - 1));
            spot(g, CORNER_TWO + SQUARE_SEP * (r - 1),
                    CORNER_TWO + SQUARE_SEP * (c - 1));
            break;
        default:
            spot(g, CORNER_ONE + SQUARE_SEP * (r - 1),
                    CORNER_ONE + SQUARE_SEP * (c - 1));
            spot(g, CORNER_ONE + SQUARE_SEP * (r - 1),
                    CORNER_TWO + SQUARE_SEP * (c - 1));
            spot(g, CORNER_TWO + SQUARE_SEP * (r - 1),
                    CORNER_ONE + SQUARE_SEP * (c - 1));
            spot(g, CORNER_TWO + SQUARE_SEP * (r - 1),
                    CORNER_TWO + SQUARE_SEP * (c - 1));
            break;
        }
    }

    /** Draw one spot centered at position (X, Y) on G. */
    private void spot(Graphics2D g, int x, int y) {
        g.setColor(SPOT_COLOR);
        g.fillOval(x - SPOT_DIM / 2, y - SPOT_DIM / 2, SPOT_DIM, SPOT_DIM);
    }

    /** Respond to the mouse click depicted by EVENT. */
    public void doClick(MouseEvent event) {
        int x = event.getX() - SEPARATOR_SIZE,
            y = event.getY() - SEPARATOR_SIZE;
        int r = 1;
        int c = 1;
        for (; c <= _board.size(); c++) {
            if (y > SQUARE_SEP * (c - 1)
                    && y < SQUARE_SEP * c) {
                break;
            }
        }
        for (; r <= _board.size(); r++) {
            if (x > SQUARE_SEP * (r - 1)
                    && x < SQUARE_SEP * r) {
                break;
            }
        }
        if (!_game.gameInProgress()) {
            return;
        }
        try {
            if (!(_game.getPlayer(_board.whoseMove()) instanceof AI)
                    && playersTurn) {
                _game.makeMove(r, c);
                if (_game.getPlayer(_board.whoseMove()) instanceof AI) {
                    playersTurn = false;
                }
            }
        } catch (AssertionError e) {
            return;
        }
    }

    /** True if a human player is taking a turn. */
    private boolean playersTurn;
    /** The Game I am playing. */
    private Game _game;
    /** The Board I am displaying. */
    private Board _board;
    /** An internal snapshot of _board (to prevent race conditions). */
    private Board _bufferedBoard;
    /** Dimension in pixels of one side of the board. */
    private int _side;
    /** Destination for commands derived from mouse clicks. */
    private PrintWriter _commandOut;
}
