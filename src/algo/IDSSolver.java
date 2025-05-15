package algo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Board;

public class IDSSolver implements Solver {
    private int visitedNodes = 0;
    private long executionTime = 0;

    @Override
    public List<Board> solve(Board start) {
        long startTime = System.currentTimeMillis();

        for (int depth = 0; depth < 100; depth++) {
            Set<Board> visited = new HashSet<>();
            List<Board> result = dfs(start, depth, visited);

            if (!result.isEmpty()) {
                executionTime = System.currentTimeMillis() - startTime;
                return result;
            }
        }

        executionTime = System.currentTimeMillis() - startTime;
        return new ArrayList<>();
    }

    private List<Board> dfs(Board current, int depth, Set<Board> visited) {
        visitedNodes++;
        if (depth < 0) return new ArrayList<>();
        if (current.isGoal()) return reconstructPath(current);

        visited.add(current);

        for (Board neighbor : current.getNeighbors()) {
            if (!visited.contains(neighbor)) {
                neighbor.parent = current;
                List<Board> result = dfs(neighbor, depth - 1, visited);
                if (!result.isEmpty()) return result;
            }
        }

        return new ArrayList<>();
    }

    private List<Board> reconstructPath(Board goal) {
        List<Board> path = new ArrayList<>();
        for (Board b = goal; b != null; b = b.parent) {
            path.add(0, b);
        }
        return path;
    }

    @Override
    public int getVisitedNodeCount() {
        return visitedNodes;
    }

    @Override
    public long getExecutionTime() {
        return executionTime;
    }
}
