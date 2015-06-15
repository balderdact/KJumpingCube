
package jump61;

import static jump61.Side.*;

import org.junit.Test;
import static org.junit.Assert.*;

/** Unit tests of Boards.
 *  @author Randy Shi
 */
public class BoardTest {

    private static final String NL = System.getProperty("line.separator");

    @Test
    public void testClear() {
        Board A = new MutableBoard(2);
        Board B = new MutableBoard(2);
        assertTrue(A.equals(B));
        A.set(1, 1, 2, BLUE);
        A.set(1, 2, 2, BLUE);
        A.set(2, 1, 2, BLUE);
        A.set(2, 2, 2, BLUE);
        assertFalse(A.equals(B));
        A.clear(A.size());
        assertTrue(A.equals(B));
    }

    @Test
    public void testIsLegal() {
        Board A = new MutableBoard(4);
        A.set(2, 2, 2, RED);
        assertEquals(false, A.isLegal(BLUE, 2, 2));
        assertEquals(true, A.isLegal(RED, 2, 2));
        assertEquals(true, A.isLegal(RED, 1, 2));
        assertEquals(true, A.isLegal(BLUE, 1, 2));
        assertEquals(false, A.isLegal(BLUE, 0, 4));
        assertEquals(false, A.isLegal(RED, 4, 0));
        assertEquals(false, A.isLegal(BLUE, -1, 0));
        assertEquals(false, A.isLegal(RED, 4, -7));
    }

    @Test
    public void testGetWinner() {
        Board A = new MutableBoard(2);
        assertEquals(null, A.getWinner());
        A.set(1, 1, 1, BLUE);
        assertEquals(null, A.getWinner());
        A.set(1, 2, 1, BLUE);
        A.set(2, 1, 1, BLUE);
        assertNull(A.getWinner());
        A.set(2, 2, 1, BLUE);
        assertEquals(BLUE, A.getWinner());
    }

    @Test
    public void testEquals() {
        Board A = new MutableBoard(10);
        Board B = new MutableBoard(3);
        Board C = new MutableBoard(10);
        assertEquals(true, A.equals(C));
        assertEquals(false, B.equals(C));
        A.set(2, 2, 2, RED);
        assertEquals(false, A.equals(C));
        C.set(2, 2, 2, RED);
        assertEquals(true, A.equals(C));
    }

    @Test
    public void testHashcode() {
        Board A = new MutableBoard(4);
        A.set(2, 2, 2, RED);
        Board B = new MutableBoard(4);
        B.set(2, 2, 2, RED);
        assertEquals(A.hashCode(), B.hashCode());
        B.set(2, 3, 2, RED);
        assertFalse(A.hashCode() == B.hashCode());
    }

    @Test
    public void testNumOfSidesAndPieces() {
        Board A = new MutableBoard(4);
        assertEquals(0, A.numOfSide(RED));
        assertEquals(0, A.numOfSide(BLUE));
        assertEquals(16, A.numOfSide(WHITE));
        assertEquals(16, A.numPieces());
        A.set(2, 2, 2, RED);
        assertEquals(1, A.numOfSide(RED));
        assertEquals(0, A.numOfSide(BLUE));
        assertEquals(15, A.numOfSide(WHITE));
        assertEquals(17, A.numPieces());
        A.set(2, 1, 2, RED);
        assertEquals(2, A.numOfSide(RED));
        assertEquals(0, A.numOfSide(BLUE));
        assertEquals(14, A.numOfSide(WHITE));
        assertEquals(18, A.numPieces());
        A.set(2, 1, 2, BLUE);
        assertEquals(1, A.numOfSide(RED));
        assertEquals(1, A.numOfSide(BLUE));
        assertEquals(14, A.numOfSide(WHITE));
        assertEquals(18, A.numPieces());
    }

    @Test
    public void testCopy() {
        Board A = new MutableBoard(2);
        Board B = new MutableBoard(2);
        A.set(1, 1, 2, BLUE);
        A.set(1, 2, 2, BLUE);
        A.set(2, 1, 2, BLUE);
        A.set(2, 2, 2, BLUE);
        B.copy(A);
        assertEquals(true, A.equals(B));
        assertEquals(8, B.numPieces());
    }

    @Test
    public void testSize() {
        Board B = new MutableBoard(5);
        assertEquals("bad length", 5, B.size());
        ConstantBoard C = new ConstantBoard(B);
        assertEquals("bad length", 5, C.size());
        Board D = new MutableBoard(C);
        assertEquals("bad length", 5, C.size());
    }

    @Test
    public void testGet() {
        Board A = new MutableBoard(4);
        A.set(2, 2, 2, RED);
        assertEquals(2, A.get(A.sqNum(2, 2)).getSpots());
        assertEquals(RED, A.get(A.sqNum(2, 2)).getSide());
    }

    @Test
    public void testSet() {
        Board B = new MutableBoard(5);
        B.set(2, 2, 1, RED);
        assertEquals("wrong number of spots", 1, B.get(2, 2).getSpots());
        assertEquals("wrong color", RED, B.get(2, 2).getSide());
        assertEquals("wrong count", 1, B.numOfSide(RED));
        assertEquals("wrong count", 0, B.numOfSide(BLUE));
        assertEquals("wrong count", 24, B.numOfSide(WHITE));
    }

    @Test
    public void testMove() {
        Board B = new MutableBoard(6);
        checkBoard("#0", B);
        B.addSpot(RED, 1, 1);
        checkBoard("#1", B, 1, 1, 2, RED);
        B.addSpot(BLUE, 2, 1);
        checkBoard("#2", B, 1, 1, 2, RED, 2, 1, 2, BLUE);
        B.addSpot(RED, 1, 1);
        checkBoard("#3", B, 1, 1, 1, RED, 2, 1, 3, RED, 1, 2, 2, RED);
    }

    /** Checks that B conforms to the description given by CONTENTS.
     *  CONTENTS should be a sequence of groups of 4 items:
     *  r, c, n, s, where r and c are row and column number of a square of B,
     *  n is the number of spots that are supposed to be there and s is the
     *  color (RED or BLUE) of the square.  All squares not listed must
     *  be WHITE with one spot.  Raises an exception signaling a unit-test
     *  failure if B does not conform. */
    private void checkBoard(String msg, Board B, Object... contents) {
        for (int k = 0; k < contents.length; k += 4) {
            String M = String.format("%s at %d %d", msg, contents[k],
                                     contents[k + 1]);
            assertEquals(M, (int) contents[k + 2],
                         B.get((int) contents[k],
                               (int) contents[k + 1]).getSpots());
            assertEquals(M, contents[k + 3],
                         B.get((int) contents[k],
                               (int) contents[k + 1]).getSide());
        }
        int c;
        c = 0;
        for (int i = B.size() * B.size() - 1; i >= 0; i -= 1) {
            assertTrue("bad white square #" + i,
                       (B.get(i).getSide() != WHITE)
                       || (B.get(i).getSpots() == 1));
            if (B.get(i).getSide() != WHITE) {
                c += 1;
            }
        }
        assertEquals("extra squares filled", contents.length / 4, c);
    }

}
