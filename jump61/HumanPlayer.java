
package jump61;

/** A Player that gets its moves from manual input.
 *  @author Randy Shi
 */
class HumanPlayer extends Player {

    /** A new player initially playing COLOR taking manual input of
     *  moves from GAME's input source. */
    HumanPlayer(Game game, Side color) {
        super(game, color);
    }

    @Override
    /** Retrieve moves using getGame().getMove() until a legal one is found and
     *  make that move in getGame().  Report erroneous moves to player. */
    void makeMove() {
        int[] move = new int[2];
        if (getGame().getMove(move)) {
            try {
                getGame().makeMove(move[0], move[1]);
            } catch (AssertionError e) {
                getGame().reportError("invalid move: %d %d",
                        move[0], move[1]);
            }
        }
    }

}
