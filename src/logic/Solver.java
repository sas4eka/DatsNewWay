package logic;

import datamodel.Solution;
import datamodel.json.GameState;

@FunctionalInterface
public interface Solver {
    Solution solve(GameState gameState);
}
