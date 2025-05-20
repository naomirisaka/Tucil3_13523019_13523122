package algo;

import java.util.List;

import model.Board;

public interface Solver {
    // solves the puzzle and returns the solution path
    List<Board> solve(Board initialBoard);

    // returns the number of nodes visited during the search
    int getVisitedNodeCount();

    // returns the time taken to solve the puzzle in ms
    long getExecutionTime();

    // reconstructs the path from the initial board to the goal board
    List<Board> reconstructPath(Board goal);

}
