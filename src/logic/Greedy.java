package logic;

import datamodel.AnalysisData;
import datamodel.Solution;
import datamodel.json.Enemy;
import datamodel.json.Food;
import datamodel.json.GameState;
import datamodel.json.PlayerAction;
import datamodel.json.Point3D;
import datamodel.json.Snake;
import datamodel.json.SnakeAction;

import java.util.ArrayList;
import java.util.List;

public class Greedy {
    static final boolean DEBUG = false;
    private static final int SNAKE_DIST_LIMIT = 50;

    static final int INF = 1_000_000_000;

    static final int[] dx = {-1, 1, 0, 0, 0, 0};
    static final int[] dy = {0, 0, -1, 1, 0, 0};
    static final int[] dz = {0, 0, 0, 0, -1, 1};

    static int[] dist = new int[0];
    static int[] used = new int[0];
    static int[] q = new int[0];
    static int[] snakeDist = new int[0];

    public static Solution solve(GameState gameState) {
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
            used = new int[nxyz];
            q = new int[nxyz];
            snakeDist = new int[nxyz];
        }
        List<Point3D> heads = new ArrayList<>();
        for (Snake snake : gameState.getSnakes()) {
            if ("alive".equals(snake.getStatus())) {
                if (!snake.getGeometry().isEmpty()) {
                    Point3D head = snake.getGeometry().getFirst();
                    heads.add(head);
                }
            }
        }
        for (int x = 0; x < nx; x++) {
            for (int y = 0; y < ny; y++) {
                for (int z = 0; z < nz; z++) {
                    int xyz = x * xm + y * ym + z;
                    dist[xyz] = INF;
                    used[xyz] = 0;
                    snakeDist[xyz] = INF;
                }
            }
        }
        for (Point3D head : heads) {
            int hx = head.getX();
            int hy = head.getY();
            int hz = head.getZ();
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
        int qsz = 0;
        int qj = 0;

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
        for (Snake snake : gameState.getSnakes()) {
            for (Point3D point : snake.getGeometry()) {
                used[point.getX() * xm + point.getY() * ym + point.getZ()] = 2;
            }
        }

        for (Food food : gameState.getFood()) {
            Point3D point = food.getCoordinates();
            int xyz = point.getX() * xm + point.getY() * ym + point.getZ();
            if (used[xyz] == 0) {
                q[qsz] = xyz;
                qsz++;
                dist[xyz] = 0;
                used[xyz] = 1;
            }
        }

        if (DEBUG) {
            System.out.println("Init time: " + (System.currentTimeMillis() - start) + " ms");
        }

        while (qj < qsz) {
            int cxyz = q[qj];
            qj++;
            int cx = cxyz / xm;
            int cy = cxyz / ym % ny;
            int cz = cxyz % nz;
            int cd = dist[cxyz];
            if (snakeDist[cxyz] > SNAKE_DIST_LIMIT) {
                continue;
            }
            for (int dir = 0; dir < 6; dir++) {
                int fx = cx + dx[dir];
                int fy = cy + dy[dir];
                int fz = cz + dz[dir];
                if (fx >= 0 && fx < nx && fy >= 0 && fy < ny && fz >= 0 && fz < nz) {
                    int fxyz = fx * xm + fy * ym + fz;
                    if (used[fxyz] == 0) {
                        used[fxyz] = 1;
                        dist[fxyz] = cd + 1;
                        q[qsz] = fxyz;
                        qsz++;
                    }
                }
            }
        }
        if (DEBUG) {
            System.out.println("CNT = " + qj);
            System.out.println("Post BFS time: " + (System.currentTimeMillis() - start) + " ms");
        }

        List<SnakeAction> actions = new ArrayList<>();
        for (Snake snake : gameState.getSnakes()) {
            if ("dead".equals(snake.getStatus())) {
                System.out.println("DEAD for " + snake.getReviveRemainMs() + " ms");
                continue;
            }
            Point3D head = snake.getGeometry().getFirst();
            int bestDist = INF + 17;
            Point3D bestDir = new Point3D(0, 0, 0);
            int cx = head.getX();
            int cy = head.getY();
            int cz = head.getZ();
            for (int dir = 0; dir < 6; dir++) {
                int fx = cx + dx[dir];
                int fy = cy + dy[dir];
                int fz = cz + dz[dir];
                if (fx >= 0 && fx < nx && fy >= 0 && fy < ny && fz >= 0 && fz < nz) {
                    int fxyz = fx * xm + fy * ym + fz;
                    if (used[fxyz] == 1) {
                        int cand = dist[fxyz] + 1;
                        if (cand < bestDist) {
                            bestDist = cand;
                            bestDir = new Point3D(dx[dir], dy[dir], dz[dir]);
                        }
                    }
                }
            }
            System.out.println("Best dist: " + bestDist + " from " + head + ". Going " + bestDir);
            actions.add(new SnakeAction(snake.getId(), bestDir));
        }
        return new Solution(new PlayerAction(actions), new AnalysisData());
    }
}
