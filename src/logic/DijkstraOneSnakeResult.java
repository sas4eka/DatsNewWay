package logic;

import datamodel.json.SnakeAction;

class DijkstraOneSnakeResult {
    SnakeAction snakeAction;
    String comment;

    public DijkstraOneSnakeResult(SnakeAction snakeAction, String comment) {
        this.snakeAction = snakeAction;
        this.comment = comment;
    }

    public SnakeAction getSnakeAction() {
        return snakeAction;
    }

    public String getComment() {
        return comment;
    }
}
