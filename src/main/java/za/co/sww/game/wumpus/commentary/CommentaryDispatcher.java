package za.co.sww.game.wumpus.commentary;

import za.co.sww.game.wumpus.io.Output;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CommentaryDispatcher {
    private final CommentaryClient commentaryClient;
    private final Output output;
    private final long timeoutMillis;

    public CommentaryDispatcher(CommentaryClient commentaryClient, Output output, long timeoutMillis) {
        this.commentaryClient = commentaryClient;
        this.output = output;
        this.timeoutMillis = timeoutMillis;
    }

    public void dispatch(CommentarySnapshot snapshot) {
        output.println("Narrator is thinking...");
        output.println("");
        Optional<String> maybeCommentary = CompletableFuture
                .supplyAsync(() -> commentaryClient.fetchCommentary(snapshot))
                .completeOnTimeout(Optional.empty(), timeoutMillis, TimeUnit.MILLISECONDS)
                .exceptionally(ignored -> Optional.empty())
                .join();
        maybeCommentary.ifPresent(commentary -> output.println("Narrator: " + commentary));
        output.println("-------------------------------");
    }
}
