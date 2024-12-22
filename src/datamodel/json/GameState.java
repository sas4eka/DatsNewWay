package datamodel.json;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameState {
    private Point3D mapSize;
    private String name;
    private int points;
    private List<Point3D> fences;
    private List<Snake> snakes;
    private List<Enemy> enemies;
    private List<Food> food;
    private SpecialFood specialFood;
    private int turn;
    private int reviveTimeoutSec;
    private int tickRemainMs;
    private List<String> errors;
    private String error;
    private String errCode;

    public GameState() {
    }

    public GameState(GameState other) {
        this.mapSize = new Point3D(other.mapSize);
        this.name = other.name;
        this.points = other.points;
        this.fences = other.fences.stream().map(Point3D::new).collect(Collectors.toList());
        this.snakes = other.snakes.stream().map(Snake::new).collect(Collectors.toList());
        this.enemies = other.enemies.stream().map(Enemy::new).collect(Collectors.toList());
        this.food = other.food.stream().map(Food::new).collect(Collectors.toList());
        this.specialFood = new SpecialFood(other.specialFood);
        this.turn = other.turn;
        this.reviveTimeoutSec = other.reviveTimeoutSec;
        this.tickRemainMs = other.tickRemainMs;
        this.errors = new ArrayList<>(other.errors);
        this.error = other.error;
        this.errCode = other.errCode;
    }

    public Point3D getMapSize() {
        return mapSize;
    }

    public void setMapSize(Point3D mapSize) {
        this.mapSize = mapSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public List<Point3D> getFences() {
        return fences;
    }

    public void setFences(List<Point3D> fences) {
        this.fences = fences;
    }

    public List<Snake> getSnakes() {
        return snakes;
    }

    public void setSnakes(List<Snake> snakes) {
        this.snakes = snakes;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void setEnemies(List<Enemy> enemies) {
        this.enemies = enemies;
    }

    public List<Food> getFood() {
        return food;
    }

    public void setFood(List<Food> food) {
        this.food = food;
    }

    public SpecialFood getSpecialFood() {
        return specialFood;
    }

    public void setSpecialFood(SpecialFood specialFood) {
        this.specialFood = specialFood;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getReviveTimeoutSec() {
        return reviveTimeoutSec;
    }

    public void setReviveTimeoutSec(int reviveTimeoutSec) {
        this.reviveTimeoutSec = reviveTimeoutSec;
    }

    public int getTickRemainMs() {
        return tickRemainMs;
    }

    public void setTickRemainMs(int tickRemainMs) {
        this.tickRemainMs = tickRemainMs;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }
}