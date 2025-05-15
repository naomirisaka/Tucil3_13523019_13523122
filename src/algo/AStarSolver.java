package algo;

import model.Board;
import util.Heuristic;

import java.util.*;

public class AStarSolver implements Solver {
    private int visitedCount = 0;
    private long execTime = 0;

    @Override
    public List<Board> solve(Board start) {
        long startTime = System.currentTimeMillis();

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Set<Board> visited = new HashSet<>();
        open.add(new Node(start, 0, Heuristic.blockingCars(start)));

        while (!open.isEmpty()) {
            Node curr = open.poll();
            visitedCount++;

            if (curr.board.isGoal()) {
                execTime = System.currentTimeMillis() - startTime;
                return reconstructPath(curr.board);
            }

            visited.add(curr.board);
            for (Board neighbor : curr.board.getNeighbors()) {
                if (!visited.contains(neighbor)) {
                    int g = curr.g + 1;
                    int h = Heuristic.blockingCars(neighbor);
                    open.add(new Node(neighbor, g, g + h));
                    neighbor.parent = curr.board;
                }
            }
        }

        execTime = System.currentTimeMillis() - startTime;
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
        return visitedCount;
    }

    @Override
    public long getExecutionTime() {
        return execTime;
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
