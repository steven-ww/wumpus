package com.example.wumpus;

public class Player {
    int numberOfArrows = 5;
    PlayerState state = PlayerState.ALIVE;
    int currentRoom = 0;

    public int getNumberOfArrows() {
        return numberOfArrows;
    }

    public void setNumberOfArrows(int numberOfArrows) {
        this.numberOfArrows = numberOfArrows;
    }

    public PlayerState getState() {
        return state;
    }

    public void setState(PlayerState state) {
        this.state = state;
    }

    public int getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(int currentRoom) {
        this.currentRoom = currentRoom;
    }


    public enum PlayerState {
        ALIVE, DEAD, WINNER
    }
}
