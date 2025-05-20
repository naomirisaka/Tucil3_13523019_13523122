package algo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import model.Board;

public class UCSSolver implements Solver {
    private int visitedNodeCount = 0;
    private long executionTime = 0;

    @Override
    public List<Board> solve(Board start) {
        long startTime = System.currentTimeMillis();

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));
        Set<Board> visited = new HashSet<>();

        pq.add(new Node(start, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            visitedNodeCount++;

            if (current.board.isGoal()) {
                executionTime = System.currentTimeMillis() - startTime;
                return reconstructPath(current.board);
            }

            if (visited.contains(current.board)) continue;
            visited.add(current.board);

            for (Board neighbor : current.board.getNeighbors()) {
                neighbor.parent = current.board;
                pq.add(new Node(neighbor, current.cost + 1));
            }
        }

        executionTime = System.currentTimeMillis() - startTime;
        return new ArrayList<>();
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
        int cost;

        Node(Board board, int cost) {
            this.board = board;
            this.cost = cost;
        }
    }

    @Override
    public List<Board> reconstructPath(Board goal) {
        List<Board> path = new ArrayList<>();
        for (Board b = goal; b != null; b = b.parent) {
            path.add(0, b);
        }
        return path;
    }
}
