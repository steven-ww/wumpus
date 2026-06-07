package za.co.sww.game.wumpus.actions;

import za.co.sww.game.wumpus.domain.Cave;
import za.co.sww.game.wumpus.domain.Player;
import za.co.sww.game.wumpus.io.Input;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.OptionalInt;

@NullMarked
public class PlayerMovement {
    Input input;

    public PlayerMovement(Input input) {
        this.input = input;
    }

    public void movePlayer(Cave[] caves, Player player) {
        boolean moving = true;
        while (moving) {
            String destination = input.readln("Move to which cave? ");
            try {
                int caveToMoveTo = Integer.parseInt(destination);
                OptionalInt caveMovedTo = Arrays.stream(caves[player.getCurrentRoom()].getLinkedCaves())
                        .filter(caveIndex -> caveIndex == caveToMoveTo)
                        .findAny();
                if (caveMovedTo.isPresent()) {
                    player.setCurrentRoom(caveMovedTo.getAsInt());
                    moving=false;
                } else {
                    IO.println("You can't move to cave " + caveToMoveTo + " from here.");
                }
            } catch (NumberFormatException e) {
                if (destination.equalsIgnoreCase("exit")) {
                    moving = false;
                } else {
                    IO.println("You can't move to cave " + destination + " from here.");
                }
            }
        }
    }
}
