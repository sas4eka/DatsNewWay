import analysis.Analyser;
import com.fasterxml.jackson.databind.ObjectMapper;
import datamodel.json.GameState;
import datamodel.json.PlayerAction;
import logic.Basic;
import logic.Greedy;
import logic.ParallelDijkstraSolver;
import logic.Solver;
import logic.Trivial;
import util.RequestManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    static final int LOGS_LIMIT = 100;
    private static final int EXTRA_SLEEP_MS = 228;

    static boolean TRUE = true;

    static ObjectMapper MAPPER = new ObjectMapper();
    static RequestManager MANAGER;
    static List<String> logs = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {
        Runtime.getRuntime().addShutdownHook(new Thread(Main::flushLogs));

        Set<String> params = new HashSet<>(Arrays.stream(args).map(String::toLowerCase).toList());
        params.addAll(List.of("replay:remote_log.txt", "status", "play"));

        Solver solver = detectSolver(params);
        String filename = extractFileName(params);
        MANAGER = new RequestManager(params.contains("prod"));

        if (!filename.isEmpty()) {
            runAnalysis(filename, solver);
        } else if (params.contains("status")) {
            runStatusLoop();
        } else if (params.contains("play")) {
            runGameLoop(solver);
        } else {
            System.out.println("Nothing to do.\nPlease provide some args, e.g. 'status' 'play' 'prod' 'greedy' 'replay:1.txt'");
        }
        ParallelDijkstraSolver.shutdown();
    }

    private static Solver detectSolver(Set<String> params) {
        if (params.contains("trivial")) {
            return Trivial::solve;
        } else if (params.contains("basic")) {
            return Basic::solve;
        } else if (params.contains("greedy")) {
            return Greedy::solve;
        }
        return ParallelDijkstraSolver::solve;
    }

    private static void runGameLoop(Solver solver) throws InterruptedException {
        int prevFirstRequestTurn = -1;
        while (TRUE) {
            try {
                long start = System.currentTimeMillis();
                String firstResponse = MANAGER.sendRequest("{}");
                long firstResponseTime = System.currentTimeMillis();
                long firstRequestLatency = firstResponseTime - start;

                writeLogs(firstResponse);
                GameState gameState = MAPPER.readValue(firstResponse, GameState.class);
                if (gameState.getError() != null || (gameState.getErrors() != null && !gameState.getErrors().isEmpty())) {
                    System.out.println("Error with response: " + firstResponse);
                    Thread.sleep(1000);
                    continue;
                }
                int firstRequestTurn = gameState.getTurn();
                PlayerAction action = solver.solve(gameState).getPlayerAction();
                long calculationTime = System.currentTimeMillis();
                long calculationDelay = calculationTime - firstResponseTime;

                long sleepTime = Math.max(0, gameState.getTickRemainMs() + EXTRA_SLEEP_MS - calculationDelay);
                int secondRequestTurn = -1;
                if (needSecondRequest(gameState, action)) {
                    String secondRequest = MAPPER.writeValueAsString(action);
                    String secondResponse = MANAGER.sendRequest(secondRequest);
                    gameState = MAPPER.readValue(secondResponse, GameState.class);
                    secondRequestTurn = gameState.getTurn();
                    sleepTime = gameState.getTickRemainMs() + EXTRA_SLEEP_MS;
                }
                System.out.println("First turn " + firstRequestTurn + (secondRequestTurn == firstRequestTurn ? "" : ", second turn " + secondRequestTurn));
                if (prevFirstRequestTurn != -1 && prevFirstRequestTurn + 1 != firstRequestTurn) {
                    System.out.println("SKIPPED TURNS: " + (firstRequestTurn - prevFirstRequestTurn - 1));
                }
                prevFirstRequestTurn = firstRequestTurn;
                long secondResponseTime = System.currentTimeMillis();
                long secondRequestLatency = secondResponseTime - calculationTime;
                long elapsedTime = secondResponseTime - start;

                System.out.printf("Time: %d ms. Calc: %d ms. R1 latency: %d ms, R2 latency: %d ms. Sleep: %d ms.",
                        elapsedTime, calculationDelay, firstRequestLatency, secondRequestLatency, sleepTime);
                System.out.println();
                System.out.println();

                if (sleepTime != 0) {
                    Thread.sleep(sleepTime);
                }
            } catch (Exception e) {
                System.out.println("FAILED " + e);
                if (!e.toString().contains("GOAWAY")) {
                    Thread.sleep(1000);
                }
            }
        }
    }

    private static void runAnalysis(String filename, Solver solver) throws IOException {
        List<GameState> gameStates = readGameStates(filename);
        Analyser analyser = new Analyser(solver);
        analyser.analyseStates(gameStates);
    }

    private static boolean needSecondRequest(GameState gameState, PlayerAction action) {
        //TODO:: skip second request if the moves are the same
        return true;
    }

    private static void runStatusLoop() throws IOException, URISyntaxException, InterruptedException {
        while (TRUE) {
            long start = System.currentTimeMillis();
            MANAGER.printStatus();
            System.out.println("Time: " + (System.currentTimeMillis() - start) + " ms");
            Thread.sleep(1000);
        }
    }

    private static String extractFileName(Set<String> params) {
        for (String s : params) {
            if (s.startsWith("replay:")) {
                return s.substring(7);
            }
        }
        return "";
    }

    static void writeLogs(String response) {
        // System.out.println(response);
        saveState(response);
        logs.add(response);
        if (logs.size() == LOGS_LIMIT) {
            flushLogs();
        }
    }

    private static void saveState(String response) {
        try {
            PrintWriter writer = new PrintWriter("state.txt");
            writer.println(response);
            writer.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void flushLogs() {
        if (logs.isEmpty()) return;
        long tm = System.currentTimeMillis();
        try {
            PrintWriter writer = new PrintWriter("logs/log" + tm + ".txt");
            for (String log : logs) {
                writer.println(log);
            }
            writer.close();
            logs.clear();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<GameState> readGameStates(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        List<GameState> gameStates = new ArrayList<>();
        while (reader.ready()) {
            String line = reader.readLine();
            if (!line.isEmpty()) {
                gameStates.add(MAPPER.readValue(line, GameState.class));
            }
        }
        reader.close();
        return gameStates;
    }
}
