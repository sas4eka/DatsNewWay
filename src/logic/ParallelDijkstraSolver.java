package logic;

import datamodel.AnalysisData;
import datamodel.Solution;
import datamodel.json.GameState;
import datamodel.json.PlayerAction;
import datamodel.json.Snake;
import datamodel.json.SnakeAction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelDijkstraSolver {
    static ExecutorService SERVICE = Executors.newFixedThreadPool(4);
    static ConcurrentHashMap<String, DijkstraOneSnakeSolver> SOLVERS = new ConcurrentHashMap<>();

    public static Solution solve(GameState gameState) {
        List<Future<DijkstraOneSnakeResult>> futures = new ArrayList<>();
        for (Snake snake : gameState.getSnakes()) {
            String id = snake.getId();
            if (!SOLVERS.contains(id)) {
                SOLVERS.put(id, new DijkstraOneSnakeSolver());
            }
            DijkstraOneSnakeSolver solver = SOLVERS.get(id);
            Future<DijkstraOneSnakeResult> submit = SERVICE.submit(() -> solver.solve(gameState, snake));
            futures.add(submit);
        }
        List<SnakeAction> actions = new ArrayList<>();
        for (var f : futures) {
            try {
                DijkstraOneSnakeResult result = f.get();
                SnakeAction snakeAction = result.getSnakeAction();
                if (snakeAction != null) {
                    actions.add(snakeAction);
                }
                System.out.println(result.getComment());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return new Solution(new PlayerAction(actions), new AnalysisData());
    }

    public static void shutdown() {
        SERVICE.shutdown();
    }
}
