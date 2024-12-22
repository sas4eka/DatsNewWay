package datamodel.json;

import java.util.List;

public class PlayerAction {
    private List<SnakeAction> snakes;

    public PlayerAction(List<SnakeAction> snakes) {
        this.snakes = snakes;
    }

    public List<SnakeAction> getSnakes() {
        return snakes;
    }

    public void setSnakes(List<SnakeAction> snakes) {
        this.snakes = snakes;
    }
}
