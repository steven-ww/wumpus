package za.co.sww.game.wumpus;

import za.co.sww.game.wumpus.service.HuntService;

public class App {

    void main() {
        HuntService h = new HuntService();
        h.runGame();
    }
}
