package algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import model.Board;
import util.Heuristic;

public class AStarSolver implements Solver {
    private int visitedNodeCount = 0;
    private long executionTime = 0;
    private final String heuristicName;

    public AStarSolver(String heuristicName) {
        this.heuristicName = heuristicName;
    }

    @Override
    public List<Board> solve(Board start) {
        long startTime = System.currentTimeMillis();

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Set<Board> visited = new HashSet<>();
        open.add(new Node(start, 0, Heuristic.evaluate(start, heuristicName)));

        while (!open.isEmpty()) {
            Node curr = open.poll();
            visitedNodeCount++;

            if (curr.board.isGoal()) {
                executionTime = System.currentTimeMillis() - startTime;
                return reconstructPath(curr.board);
            }

            visited.add(curr.board);
            for (Board neighbor : curr.board.getNeighbors()) {
                if (!visited.contains(neighbor)) {
                    int g = curr.g + 1;
                    int h = Heuristic.evaluate(neighbor, heuristicName);
                    open.add(new Node(neighbor, g, g + h));
                    neighbor.parent = curr.board;
                }
            }
        }

        executionTime = System.currentTimeMillis() - startTime;
        return new ArrayList<>(); // tidak ada solusi
    }

    private List<Board> reconstructPath(Board goal) {
        List<Board> path = new ArrayList<>();
        for (Board at = goal; at != null; at = at.parent)
            path.add(at);
        Collections.reverse(path);
        return path;
    }

    @Override
    public int getVisitedNodeCount() {
        return visitedNodeCount;
    }

    @Override
    public long getExecutionTime() {
        return executionTime;
    }

    private static class Node {
        Board board;
        int g, f;

        Node(Board b, int g, int f) {
            this.board = b;
            this.g = g;
            this.f = f;
        }
    }
}
