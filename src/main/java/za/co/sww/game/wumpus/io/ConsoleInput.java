package za.co.sww.game.wumpus.io;

public class ConsoleInput implements Input {

    @Override
    public String readln(String prompt) {
        return IO.readln(prompt);
    }
}

