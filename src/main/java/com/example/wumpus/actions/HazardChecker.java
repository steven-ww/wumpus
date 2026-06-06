package com.example.wumpus.actions;

import com.example.wumpus.Cave;
import com.example.wumpus.Player;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public class HazardChecker {
    RandomGenerator random = RandomGenerator.getDefault();

    public void checkForHazards(Cave[] caves, Player player) {
        Cave currentCave = caves[player.getCurrentRoom()];
        if (currentCave.hasHazard()) {
            if (currentCave.hasWumpus()) {
                bumpTheWumpus(caves, currentCave);
                IO.println("You bumped into the Wumpus!");
                if (currentCave.hasWumpus()) {
                    IO.println("The Wumpus found you and ate you. You lose.");
                    player.setState(Player.PlayerState.DEAD);
                }
            }
            if (currentCave.hasBats()) {
                int newCave = random.nextInt(caves.length);
                player.setCurrentRoom(newCave);
                IO.println("Giant bats picked you up and dropped you in cave " + newCave + "!");
                checkForHazards(caves, player);
            }
            if (currentCave.isBottomLessPit()) {
                IO.println("You fell into a bottomless pit. You lose.");
                player.setState(Player.PlayerState.DEAD);
            }
        }
    }

    private void bumpTheWumpus(Cave[] caves, Cave currentCave) {
        int newWumpusCaveOption = random.nextInt(4);
        if (newWumpusCaveOption < 3 ) {
            int wumpusCaveNumber = currentCave.getLinkedCaves()[newWumpusCaveOption];
            caves[wumpusCaveNumber].setHasWumpus(true);
            currentCave.setHasWumpus(false);
        }
    }

    public void printHazards(Cave[] caves, int room) {
        Arrays.stream(caves[room].getLinkedCaves())
                .forEach(caveIndex -> printHazard(caves[caveIndex]));
    }

    private void printHazard(Cave cave) {
        if (cave.hasBats()) {
            IO.println("You hear the flapping of giant bats nearby.");
            return;
        }
        if (cave.hasWumpus()) {
            IO.println("You smell something terrible nearby.");
            return;
        }
        if (cave.isBottomLessPit()) {
            IO.println("You feel a cold draft from a nearby cave.");
        }
    }
}
