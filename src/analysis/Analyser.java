package analysis;

import datamodel.Solution;
import datamodel.json.GameState;
import datamodel.json.PlayerAction;
import logic.Solver;

import java.util.List;

public class Analyser {
    private final Solver solver;

    public Analyser(Solver solver) {
        this.solver = solver;
    }

    public void analyseStates(List<GameState> gameStates) {
        int size = gameStates.size();
        int lastTurn = -1;
        int skips = 0;
        for (int i = 0; i < size; i++) {
            GameState gameState = gameStates.get(i);
            if (gameState.getError() != null || (gameState.getErrors() != null && !gameState.getErrors().isEmpty())) {
                System.out.println("Skipping log with error");
                continue;
            }
            int turn = gameState.getTurn();
            System.out.println("==================================================");
            System.out.println("Analysing game state " + (i + 1) + " of " + size + ", turn " + turn + ".");
            if (lastTurn != -1 && lastTurn + 1 != turn) {
                System.out.println("SKIPPED TURNS: " + (turn - lastTurn - 1));
                skips++;
            }
            lastTurn = turn;
            long start = System.currentTimeMillis();
            Solution solution = solver.solve(gameState);
            long calculationTime = System.currentTimeMillis() - start;
            PlayerAction action = solution.getPlayerAction();
            System.out.println("Actions taken: " + action.getSnakes().size());
            System.out.println("Calc: " + calculationTime + " ms");
        }
        System.out.println();
        System.out.println("Found " + skips + " skips");
    }
}
