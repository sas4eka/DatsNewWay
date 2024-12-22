package logic;

import datamodel.AnalysisData;
import datamodel.Solution;
import datamodel.json.Food;
import datamodel.json.GameState;
import datamodel.json.PlayerAction;
import datamodel.json.Point3D;
import datamodel.json.Snake;
import datamodel.json.SnakeAction;

import java.util.ArrayList;
import java.util.List;

public class Basic {
    public static Solution solve(GameState gameState) {

        List<SnakeAction> actions = new ArrayList<>();
        for (Snake snake : gameState.getSnakes()) {
            if ("dead".equals(snake.getStatus())) {
                System.out.println("DEAD for " + snake.getReviveRemainMs() + " ms");
                continue;
            }
            Point3D head = snake.getGeometry().getFirst();
            Point3D closestTarget = getClosestTarget(head, gameState);
            Point3D direction = getDirection(head, closestTarget);
            actions.add(new SnakeAction(snake.getId(), direction));
        }
        return new Solution(new PlayerAction(actions), new AnalysisData());
    }

    private static Point3D getDirection(Point3D head, Point3D closestTarget) {
        Point3D direction = new Point3D(0, 0, 0);
        if (head.getX() < closestTarget.getX()) {
            direction.setX(1);
        } else if (head.getY() < closestTarget.getY()) {
            direction.setY(1);
        } else if (head.getZ() < closestTarget.getZ()) {
            direction.setZ(1);
        } else if (head.getX() > closestTarget.getX()) {
            direction.setX(-1);
        } else if (head.getY() > closestTarget.getY()) {
            direction.setY(-1);
        } else if (head.getZ() > closestTarget.getZ()) {
            direction.setZ(-1);
        }
        return direction;
    }

    private static Point3D getClosestTarget(Point3D head, GameState gameState) {
        Point3D best = new Point3D(0, 0, 0);
        int bestDist = 1_000_000;
        List<Point3D> cands = new ArrayList<>();
        for (Food food : gameState.getFood()) {
            cands.add(food.getCoordinates());
        }
        cands.addAll(gameState.getSpecialFood().getGolden());
        cands.addAll(gameState.getSpecialFood().getSuspicious());
        for (Point3D cand : cands) {
            int dist = head.getDist(cand);
            if (dist < bestDist) {
                bestDist = dist;
                best = cand;
            }
        }
        Point3D direction = getDirection(head, best);
        System.out.println("Best dist: " + bestDist + " " + best + " from " + head + ". Going " + direction);
        return best;
    }
}
