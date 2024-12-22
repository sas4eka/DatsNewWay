package logic;

import datamodel.json.Enemy;
import datamodel.json.Food;
import datamodel.json.GameState;
import datamodel.json.Point3D;
import datamodel.json.Snake;
import datamodel.json.SnakeAction;

import java.util.Comparator;
import java.util.PriorityQueue;

public class DijkstraOneSnakeSolver {
    static final boolean DEBUG = false;
    private static final int SNAKE_DIST_LIMIT = 50;
    private static final double DIST_POW = 1.3;
    private static final double CLOSER_TO_ENEMY_PENALTY = 10.0;

    static final int INF = 1_000_000_000;
    static final double DINF = 1e18;

    static final int[] dx = {-1, 1, 0, 0, 0, 0};
    static final int[] dy = {0, 0, -1, 1, 0, 0};
    static final int[] dz = {0, 0, 0, 0, -1, 1};

    int[] dist = new int[0];
    double[] score = new double[0];
    int[] used = new int[0];
    int[] snakeDist = new int[0];

    DijkstraOneSnakeResult solve(GameState gameState, Snake snake) {
        if (!"alive".equals(snake.getStatus())) {
            return new DijkstraOneSnakeResult(null, "DEAD for " + snake.getReviveRemainMs() + " ms");
        }
        if (snake.getGeometry().isEmpty()) {
            return new DijkstraOneSnakeResult(null, "EMPTY SNAKE");
        }
        long start = System.currentTimeMillis();
        int nx = gameState.getMapSize().getX();
        int ny = gameState.getMapSize().getY();
        int nz = gameState.getMapSize().getZ();

        int nxyz = nx * ny * nz;

        if (DEBUG) {
            System.out.println("Cardinality: " + nxyz);
        }

        int xm = ny * nz;
        int ym = nz;

        if (dist.length != nxyz) {
            dist = new int[nxyz];
            score = new double[nxyz];
            used = new int[nxyz];
            snakeDist = new int[nxyz];
        }

        Point3D myHead = snake.getGeometry().getFirst();
        for (int x = 0; x < nx; x++) {
            for (int y = 0; y < ny; y++) {
                for (int z = 0; z < nz; z++) {
                    int xyz = x * xm + y * ym + z;
                    dist[xyz] = INF;
                    score[xyz] = -DINF;
                    used[xyz] = 0;
                    snakeDist[xyz] = INF;
                }
            }
        }
        {
            int hx = myHead.getX();
            int hy = myHead.getY();
            int hz = myHead.getZ();
            for (int x = 0; x < nx; x++) {
                int xd = Math.abs(hx - x);
                for (int y = 0; y < ny; y++) {
                    int xyd = xd + Math.abs(hy - y);
                    for (int z = 0; z < nz; z++) {
                        int cd = Math.abs(hz - z) + xyd;
                        int xyz = x * xm + y * ym + z;
                        if (cd < snakeDist[xyz]) {
                            snakeDist[xyz] = cd;
                        }
                    }
                }
            }
        }

        for (Point3D point : gameState.getFences()) {
            used[point.getX() * xm + point.getY() * ym + point.getZ()] = 2;
        }
        for (Enemy enemy : gameState.getEnemies()) {
            for (Point3D point : enemy.getGeometry()) {
                used[point.getX() * xm + point.getY() * ym + point.getZ()] = 2;
            }
            if (!enemy.getGeometry().isEmpty()) {
                Point3D head = enemy.getGeometry().getFirst();
                int cx = head.getX();
                int cy = head.getY();
                int cz = head.getZ();
                for (int dir = 0; dir < 6; dir++) {
                    int fx = cx + dx[dir];
                    int fy = cy + dy[dir];
                    int fz = cz + dz[dir];
                    if (fx >= 0 && fx < nx && fy >= 0 && fy < ny && fz >= 0 && fz < nz) {
                        int fxyz = fx * xm + fy * ym + fz;
                        used[fxyz] = 2;
                    }
                }
            }
        }
        for (Snake body : gameState.getSnakes()) {
            for (Point3D point : body.getGeometry()) {
                used[point.getX() * xm + point.getY() * ym + point.getZ()] = 2;
            }
        }

        PriorityQueue<State> q = new PriorityQueue<>(Comparator.reverseOrder());

        for (Food food : gameState.getFood()) {
            Point3D point = food.getCoordinates();
            int xyz = point.getX() * xm + point.getY() * ym + point.getZ();
            if (used[xyz] == 0) {
                double points = food.getPoints();
                int myDist = myHead.getDist(point);
                int enemyDist = INF;
                for (Enemy enemy : gameState.getEnemies()) {
                    if (enemy.getGeometry().size() > 2) {
                        Point3D head = enemy.getGeometry().getFirst();
                        enemyDist = Math.min(head.getDist(point), enemyDist);
                    }
                }
                int myOtherDist = INF;
                for (Snake myOther : gameState.getSnakes()) {
                    if (myOther.getId().equals(snake.getId())) {
                        continue;
                    }
                    if ("dead".equals(myOther.getStatus())) {
                        continue;
                    }
                    Point3D myOtherHead = myOther.getGeometry().getFirst();
                    myOtherDist = Math.min(myOtherHead.getDist(point), myOtherDist);
                }
                if (enemyDist <= myDist || myOtherDist <= myDist) {
                    points /= CLOSER_TO_ENEMY_PENALTY;
                }
                if (points > 0) {
                    q.add(new State(xyz, points));
                    score[xyz] = points;
                    dist[xyz] = 0;
                }
            }
        }

        if (DEBUG) {
            System.out.println("Init time: " + (System.currentTimeMillis() - start) + " ms");
        }

        int cnt = 0;
        while (!q.isEmpty()) {
            cnt++;
            State state = q.poll();
            int cxyz = state.getXyz();
            double cs = score[cxyz];
            if (state.getScore() != cs) {
                continue;
            }
            int cx = cxyz / xm;
            int cy = cxyz / ym % ny;
            int cz = cxyz % nz;
            int cd = dist[cxyz];
            if (snakeDist[cxyz] > SNAKE_DIST_LIMIT) {
                continue;
            }
            int nd = cd + 1;
            double scoreScale = Math.pow(cd + 1, DIST_POW) / Math.pow(nd + 1, DIST_POW);
            for (int dir = 0; dir < 6; dir++) {
                int fx = cx + dx[dir];
                int fy = cy + dy[dir];
                int fz = cz + dz[dir];
                if (fx >= 0 && fx < nx && fy >= 0 && fy < ny && fz >= 0 && fz < nz) {
                    int fxyz = fx * xm + fy * ym + fz;
                    if (used[fxyz] == 0) {
                        double ns = cs * scoreScale;
                        if (ns > score[fxyz]) {
                            score[fxyz] = ns;
                            dist[fxyz] = nd;
                            q.add(new State(fxyz, ns));
                        }
                    }
                }
            }
        }
        if (DEBUG) {
            System.out.println("CNT = " + cnt);
            System.out.println("Post Dijkstra time: " + (System.currentTimeMillis() - start) + " ms");
        }

        {
            Point3D center = new Point3D(nx / 2, ny / 2, nz / 2);
            double bestScore = -2 * DINF;
            int bestDist = INF + 17;
            Point3D bestDir = new Point3D(0, 0, 0);
            int cx = myHead.getX();
            int cy = myHead.getY();
            int cz = myHead.getZ();
            for (int dir = 0; dir < 6; dir++) {
                int fx = cx + dx[dir];
                int fy = cy + dy[dir];
                int fz = cz + dz[dir];
                if (fx >= 0 && fx < nx && fy >= 0 && fy < ny && fz >= 0 && fz < nz) {
                    int fxyz = fx * xm + fy * ym + fz;
                    if (used[fxyz] == 0) {
                        double cand = score[fxyz];
                        if (cand < -DINF / 2) {
                            cand *= (center.getDist(new Point3D(fx, fy, fz)) + 1) * 0.0000001;
                        }
                        if (cand > bestScore) {
                            bestScore = cand;
                            bestDist = dist[fxyz] + 1;
                            bestDir = new Point3D(dx[dir], dy[dir], dz[dir]);
                        }
                    }
                }
            }
            double potential = bestScore * Math.pow(bestDist, DIST_POW);
            String comment = "Best score: " + String.format("%.3f (%.1f)", bestScore, potential) + " with dist " + bestDist + " from " + myHead + ". Going " + bestDir;
            SnakeAction snakeAction = new SnakeAction(snake.getId(), bestDir);
            return new DijkstraOneSnakeResult(snakeAction, comment);
        }
    }

    static class State implements Comparable<State> {
        int xyz;
        double score;

        public State(int xyz, double score) {
            this.xyz = xyz;
            this.score = score;
        }

        public int getXyz() {
            return xyz;
        }

        public double getScore() {
            return score;
        }

        @Override
        public int compareTo(State o) {
            if (score != o.score) {
                return Double.compare(score, o.score);
            }
            return Integer.compare(xyz, o.xyz);
        }
    }

}
