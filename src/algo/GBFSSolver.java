package algo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import model.Board;
import util.Heuristic;

public class GBFSSolver implements Solver {
    private int visitedNodes = 0;
    private long executionTime = 0;
    private final String heuristicName;

    public GBFSSolver(String heuristicName) {
        this.heuristicName = heuristicName;
    }

    private int heuristic(Board board) {
        return Heuristic.evaluate(board, heuristicName);
    }

    @Override
    public List<Board> solve(Board start) {
        long startTime = System.currentTimeMillis();

        PriorityQueue<Board> pq = new PriorityQueue<>(Comparator.comparingInt(this::heuristic));
        Set<Board> visited = new HashSet<>();

        pq.add(start);

        while (!pq.isEmpty()) {
            Board current = pq.poll();
            visitedNodes++;

            if (current.isGoal()) {
                executionTime = System.currentTimeMillis() - startTime;
                return reconstructPath(current);
            }

            if (visited.contains(current)) continue;
            visited.add(current);

            for (Board neighbor : current.getNeighbors()) {
                neighbor.parent = current;
                pq.add(neighbor);
            }
        }

        executionTime = System.currentTimeMillis() - startTime;
        return new ArrayList<>();
    }

    @Override
    public List<Board> reconstructPath(Board goal) {
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
