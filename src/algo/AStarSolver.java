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

        if (start.isGoal()) {
            executionTime = System.currentTimeMillis() - startTime;
            visitedNodeCount = 1;
            return Collections.singletonList(start);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.f()));
        Set<Board> visited = new HashSet<>();

        pq.add(new Node(start, 0, Heuristic.evaluate(start, heuristicName)));

        System.out.println("[A*] Starting A* with heuristic: " + heuristicName);
        int iter = 0;

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (visited.contains(current.board)) continue;
            visited.add(current.board);
            visitedNodeCount++;

            if (++iter % 100 == 0 || iter <= 5) {
                System.out.printf("[A*] Iteration %d, g=%d, h=%d, f=%d, frontier=%d%n",
                    iter, current.cost, current.heuristic, current.f(), pq.size());
            }

            if (current.board.isGoal()) {
                executionTime = System.currentTimeMillis() - startTime;
                System.out.printf("[A*] Goal reached after %d nodes, time: %d ms%n", visitedNodeCount, executionTime);
                return reconstructPath(current.board);
            }

            for (Board neighbor : current.board.getNeighbors()) {
                if (!visited.contains(neighbor)) {
                    neighbor.parent = current.board;
                    int g = current.cost + 1;
                    int h = Heuristic.evaluate(neighbor, heuristicName);
                    pq.add(new Node(neighbor, g, h));

                    if (iter <= 5) {
                        System.out.printf("[A*]  Enqueue move: %-20s | g=%d, h=%d, f=%d%n",
                            neighbor.move, g, h, g + h);
                    }
                }
            }
        }

        executionTime = System.currentTimeMillis() - startTime;
        System.out.printf("[A*] No solution found after %d nodes, time: %d ms%n", visitedNodeCount, executionTime);
        return new ArrayList<>();
    }

    private List<Board> reconstructPath(Board goal) {
        List<Board> path = new ArrayList<>();
        for (Board b = goal; b != null; b = b.parent) {
            path.add(0, b);
        }
        return path;
    }

    private static class Node {
        Board board;
        int cost;       // g(n)
        int heuristic;  // h(n)

        Node(Board board, int cost, int heuristic) {
            this.board = board;
            this.cost = cost;
            this.heuristic = heuristic;
        }

        int f() {
            return cost + heuristic;
        }
    }

    @Override
    public int getVisitedNodeCount() {
        return visitedNodeCount;
    }

    @Override
    public long getExecutionTime() {
        return executionTime;
    }
}
