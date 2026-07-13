package za.co.sww.game.wumpus.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import za.co.sww.game.wumpus.actions.HazardChecker;
import za.co.sww.game.wumpus.actions.PlayerMovement;
import za.co.sww.game.wumpus.actions.ShootAction;
import za.co.sww.game.wumpus.commentary.CommentaryClient;
import za.co.sww.game.wumpus.commentary.CommentaryDispatcher;
import za.co.sww.game.wumpus.commentary.CommentarySnapshot;
import za.co.sww.game.wumpus.commentary.HttpCommentaryClient;
import za.co.sww.game.wumpus.commentary.NoopCommentaryClient;
import za.co.sww.game.wumpus.domain.Cave;
import za.co.sww.game.wumpus.domain.Player;
import za.co.sww.game.wumpus.io.ConsoleInput;
import za.co.sww.game.wumpus.io.ConsoleOutput;
import za.co.sww.game.wumpus.io.Input;
import za.co.sww.game.wumpus.io.Output;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class HuntService {
    private static final int HISTORY_LIMIT = 3;

    Cave[] caves = new Cave[20];
    final CavesService cavesService = new CavesService();
    private final Input input;
    private final Output output;
    private final CommentaryDispatcher commentaryDispatcher;
    private final boolean includeHiddenState;
    private final Deque<String> recentActionSummaries = new ArrayDeque<>();
    private int movesTaken;

    PlayerMovement playerMovement;
    HazardChecker hazardChecker;
    ShootAction shootAction;

    public HuntService() {
        this(new ConsoleInput(), new ConsoleOutput());
    }

    public HuntService(Input input, Output output) {
        this(input, output, createCommentaryDispatcher(output));
    }

    public HuntService(Input input, Output output, CommentaryDispatcher commentaryDispatcher) {
        this.input = input;
        this.output = output;
        this.commentaryDispatcher = commentaryDispatcher;
        this.includeHiddenState = Boolean.parseBoolean(readConfig("WUMPUS_COMMENTARY_INCLUDE_HIDDEN_STATE")
                .orElse("false"));
        this.playerMovement = new PlayerMovement(input);
        this.hazardChecker = new HazardChecker(output);
        this.shootAction = new ShootAction(input, output);
    }

    public void runGame() {
        boolean gameRunning = true;
        do {
            Player player = new Player();
            caves = cavesService.buildCaves();
            player.setCurrentRoom(cavesService.initializeCaves(caves));
            recentActionSummaries.clear();
            movesTaken = 0;

            IO.println("Welcome to Hunt the Wumpus");
            IO.println();
            IO.println("You are hunting the Wumpus through a system of 20 caves, each linked to 3 others.");
            IO.println("The Wumpus is asleep somewhere in these caves. Find it and shoot it before it eats");
            IO.println("you or you blunder into one of the other hazards.");
            IO.println();
            IO.println("Your senses warn you about the 3 caves next to you, but not which one holds what:");
            IO.println("  - You smell something terrible nearby when the Wumpus is in an adjacent cave.");
            IO.println("  - You feel a cold draft from a nearby cave when a bottomless pit is adjacent.");
            IO.println("  - You hear the flapping of giant bats nearby when a bat cave is adjacent.");
            IO.println();
            IO.println("The hazards:");
            IO.println("  - 2 bottomless pits. Stumble into one and you fall to your death.");
            IO.println("  - 2 caves of super bats. They grab you and whisk you away to a random cave.");
            IO.println();
            IO.println("You carry 5 crooked arrows. When you shoot, you steer an arrow through up to 5 caves,");
            IO.println("naming the caves it should fly through. If it reaches the Wumpus you win; if you miss,");
            IO.println("the startled Wumpus may move, and it might end up in your cave and eat you.");
            IO.println("Good luck adventurer!");
            IO.println();
            Scanner scanner = new Scanner(System.in);

            output.println("Press Enter to continue...");
            scanner.nextLine();

            startGameLoop(player);
            gameRunning = input.readln("Play again? (Y/N) ").toUpperCase().equals("Y");
        } while (gameRunning);
    }

    public void startGameLoop(Player player) {
        while (player.getState() == Player.PlayerState.ALIVE) {
            printPlayerCave(player);
            hazardChecker.printHazards(caves, player.getCurrentRoom());
            switch (input.readln("Shoot or move? (S/M) ").toUpperCase()) {
                case "S":
                    runShoot(player);
                    break;
                case "M":
                    runMove(player);
                    break;
                default:
                    output.println("That's not a valid action. Please enter S to shoot or M to move.");
            }
        }
        switch (player.getState()) {
            case WINNER:
                output.println("Congratulations! You have slain the Wumpus and won the game!");
                break;
            case DEAD:
                output.println("Game over. Better luck next time!");
                break;
        }
    }

    private void runMove(Player player) {
        PlayerMovement.MoveResult moveResult = playerMovement.movePlayer(caves, player);
        if (!moveResult.moved()) {
            return;
        }
        movesTaken++;
        HazardChecker.HazardOutcome hazardOutcome = hazardChecker.checkForHazards(caves, player);
        String outcome = mapMoveOutcome(hazardOutcome);
        dispatchCommentary(
                "MOVE",
                "MOVE_TO_ROOM",
                moveResult.targetRoom(),
                List.of(),
                player.getCurrentRoom(),
                outcome,
                player
        );
        rememberActionSummary("MOVE", outcome, moveResult.targetRoom());
    }

    private String mapMoveOutcome(HazardChecker.HazardOutcome hazardOutcome) {
        return switch (hazardOutcome) {
            case SAFE -> "SAFE";
            case BATS_RELOCATED -> "BATS_RELOCATED";
            case PIT_DEATH -> "PIT_DEATH";
            case WUMPUS_BUMPED -> "WUMPUS_BUMPED";
            case WUMPUS_ATE_PLAYER -> "WUMPUS_BUMPED";
        };
    }

    private void runShoot(Player player) {
        ShootAction.ShootResult shootResult = shootAction.shootArrow(caves, player);
        movesTaken++;
        Integer intendedTargetRoom = shootResult.path().isEmpty()
                ? null
                : shootResult.path().get(shootResult.path().size() - 1);
        dispatchCommentary(
                "SHOOT",
                "SHOOT_THROUGH_CAVES",
                intendedTargetRoom,
                List.copyOf(shootResult.path()),
                shootResult.finalArrowRoom(),
                shootResult.outcome(),
                player
        );
        rememberActionSummary("SHOOT", shootResult.outcome(), shootResult.finalArrowRoom());
    }

    private void dispatchCommentary(
            String action,
            String actionIntent,
            Integer intendedTargetRoom,
            List<Integer> nominatedPath,
            Integer targetRoom,
            String outcome,
            Player player
    ) {
        CommentarySnapshot snapshot = new CommentarySnapshot(
                action,
                actionIntent,
                intendedTargetRoom,
                nominatedPath,
                targetRoom,
                outcome,
                player.getCurrentRoom(),
                adjacentRooms(player),
                hazardChecker.listHazardWarnings(caves, player.getCurrentRoom()),
                player.getNumberOfArrows(),
                movesTaken,
                List.copyOf(recentActionSummaries),
                buildHiddenState()
        );
        commentaryDispatcher.dispatch(snapshot);
    }

    private List<Integer> adjacentRooms(Player player) {
        int room = player.getCurrentRoom();
        if (room < 0 || room >= caves.length) {
            return List.of();
        }
        return Arrays.stream(caves[room].getLinkedCaves()).boxed().toList();
    }

    private CommentarySnapshot.DebugHiddenState buildHiddenState() {
        if (!includeHiddenState) {
            return null;
        }
        Integer wumpusRoom = null;
        List<Integer> pits = new ArrayList<>();
        List<Integer> bats = new ArrayList<>();
        for (int i = 0; i < caves.length; i++) {
            if (caves[i].hasWumpus()) {
                wumpusRoom = i;
            }
            if (caves[i].isBottomLessPit()) {
                pits.add(i);
            }
            if (caves[i].hasBats()) {
                bats.add(i);
            }
        }
        return new CommentarySnapshot.DebugHiddenState(wumpusRoom, pits, bats);
    }

    private void rememberActionSummary(String action, String outcome, Integer targetRoom) {
        String summary = action + " -> " + outcome;
        if (targetRoom != null) {
            summary = summary + " @ " + targetRoom;
        }
        recentActionSummaries.addLast(summary);
        while (recentActionSummaries.size() > HISTORY_LIMIT) {
            recentActionSummaries.removeFirst();
        }
    }

    private static CommentaryDispatcher createCommentaryDispatcher(Output output) {
        long timeoutMillis = parseLong(readConfig("WUMPUS_COMMENTARY_TIMEOUT_MS").orElse("1500"), 1500);
        Optional<String> maybeUrl = readConfig("WUMPUS_COMMENTARY_URL");
        if (maybeUrl.isEmpty()) {
            return new CommentaryDispatcher(new NoopCommentaryClient(), output, timeoutMillis);
        }
        try {
            CommentaryClient client = new HttpCommentaryClient(
                    HttpClient.newHttpClient(),
                    new ObjectMapper(),
                    URI.create(maybeUrl.get()),
                    Duration.ofMillis(timeoutMillis)
            );
            return new CommentaryDispatcher(client, output, timeoutMillis);
        } catch (IllegalArgumentException ex) {
            return new CommentaryDispatcher(new NoopCommentaryClient(), output, timeoutMillis);
        }
    }

    private static Optional<String> readConfig(String key) {
        String property = System.getProperty(key);
        if (property != null && !property.isBlank()) {
            return Optional.of(property.trim());
        }
        String environmentValue = System.getenv(key);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return Optional.of(environmentValue.trim());
        }
        return Optional.empty();
    }

    private static long parseLong(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private void printPlayerCave(@NonNull Player player) {
        output.println("You are in cave " + player.getCurrentRoom() + ". Tunnels lead to caves " +
                caves[player.getCurrentRoom()].getLinkedCaves()[0] + ", " +
                caves[player.getCurrentRoom()].getLinkedCaves()[1] + " and " +
                caves[player.getCurrentRoom()].getLinkedCaves()[2]);
    }
}