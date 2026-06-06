package com.example.wumpus.io;

public class ConsoleOutput implements Output {
    @Override
    public void println(String message) {
        IO.println(message);
    }
}
