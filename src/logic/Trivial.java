package logic;

import datamodel.AnalysisData;
import datamodel.Solution;
import datamodel.json.GameState;
import datamodel.json.PlayerAction;
import datamodel.json.Point3D;
import datamodel.json.Snake;
import datamodel.json.SnakeAction;

import java.util.ArrayList;
import java.util.List;

public class Trivial {
    public static Solution solve(GameState gameState) {
        List<SnakeAction> actions = new ArrayList<>();
        for (Snake snake : gameState.getSnakes()) {
            actions.add(new SnakeAction(snake.getId(), new Point3D(1, 0, 0)));
        }
        return new Solution(new PlayerAction(actions), new AnalysisData());
    }
}
