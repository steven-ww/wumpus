package za.co.sww.game.wumpus.domain;

public class Cave {



    int[] linkedCaves;

    Boolean isBottomLessPit = false;
    Boolean hasBats = false;
    Boolean hasWumpus = false;

    public Boolean hasPlayer() {
        return hasPlayer;
    }

    public void setHasPlayer(Boolean hasPlayer) {
        this.hasPlayer = hasPlayer;
    }

    Boolean hasPlayer = false;

    public Cave(int[] linkedCaves) {
        this.linkedCaves = linkedCaves;
    }

    public int[] getLinkedCaves() {
        return linkedCaves;
    }

    public void setIsBottomLessPit(Boolean isBottomLessPit) {
        this.isBottomLessPit = isBottomLessPit;
    }

    public void setHasBats(Boolean hasBats) {
        this.hasBats = hasBats;
    }

    public Boolean isBottomLessPit() {
        return isBottomLessPit;
    }

    public Boolean hasBats() {
        return hasBats;
    }

    public Boolean hasWumpus() {
        return hasWumpus;
    }

    public void setHasWumpus(Boolean hasWumpus) {
        this.hasWumpus = hasWumpus;
    }

    public boolean hasHazard() {
        return isBottomLessPit || hasBats || hasWumpus;
    }

    public enum HazardType {
        PIT, BATS, WUMPUS, NONE
    }

    public HazardType getHazardType() {
        if (hasBats) return HazardType.BATS;
        if (hasWumpus) return HazardType.WUMPUS;
        if (isBottomLessPit) return HazardType.PIT;
        return HazardType.NONE;
    }
}
