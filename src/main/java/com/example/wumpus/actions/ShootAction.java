package com.example.wumpus.actions;

import com.example.wumpus.Cave;
import com.example.wumpus.Player;
import com.example.wumpus.io.Input;
import com.example.wumpus.io.Output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.random.RandomGenerator;

public class ShootAction {

    Input input;
    Output output;
    RandomGenerator randomGenerator = RandomGenerator.getDefault();

    public ShootAction(Input input, Output output) {
        this.output = output;
        this.input = input;
    }

    public void shootArrow(Cave[] caves, Player player) {
        int noOfCavesToShootInto = getValidNumberOfCavesToShootInto();
        ArrayList<Integer> cavesToShootInto = getCavesToShootInto(player.getCurrentRoom(), noOfCavesToShootInto);
        shootArrowThroughCaves(caves, cavesToShootInto, player.getCurrentRoom(), player);
    }

    private void shootArrowThroughCaves(Cave[] caves,
                                        ArrayList<Integer> cavesToShootInto,
                                        int currentCaveIndex, Player player) {
        boolean arrowIsFlyingTrue = true;

        for (int caveIndex : cavesToShootInto) {

            if (arrowIsFlyingTrue) {
                OptionalInt maybeCaveInd = Arrays.stream(caves[currentCaveIndex].getLinkedCaves())
                        .filter(linkedCaveInd -> linkedCaveInd == caveIndex).findAny();

                if (maybeCaveInd.isPresent()) {
                    currentCaveIndex = maybeCaveInd.getAsInt();
                    if (checkForWumpus(caves, currentCaveIndex, player)) {
                        return;
                    }
                    if (currentCaveIndex == player.getCurrentRoom()) {
                        output.println("Your arrow flew into cave " + currentCaveIndex + " and you shot yourself!");
                        player.setState(Player.PlayerState.DEAD);
                        return;
                    }
                } else {
                    arrowIsFlyingTrue = false;
                    output.println("Your arrow couldn't find cave " + caveIndex + " from cave " + currentCaveIndex +
                            " and is flying off course!");
                }
            } else {
                int randdomLinkedCave = randomGenerator.nextInt(3);
                currentCaveIndex = caves[currentCaveIndex].getLinkedCaves()[randdomLinkedCave];
                if (checkForWumpus(caves, currentCaveIndex, player)) {
                    return;
                }
                if (currentCaveIndex == player.getCurrentRoom()) {
                    output.println("Your arrow flew into cave " + currentCaveIndex + " and you shot yourself!");
                    player.setState(Player.PlayerState.DEAD);
                    return;
                }
            }
        }
        output.println("Your arrow missed and flew into cave " + currentCaveIndex + ".");

    }

    private boolean checkForWumpus(Cave[] caves, int caveIndex, Player player) {
        Cave cave = caves[caveIndex];
        if (cave.hasWumpus()) {
            output.println("Your arrow flew into cave " + caveIndex + " and killed the Wumpus! You win!");
            player.setState(Player.PlayerState.WINNER);
            return true;
        }
        return false;
    }

    private ArrayList<Integer> getCavesToShootInto(int startingRoom, int noOfCavesToShootInto) {
        ArrayList<Integer> cavesToShootInto = new ArrayList<>();
        for (int i = 0; i < noOfCavesToShootInto; i++) {
            int roomToCompare;
            if (i < 2) {
                roomToCompare = startingRoom;
            } else {
                roomToCompare = cavesToShootInto.get(i - 2);
            }
            cavesToShootInto.add(getValidCaveToShootInto("Cave " + i + "?", roomToCompare)) ;
        }
        return cavesToShootInto;
    }

    private int getValidNumberOfCavesToShootInto() {
        boolean isValidNumber = false;
        int number = 0;
        while (!isValidNumber) {
            try {
                String noOfCavesToShootInto = input.readln("How many caves should the arrow fly through? (1-5)");

                number = Integer.parseInt(noOfCavesToShootInto);
                if (number >= 1 && number <= 5) {
                    isValidNumber = true;
                } else {
                    output.println("Please enter a number between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                output.println("Please enter a valid number between 1 and 5.");
            }
        }
        return number;
    }

    private Integer getValidCaveToShootInto(String prompt, int currentRoom) {
        boolean isValidNumber = false;
        int number = 0;
        while (!isValidNumber) {
            try {
                String caveToShootInto = input.readln(prompt);

                number = Integer.parseInt(caveToShootInto);
                if (number >= 0 && number <= 19) {
                    if (number == currentRoom) {
                        output.println("Arrows aren't that crooked — pick another cave.");
                    } else {
                        isValidNumber = true;
                    }
                } else {
                    output.println("Please enter a valid cave number between 0 and 19.");
                }
            } catch (NumberFormatException e) {
                output.println("Please enter a valid cave number between 0 and 19.");
            }
        }
        return number;
    }
}
