
package jump61;

import static jump61.Side.*;
import static jump61.Square.square;

import java.util.HashMap;

/** A Jump61 board state that may be modified.
 *  @author Randy Shi
 *  I got help from David Au for the overfull handling
 *  when I had a problem with checking overfull iteratively.
 */
class MutableBoard extends Board {

    /** An N x N board in initial configuration. */
    MutableBoard(int N) {
        initializeBoard(N);
    }

    /** A board whose initial contents are copied from BOARD0, but whose
     *  undo history is clear. */
    MutableBoard(Board board0) {
        copy(board0);
    }


    @Override
    void clear(int N) {
        initializeBoard(N);
        announce();
    }

    /** (Re)initialize me to a cleared board with N squares on a side. */
    void initializeBoard(int N) {
        _size = N;
        _spots = N * N;
        _board = new HashMap<Integer, Square>();
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= N; j++) {
                _board.put(sqNum(i, j), Square.INITIAL);
            }
        }
    }

    /** Copies the contents of BOARD to the board. */
    void copyToBoard(Board board) {
        for (int i = 1; i <= size(); i++) {
            for (int j = 1; j <= size(); j++) {
                int key = sqNum(i, j);
                Side side = board.get(key).getSide();
                int spots = board.get(key).getSpots();
                _spots += board.get(key).getSpots()
                        - get(key).getSpots();
                _board.put(key, square(side, spots));
            }
        }
    }

    @Override
    void copy(Board board) {
        initializeBoard(board.size());
        copyToBoard(board);
    }

    @Override
    int size() {
        return _size;
    }

    @Override
    Square get(int n) {
        return _board.get(n);
    }

    @Override
    int numOfSide(Side side) {
        int numSides = 0;
        for (int i = 1; i <= size(); i++) {
            for (int j = 1; j <= size(); j++) {
                int key = sqNum(i, j);
                boolean hasSide = get(key).getSide().equals(side);
                numSides += hasSide ? 1 : 0;
            }
        }
        return numSides;
    }

    @Override
    int numPieces() {
        return _spots;
    }

    @Override
    void addSpot(Side player, int r, int c) {
        addSpot(player, sqNum(r, c));
        announce();
    }

    @Override
    void addSpot(Side player, int n) {
        _spots++;
        addSpots(player, n);
        announce();
    }

    /** Adds spots for PLAYER at N and takes care of spilling. */
    void addSpots(Side player, int n) {
        if (isLegal(player, n)) {
            int newSpotNum = get(n).getSpots() + 1;
            if (newSpotNum > neighbors(n)) {
                newSpotNum = 1;
            }
            _board.put(n, square(player, newSpotNum));
            if (newSpotNum == 1 && getWinner() == null) {
                jump(player, n);
            }
        }
    }

    /** Has the spots of PLAYER at square I jump to their places. */
    void jump(Side player, int i) {
        int r = row(i), c = col(i);
        if (c - 1 > 0) {
            i = sqNum(r, c - 1);
            _board.put(i, square(player, get(i).getSpots()));
            addSpots(player, i);
        }
        if (c + 1 <= size()) {
            i = sqNum(r, c + 1);
            _board.put(i, square(player, get(i).getSpots()));
            addSpots(player, i);
        }
        if (r - 1 > 0) {
            i = sqNum(r - 1, c);
            _board.put(i, square(player, get(i).getSpots()));
            addSpots(player, i);
        }
        if (r + 1 <= size()) {
            i = sqNum(r + 1, c);
            _board.put(i, square(player, get(i).getSpots()));
            addSpots(player, i);
        }
    }

    @Override
    void set(int r, int c, int num, Side player) {
        internalSet(sqNum(r, c), square(player, num));
    }

    @Override
    void set(int n, int num, Side player) {
        internalSet(n, square(player, num));
        announce();
    }

    /** Set the contents of the square with index IND to SQ. Update counts
     *  of numbers of squares of each color.  */
    private void internalSet(int ind, Square sq) {
        _spots += sq.getSpots() - get(ind).getSpots();
        _board.put(ind, sq);
    }

    /** Notify all Observers of a change. */
    private void announce() {
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MutableBoard)) {
            return obj.equals(this);
        } else {
            if (size() != ((MutableBoard) obj).size()) {
                return false;
            }
            for (int i = 1; i <= size(); i++) {
                for (int j = 1; j <= size(); j++) {
                    if (get(sqNum(i, j)).getSpots() != ((MutableBoard)
                            obj).get(sqNum(i, j)).getSpots()) {
                        return false;
                    }
                    if (!get(sqNum(i, j)).getSide().equals(((MutableBoard)
                            obj).get(sqNum(i, j)).getSide())) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (int i = 1; i <= size(); i++) {
            for (int j = 1; j <= size(); j++) {
                int sqNum = sqNum(i, j);
                int toAdd = 0;
                Side side = get(sqNum).getSide();
                switch (side) {
                case RED:
                    toAdd = 1;
                    break;
                case BLUE:
                    toAdd = 2;
                    break;
                default:
                    break;
                }
                hashCode += (get(sqNum).getSpots() + toAdd) * sqNum;
            }
        }
        return hashCode;
    }

}
