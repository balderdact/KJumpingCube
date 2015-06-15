
package jump61;

import java.util.ArrayList;
import java.util.List;

/** An automated Player.
 *  @author Randy Shi
 *  I got the idea of making different strategies from Jae Lee.
 *  My minimax function is derived from DSIJ.
 */
class AI extends Player {

    /** A new player of GAME initially playing COLOR that chooses
     *  moves automatically.
     */
    AI(Game game, Side color) {
        super(game, color);
    }

    @Override
    void makeMove() {
        Board board = getGame()._board;
        ArrayList<Integer> allMoves = new ArrayList<Integer>();
        ArrayList<Integer> corners = new ArrayList<Integer>();
        ArrayList<Integer> sides = new ArrayList<Integer>();
        ArrayList<Integer> inners = new ArrayList<Integer>();
        addDesignations(allMoves, 0);
        addDesignations(corners, 2);
        addDesignations(sides, 3);
        addDesignations(inners, 4);
        int bestMove = -1;
        if (bestSpill(board, allMoves) != -1) {
            bestMove = bestSpill(board, allMoves);
        } else if (bestMove(board, corners, 2) != -1) {
            bestMove = bestMove(board, corners, 2);
        } else if (bestMove(board, sides, 3) != -1) {
            bestMove = bestMove(board, sides, 3);
        } else if (bestMove(board, inners, 4) != -1) {
            bestMove = bestMove(board, inners, 4);
        } else {
            bestMove = bestMinimaxVal(board, allMoves);
        }
        try {
            getGame().makeMove(board.row(bestMove), board.col(bestMove));
        } catch (AssertionError e) {
            bestMove = 0;
            while (!board.isLegal(getSide(), bestMove)) {
                bestMove++;
            }
            getGame().makeMove(board.row(bestMove), board.col(bestMove));
        }
        if (getSide() == Side.RED) {
            getGame().message("Red moves %d %d.\n", board.row(bestMove),
                    board.col(bestMove));
        } else {
            getGame().message("Blue moves %d %d.\n", board.row(bestMove),
                    board.col(bestMove));
        }
    }

    /** Adds designated squares to MOVES. N tells this method which
     *  moves are being designated:
     *  N == 0 -> MOVES is all possible moves;
     *  N == 2 -> MOVES is all corner squares;
     *  N == 3 -> MOVES is all side squares;
     *  N == 4 -> MOVES is all inner squares. */
    private void addDesignations(ArrayList<Integer> moves, int n) {
        Board b = getGame()._board;
        if (n != 0) {
            for (int i = 0; i < Math.pow(b.size(), 2); i++) {
                if (b.isLegal(getSide(), i) && b.neighbors(i) == n) {
                    moves.add(i);
                }
            }
        } else {
            for (int i = 0; i < Math.pow(b.size(), 2); i++) {
                if (b.isLegal(getSide(), i)) {
                    moves.add(i);
                }
            }
        }
    }

    /** Adds a (square at {R, C})'s neighbors that are occupied by
     *  the opponent of P to NEIGHBORS. */
    private void addOppNeighbors(List<Integer> neighbors,
            int r, int c, Side p) {
        Board b = getGame()._board;
        int prevRow = r - 1, nextRow = r + 1,
                prevCol = c - 1, nextCol = c + 1;
        if (prevRow >= 1) {
            if (b._board.get(b.sqNum(prevRow, c)).getSide()
                    == p.opposite()) {
                neighbors.add(b.sqNum(prevRow, c));
            }
        }
        if (prevCol >= 1) {
            if (b._board.get(b.sqNum(r, prevCol)).getSide()
                    == p.opposite()) {
                neighbors.add(b.sqNum(r, prevCol));
            }
        }
        if (nextRow <= b.size()) {
            if (b._board.get(b.sqNum(nextRow, c)).getSide()
                    == p.opposite()) {
                neighbors.add(b.sqNum(nextRow, c));
            }
        }
        if (nextCol <= b.size()) {
            if (b._board.get(b.sqNum(r, nextCol)).getSide()
                    == p.opposite()) {
                neighbors.add(b.sqNum(r, nextCol));
            }
        }
    }

    /** If there are squares on B in MOVES that the AI can spill that
     *  give an advantage, then this method finds the spill with the
     *  highest heuristic value and returns that move. */
    int bestSpill(Board b, ArrayList<Integer> moves) {
        ArrayList<Integer> bestSquares = new ArrayList<Integer>();
        for (int move : moves) {
            ArrayList<Integer> neighborKeys = new ArrayList<Integer>();
            addOppNeighbors(neighborKeys,
                    b.row(move), b.col(move), getSide());
            for (int key: neighborKeys) {
                Square neighbor = b.get(key);
                if (neighbor.getSpots() == b.neighbors(key)
                        && b.get(move).getSpots() == b.neighbors(move)) {
                    bestSquares.add(move);
                }
            }
        }
        if (bestSquares.isEmpty()) {
            return -1;
        }
        int bestEval = Integer.MIN_VALUE;
        int bestMove = -1;
        for (int square : bestSquares) {
            Board next = new MutableBoard(b);
            next.addSpot(getSide(), square);
            int minimax = bestMinimaxVal(next, bestSquares);
            if (minimax == Integer.MAX_VALUE) {
                return square;
            }
            if (minimax > bestEval) {
                bestEval = minimax;
                bestMove = square;
            }
        }
        return bestMove;
    }

