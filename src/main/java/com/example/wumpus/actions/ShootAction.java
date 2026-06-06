package com.example.wumpus.actions;

import com.example.wumpus.Cave;
import com.example.wumpus.Player;
import com.example.wumpus.io.Input;
import com.example.wumpus.io.Output;

import java.util.ArrayList;

public class ShootAction {

    Input input;
    Output output;

    public ShootAction(Input input, Output output) {
        this.output = output;
        this.input = input;
    }

    public void shootArrow(Cave[] caves, Player player) {
        int noOfCavesToShootInto = getValidNumberOfCavesToShootInto();
        ArrayList<Integer> cavesToShootInto = new ArrayList<>();

        for (int i = 0; i < noOfCavesToShootInto; i++) {
            cavesToShootInto.add(getValidCaveToShootInto("Cave " + i + "?")) ;
        }

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

    private Integer getValidCaveToShootInto(String prompt) {
        boolean isValidNumber = false;
        int number = 0;
        while (!isValidNumber) {
            try {
                String caveToShootInto = input.readln(prompt);

                number = Integer.parseInt(caveToShootInto);
                if (number >= 0 && number <= 19) {
                    isValidNumber = true;
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
