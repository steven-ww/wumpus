package za.co.sww.game.wumpus.commentary;

import java.util.List;

public record CommentarySnapshot(
        String action,
        Integer targetRoom,
        String outcome,
        Integer playerRoom,
        List<Integer> adjacentRooms,
        List<String> hazardWarnings,
        Integer arrowsRemaining,
        Integer movesTaken,
        List<String> previousActionSummaries,
        DebugHiddenState debugHiddenState
) {
    public record DebugHiddenState(
            Integer wumpusRoom,
            List<Integer> pits,
            List<Integer> bats
    ) {
    }
}
