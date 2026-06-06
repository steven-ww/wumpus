package com.example.wumpus.actions;

import com.example.wumpus.Cave;
import com.example.wumpus.Player;
import com.example.wumpus.io.Output;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public class HazardChecker {

    Output output;

    public HazardChecker(Output output) {
      this.output = output;
    }

    RandomGenerator random = RandomGenerator.getDefault();

    public void checkForHazards(Cave[] caves, Player player) {
        Cave currentCave = caves[player.getCurrentRoom()];
        if (currentCave.hasHazard()) {
            if (currentCave.hasWumpus()) {
                output.println("You bumped into the Wumpus!");
                bumpTheWumpus(caves, currentCave, player);
            }
            if (player.getState() == Player.PlayerState.ALIVE && currentCave.hasBats()) {
                int newCave = random.nextInt(caves.length);
                player.setCurrentRoom(newCave);
                output.println("Giant bats picked you up and dropped you in cave " + newCave + "!");
            }
            if (currentCave.isBottomLessPit()) {
                output.println("You fell into a bottomless pit. You lose.");
                player.setState(Player.PlayerState.DEAD);
            }
        }
    }

    public void bumpTheWumpus(Cave[] caves, Cave currentCave, Player player) {
        int newWumpusCaveOption = random.nextInt(4);
        if (newWumpusCaveOption < 3 ) {
            int wumpusCaveNumber = currentCave.getLinkedCaves()[newWumpusCaveOption];
            caves[wumpusCaveNumber].setHasWumpus(true);
            currentCave.setHasWumpus(false);
        }
        if (caves[player.getCurrentRoom()].hasWumpus()) {
            output.println("The Wumpus found you and ate you. You lose.");
            player.setState(Player.PlayerState.DEAD);
        }
    }

    public Cave getWumpusCave(Cave[] caves) {
        return Arrays.stream(caves)
                .filter(Cave::hasWumpus)
                .findAny()
                .orElseThrow(() -> new RuntimeException("No cave has the Wumpus!"));
    }

    public void printHazards(Cave[] caves, int room) {
        Arrays.stream(caves[room].getLinkedCaves())
                .forEach(caveIndex -> printHazard(caves[caveIndex]));
    }

    private void printHazard(Cave cave) {
        if (cave.hasBats()) {
            output.println("You hear the flapping of giant bats nearby.");
            return;
        }
        if (cave.hasWumpus()) {
            output.println("You smell something terrible nearby.");
            return;
        }
        if (cave.isBottomLessPit()) {
            output.println("You feel a cold draft from a nearby cave.");
        }
    }
}
