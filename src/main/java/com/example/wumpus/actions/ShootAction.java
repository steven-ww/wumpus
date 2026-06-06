package com.example.wumpus.actions;

import com.example.wumpus.Cave;
import com.example.wumpus.Player;
import com.example.wumpus.io.Input;
import com.example.wumpus.io.Output;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.random.RandomGenerator;

public class ShootAction {

    Input input;
    Output output;
    RandomGenerator randomGenerator = RandomGenerator.getDefault();
    HazardChecker hazardChecker;

    public ShootAction(Input input, Output output) {
        this.output = output;
        this.input = input;
        this.hazardChecker = new HazardChecker(output);
    }


    public void shootArrow(Cave[] caves, @NonNull Player player) {
        player.setNumberOfArrows(player.getNumberOfArrows()-1);

        int noOfCavesToShootInto = getValidNumberOfCavesToShootInto();
        ArrayList<Integer> cavesToShootInto = getCavesToShootInto(player.getCurrentRoom(), noOfCavesToShootInto);
        shootArrowThroughCaves(caves, cavesToShootInto, player.getCurrentRoom(), player);

        if (player.getState() == Player.PlayerState.ALIVE && player.getNumberOfArrows() == 0) {
            output.println("You have no more arrows left. You lose.");
            player.setState(Player.PlayerState.DEAD);
        }
    }

    private void shootArrowThroughCaves(@NonNull Cave[] caves,
                                        @NonNull ArrayList<Integer> cavesToShootInto,
                                        int currentCaveIndex, @NonNull Player player) {
        boolean arrowIsOnCourse = true;

        for (int nominatedCave : cavesToShootInto) {
            if (arrowIsOnCourse) {
                OptionalInt maybeCaveInd = Arrays.stream(caves[currentCaveIndex].getLinkedCaves())
                        .filter(linkedCaveInd -> linkedCaveInd == nominatedCave).findAny();

                if (maybeCaveInd.isPresent()) {
                    currentCaveIndex = maybeCaveInd.getAsInt();
                } else {
                    output.println("Your arrow couldn't find cave " + nominatedCave + " from cave " + currentCaveIndex +
                            " and is flying off course!");
                    arrowIsOnCourse = false;
                    int randomLinkedCaveIndex = randomGenerator.nextInt(3);
                    currentCaveIndex = caves[currentCaveIndex].getLinkedCaves()[randomLinkedCaveIndex];
                }
            } else {
                int randomLinkedCaveIndex = randomGenerator.nextInt(3);
                currentCaveIndex = caves[currentCaveIndex].getLinkedCaves()[randomLinkedCaveIndex];
            }

            if (checkForWumpus(caves, currentCaveIndex, player)) {
                return;
            }
            if (currentCaveIndex == player.getCurrentRoom()) {
                output.println("Your arrow flew into cave " + currentCaveIndex + " and you shot yourself!");
                player.setState(Player.PlayerState.DEAD);
                return;
            }
        }
        output.println("Your arrow missed and flew into cave " + currentCaveIndex + ".");
        hazardChecker.bumpTheWumpus(caves, hazardChecker.getWumpusCave(caves), player);
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