    /** Returns the best move to make on BOARD from DESIGNATIONS.
     *  N tells this method which moves are being designated:
     *  N == 2 -> DESIGNATIONS is all corner squares;
     *  N == 3 -> DESIGNATIONS is all side squares;
     *  N == 4 -> DESIGNATIONS is all inner squares. */
    int bestMove(Board board, ArrayList<Integer> designations, int n) {
        for (int move : designations) {
            ArrayList<Integer> neighborKeys = new ArrayList<Integer>();
            addOppNeighbors(neighborKeys,
                    board.row(move), board.col(move), getSide());
            boolean okayToMove = true;
            for (int key: neighborKeys) {
                Square neighbor = board.get(key);
                if (neighbor.getSpots() <= board.neighbors(key)
                        && board.get(move).getSpots()
                        < neighbor.getSpots()) {
                    okayToMove = false;
                }
            }
            if (board.get(move).getSpots() < n && okayToMove) {
                return move;
            }
        }
        return -1;
    }

    /** Returns the best minimax value from ALLMOVES for B. */
    private int bestMinimaxVal(Board b, ArrayList<Integer> allMoves) {
        int max = Integer.MIN_VALUE;
        int maxMove = Integer.MIN_VALUE;
        for (int move : allMoves) {
            if (b.isLegal(getSide(), move)) {
                Board next = new MutableBoard(b);
                next.addSpot(getSide(), move);
                int currVal = minimax(getSide(), next, 1, Integer.MIN_VALUE);
                if (max < currVal) {
                    maxMove = move;
                    max = currVal;
                }
            }
        }
        return maxMove;
    }

    /** Returns the best move possible for P given B. Recurses
     *  to a depth of D and uses CUTOFF to perform pruning. */
    private int minimax(Side p, Board b, int d, int cutoff) {
        if (b.numOfSide(p) == Math.pow(b.size(), 2)) {
            return Integer.MAX_VALUE;
        }
        if (b.numOfSide(p.opposite()) == Math.pow(b.size(), 2)) {
            return Integer.MIN_VALUE;
        }
        if (d == 0) {
            return staticEval(p, b);
        }
        int bestSoFar = Integer.MIN_VALUE;
        for (int m = 0; m < b.size() * b.size(); m++) {
            if (b.isLegal(p, m)) {
                Board next = new MutableBoard(b);
                next.addSpot(p, m);
                int response = minimax(p.opposite(), next, d - 1, -bestSoFar);
                if (cutoff < response) {
                    cutoff = response;
                    bestSoFar = m;
                }
            }
        }
        return bestSoFar;
    }

    /** Returns heuristic value of board B for player P.
     *  Higher is better for P. */
    private int staticEval(Side p, Board b) {
        if (b.numOfSide(p) == b.size() * b.size()) {
            return Integer.MAX_VALUE;
        }
        if (b.numOfSide(p.opposite())
            == b.size() * b.size()) {
            return Integer.MIN_VALUE;
        }
        return evaluate(p, b) - evaluate(p.opposite(), b);
    }

    /** Returns the heuristic of board B for player P. */
    private int evaluate(Side p, Board b) {
        ArrayList<Integer> moves = new ArrayList<Integer>();
        for (int i = 0; i < Math.pow(b.size(), 2); i++) {
            if (b.isLegal(p, i)) {
                moves.add(i);
            }
        }
        int max = 0;
        int oppMax = 0;
        for (int move : moves) {
            if (b.isLegal(getSide(), move)) {
                Board next = new MutableBoard(b);
                next.addSpot(p, move);
                ArrayList<Integer> oppMoves = new ArrayList<Integer>();
                for (int i = 0; i < Math.pow(next.size(), 2); i++) {
                    if (next.isLegal(p.opposite(), i)) {
                        oppMoves.add(i);
                    }
                }
                for (int oppMove : oppMoves) {
                    if (next.isLegal(p.opposite(), oppMove)) {
                        Board nextNext = new MutableBoard(next);
                        nextNext.addSpot(p.opposite(), oppMove);
                        int numNewOppSides = helperVal(p.opposite(),
                                nextNext);
                        if (numNewOppSides > oppMax) {
                            oppMax = numNewOppSides;
                        }
                    }
                }
                int numNewSides = helperVal(p, next);
                if (numNewSides - oppMax > max) {
                    max = numNewSides - oppMax;
                }
            }
        }
        return helperVal(p, b) + max;
    }

    /** Returns a helper value of board B for player P for
     *  the heuristic value. */
    private int helperVal(Side p, Board b) {
        int numSides = ((MutableBoard) b).numOfSide(p);
        boolean full = numSides == b.size() * b.size();
        return full ? Integer.MAX_VALUE : numSides;
    }

}
