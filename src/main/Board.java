import lib.Queue;
import lib.StdRandom;

import java.util.Arrays;

/*************************************************************************
 * Name:antonio saco
 *
 * Compilation: javac Board.java
 *
 * Dependencies: StdRandom.java
 *
 * Description: an immutable data type Board, to solve the 8-puzzle problem (and
 * its natural generalizations) using the A* search algorithm.
 *
 * https://www.cs.princeton.edu/courses/archive/fall12/cos226/assignments/8puzzle.html
 *
 *************************************************************************/
public class Board {
    private short[][] board;
    private short[] index0;
    private short hamming = -1;
    private short manhattan = -1;
    private int hashCode = -1;
    private String toString;

    // construct a board from an N-by-N array of blocks
    // (where blocks[i][j] = block in row i, column j)
    public Board(int[][] blocks) {
        // initialize board
        board = new short[blocks.length][blocks.length];
        for (short i = 0; i < blocks.length; i++)
            for (short j = 0; j < blocks.length; j++)
                board[i][j] = Integer.valueOf(blocks[i][j]).shortValue();

        // calculate priorities
        // hamming();
        // manhattan();
        StdRandom.setSeed(System.currentTimeMillis());
        hash();
    }

    private Board(short[][] blocks) {
        board = new short[blocks.length][blocks.length];
        for (short i = 0; i < blocks.length; i++)
            board[i] = Arrays.copyOf(blocks[i], blocks[i].length);
        // calculate priorities
        // hamming();
        // manhattan();
        StdRandom.setSeed(System.currentTimeMillis());
        hash();
    }

    // board dimension N
    public int dimension() {
        return board.length;
    }

    // number of blocks out of place
    public int hamming() {
        if (hamming != -1)
            return hamming;
        hamming = 0;
        short i = 1;
        for (short[] block : board)
            for (short b : block)
                if (i++ != b && b != 0) {
                    hamming++;
                }
        return hamming;
    }

    // sum of Manhattan distances between blocks and goal
    public int manhattan() {
        if (manhattan == -1) {
            // initialize goaldBoard for manhattan
            short[][] goalBoard = new short[dimension() * dimension()][2];
            int gb = 0;
            for (short i = 0; i < dimension(); i++)
                for (short j = 0; j < dimension(); j++) {
                    goalBoard[gb][0] = i;
                    goalBoard[gb][1] = j;
                    gb++;
                }

            manhattan = 0;
            short g = 1;
            int b;
            for (short i = 0; i < dimension(); i++)
                for (short j = 0; j < dimension(); j++) {
                    b = board[i][j];
                    if (g++ != b && b != 0) {
                        manhattan += (Math.abs(goalBoard[b - 1][1] - j) + Math
                                .abs(goalBoard[b - 1][0] - i));
                    }
                }
        }
        return manhattan;
    }

    // is this board the goal board?
    public boolean isGoal() {
        return manhattan() == 0;
    }

    // a board obtained by exchanging two adjacent blocks in the same row
    public Board twin() {
        int i = StdRandom.uniform(dimension() - 1);
        int j = StdRandom.uniform(dimension() - 1);
        short[][] twinBoard = copyBoard();
        short x = board[i][j];
        short y = board[i][j + 1];

        // DEFAULT
        if (x != 0 && y != 0) {
            twinBoard[i][j] = y;
            twinBoard[i][j + 1] = x;
        }
        // 2x2
        if (dimension() == 2 || y == 0) {
            if (x == 0 || y == 0) {
                x = board[i + 1][j];
                y = board[i + 1][j + 1];
                twinBoard[i + 1][j + 1] = x;
                twinBoard[i + 1][j] = y;
            }
        } else {
            if (x == 0) {
                if (j + 2 < dimension()) {
                    twinBoard[i][j + 1] = board[i][j + 2];
                    twinBoard[i][j + 2] = y;
                }
            }
        }

        return new Board(twinBoard);
    }

    private void hash() {
        if (hashCode == -1) {
            hashCode = Arrays.hashCode(board);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Board other = (Board) obj;
        if (hashCode != -1 && hashCode == other.hashCode)
            return true;
        if (!Arrays.deepEquals(board, other.board))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    // all neighboring boards
    public Iterable<Board> neighbors() {
        Queue<Board> neighbors = new Queue<>();

        // find 0
        if (index0 == null) {
            index0 = new short[2];
            int b = -1;
            for (short i = 0; i < dimension(); i++) {
                for (short j = 0; j < dimension(); j++) {
                    b = board[i][j];
                    if (b == 0) {
                        index0[0] = i;
                        index0[1] = j;
                        break;
                    }
                }
                if (b == 0)
                    break;
            }
        }

        short[][] boardNeighbor;

        if (index0[0] > 0 && index0[0] <= dimension() - 1) { // up
            boardNeighbor = copyBoard();
            swap(boardNeighbor, index0[0] - 1, index0[1], index0[0], index0[1]);
            neighbors.enqueue(new Board(boardNeighbor));
        }
        if (index0[0] >= 0 && index0[0] < dimension() - 1) { // down
            boardNeighbor = copyBoard();
            swap(boardNeighbor, index0[0] + 1, index0[1], index0[0], index0[1]);
            neighbors.enqueue(new Board(boardNeighbor));
        }
        if (index0[1] > 0 && index0[1] <= dimension() - 1) { // left
            boardNeighbor = copyBoard();
            swap(boardNeighbor, index0[0], index0[1] - 1, index0[0], index0[1]);
            neighbors.enqueue(new Board(boardNeighbor));
        }
        if (index0[1] >= 0 && index0[1] < dimension() - 1) { // right
            boardNeighbor = copyBoard();
            swap(boardNeighbor, index0[0], index0[1] + 1, index0[0], index0[1]);
            neighbors.enqueue(new Board(boardNeighbor));
        }

        return neighbors;
    }

    private short[][] copyBoard() {
        short[][] boardCopy = new short[dimension()][dimension()];

        for (short i = 0; i < dimension(); i++)
            System.arraycopy(board[i], 0, boardCopy[i], 0, dimension());

        // Arrays.copyOf(board[i], board[i].length);
        return boardCopy;
    }

    private void swap(final short[][] boardToSwap, int i, int j, int i0, int j0) {
        short tmp = boardToSwap[i][j];
        boardToSwap[i][j] = 0;
        boardToSwap[i0][j0] = tmp;
    }

    // string representation of the board (in the output format specified below)
    public String toString() {
        if (toString == null) {
            StringBuilder s = new StringBuilder();
            s.append(dimension() + "\n");
            // s.append(N + ":" + manhattan() + "\n");
            for (short i = 0; i < dimension(); i++) {
                for (short j = 0; j < dimension(); j++) {
                    // if (board[i][j] != 0)
                    s.append(String.format("%2d ", board[i][j]));
                    // else
                    // s.append("* ");
                }
                s.append("\n");
            }
            toString = s.toString();
        }
        return toString;
    }

}
