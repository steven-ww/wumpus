package za.co.sww.game.wumpus.actions;

import za.co.sww.game.wumpus.domain.Cave;
import za.co.sww.game.wumpus.domain.Player;
import za.co.sww.game.wumpus.io.Output;
import org.jspecify.annotations.NullMarked;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.random.RandomGenerator;

@NullMarked
public class HazardChecker {

    Output output;

    public HazardChecker(Output output) {
      this.output = output;
    }

    RandomGenerator random = RandomGenerator.getDefault();

    public HazardOutcome checkForHazards(Cave[] caves, Player player) {
        Cave currentCave = caves[player.getCurrentRoom()];
        return switch (currentCave.getHazardType()) {
            case BATS -> handleBats(caves, player);
            case WUMPUS -> handleWumpus(caves, currentCave, player);
            case PIT -> handlePit(player);
            case NONE -> HazardOutcome.SAFE;
        };
    }

    private HazardOutcome handleBats(Cave[] caves, Player player) {
        int newCave = random.nextInt(caves.length);
        player.setCurrentRoom(newCave);
        output.println("Giant bats picked you up and dropped you in cave " + newCave + "!");
        HazardOutcome chainedOutcome = checkForHazards(caves, player);
        if (chainedOutcome == HazardOutcome.SAFE) {
            return HazardOutcome.BATS_RELOCATED;
        }
        return chainedOutcome;
    }

    private HazardOutcome handleWumpus(Cave[] caves, Cave currentCave, Player player) {
        output.println("You bumped into the Wumpus!");
        return bumpTheWumpus(caves, currentCave, player);
    }

    private HazardOutcome handlePit(Player player) {
        output.println("You fell into a bottomless pit. You lose.");
        player.setState(Player.PlayerState.DEAD);
        return HazardOutcome.PIT_DEATH;
    }

    public HazardOutcome bumpTheWumpus(Cave[] caves, Cave currentCave, Player player) {
        int newWumpusCaveOption = random.nextInt(4);
        if (newWumpusCaveOption < 3 ) {
            int wumpusCaveNumber = currentCave.getLinkedCaves()[newWumpusCaveOption];
            caves[wumpusCaveNumber].setHasWumpus(true);
            currentCave.setHasWumpus(false);
        }
        if (caves[player.getCurrentRoom()].hasWumpus()) {
            output.println("The Wumpus found you and ate you. You lose.");
            player.setState(Player.PlayerState.DEAD);
            return HazardOutcome.WUMPUS_ATE_PLAYER;
        }
        return HazardOutcome.WUMPUS_BUMPED;
    }

    public Cave getWumpusCave(Cave[] caves) {
        return Arrays.stream(caves)
                .filter(Cave::hasWumpus)
                .findAny()
                .orElseThrow(() -> new RuntimeException("No cave has the Wumpus!"));
    }

    public void printHazards(Cave[] caves, int room) {
        listHazardWarnings(caves, room).forEach(output::println);
    }

    public List<String> listHazardWarnings(Cave[] caves, int room) {
        List<String> warnings = new ArrayList<>();
        Arrays.stream(caves[room].getLinkedCaves())
                .forEach(caveIndex -> maybeHazardWarning(caves[caveIndex]).ifPresent(warnings::add));
        return warnings;
    }

    private java.util.Optional<String> maybeHazardWarning(Cave cave) {
        if (cave.hasBats()) {
            return java.util.Optional.of("You hear the flapping of giant bats nearby.");
        }
        if (cave.hasWumpus()) {
            return java.util.Optional.of("You smell something terrible nearby.");
        }
        if (cave.isBottomLessPit()) {
            return java.util.Optional.of("You feel a cold draft from a nearby cave.");
        }
        return java.util.Optional.empty();
    }

    public enum HazardOutcome {
        SAFE,
        BATS_RELOCATED,
        PIT_DEATH,
        WUMPUS_BUMPED,
        WUMPUS_ATE_PLAYER
    }
}
