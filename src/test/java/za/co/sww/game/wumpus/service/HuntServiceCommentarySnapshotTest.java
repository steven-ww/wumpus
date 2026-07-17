package za.co.sww.game.wumpus.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.sww.game.wumpus.commentary.CommentaryDispatcher;
import za.co.sww.game.wumpus.commentary.CommentarySnapshot;
import za.co.sww.game.wumpus.domain.Cave;
import za.co.sww.game.wumpus.domain.Player;
import za.co.sww.game.wumpus.io.Input;
import za.co.sww.game.wumpus.io.Output;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HuntServiceCommentarySnapshotTest {
    private TestInput input;
    private TestOutput output;
    private CapturingDispatcher dispatcher;
    private HuntService huntService;
    private Cave[] caves;

    @BeforeEach
    void setUp() {
        input = new TestInput();
        output = new TestOutput();
        dispatcher = new CapturingDispatcher(output);
        huntService = new HuntService(input, output, dispatcher);
        caves = createDefaultCaves();
        huntService.caves = caves;
    }

    @Test
    void shouldBuildMoveSnapshotForPitDeath() throws Exception {
        Player player = new Player();
        player.setCurrentRoom(0);
        caves[1].setIsBottomLessPit(true);
        input.addInput("1");

        invokeRunMove(huntService, player);

        CommentarySnapshot snapshot = dispatcher.lastSnapshot();
        assertNotNull(snapshot);
        assertEquals("MOVE", snapshot.action());
        assertEquals("MOVE_TO_ROOM", snapshot.actionIntent());
        assertEquals(1, snapshot.intendedTargetRoom());
        assertEquals(List.of(), snapshot.nominatedPath());
        assertEquals(1, snapshot.targetRoom());
        assertEquals("PIT_DEATH", snapshot.outcome());
    }

    @Test
    void shouldBuildMoveSnapshotForBatsRelocation() throws Exception {
        Player player = new Player();
        player.setCurrentRoom(0);
        caves[1].setHasBats(true);
        setField(huntService.hazardChecker, "random", new FixedRandom(5));
        input.addInput("1");

        invokeRunMove(huntService, player);

        CommentarySnapshot snapshot = dispatcher.lastSnapshot();
        assertNotNull(snapshot);
        assertEquals("MOVE", snapshot.action());
        assertEquals("MOVE_TO_ROOM", snapshot.actionIntent());
        assertEquals(1, snapshot.intendedTargetRoom());
        assertEquals("BATS_RELOCATED", snapshot.outcome());
    }

    @Test
    void shouldBuildMoveSnapshotForWumpusBump() throws Exception {
        Player player = new Player();
        player.setCurrentRoom(0);
        caves[1].setHasWumpus(true);
        setField(huntService.hazardChecker, "random", new FixedRandom(0));
        input.addInput("1");

        invokeRunMove(huntService, player);

        CommentarySnapshot snapshot = dispatcher.lastSnapshot();
        assertNotNull(snapshot);
        assertEquals("MOVE", snapshot.action());
        assertEquals("MOVE_TO_ROOM", snapshot.actionIntent());
        assertEquals(1, snapshot.intendedTargetRoom());
        assertEquals("WUMPUS_BUMPED", snapshot.outcome());
    }

    @Test
    void shouldBuildShootSnapshotForShotSelf() throws Exception {
        Player player = new Player();
        player.setCurrentRoom(0);
        caves[18].setHasWumpus(true);
        input.addInput("3");
        input.addInput("2");
        input.addInput("1");
        input.addInput("0");

        invokeRunShoot(huntService, player);

        CommentarySnapshot snapshot = dispatcher.lastSnapshot();
        assertNotNull(snapshot);
        assertEquals("SHOOT", snapshot.action());
        assertEquals("SHOOT_THROUGH_CAVES", snapshot.actionIntent());
        assertEquals(0, snapshot.intendedTargetRoom());
        assertEquals(List.of(2, 1, 0), snapshot.nominatedPath());
        assertEquals(0, snapshot.targetRoom());
        assertEquals("SHOT_SELF", snapshot.outcome());
    }

    @Test
    void shouldBuildShootSnapshotForShotHit() throws Exception {
        Player player = new Player();
        player.setCurrentRoom(0);
        caves[2].setHasWumpus(true);
        input.addInput("1");
        input.addInput("2");

        invokeRunShoot(huntService, player);

        CommentarySnapshot snapshot = dispatcher.lastSnapshot();
        assertNotNull(snapshot);
        assertEquals("SHOOT", snapshot.action());
        assertEquals("SHOOT_THROUGH_CAVES", snapshot.actionIntent());
        assertEquals(2, snapshot.intendedTargetRoom());
        assertEquals(List.of(2), snapshot.nominatedPath());
        assertEquals(2, snapshot.targetRoom());
        assertEquals("SHOT_WUMPUS", snapshot.outcome());
    }

    @Test
    void shouldBuildShootSnapshotForShotMiss() throws Exception {
        Player player = new Player();
        player.setCurrentRoom(0);
        caves[10].setHasWumpus(true);
        Object shootHazardChecker = getField(huntService.shootAction, "hazardChecker");
        setField(shootHazardChecker, "random", new FixedRandom(0));
        input.addInput("1");
        input.addInput("1");

        invokeRunShoot(huntService, player);

        CommentarySnapshot snapshot = dispatcher.lastSnapshot();
        assertNotNull(snapshot);
        assertEquals("SHOOT", snapshot.action());
        assertEquals("SHOOT_THROUGH_CAVES", snapshot.actionIntent());
        assertEquals(1, snapshot.intendedTargetRoom());
        assertEquals(List.of(1), snapshot.nominatedPath());
        assertEquals(1, snapshot.targetRoom());
        assertEquals("SHOT_MISSED", snapshot.outcome());
    }

    private static void invokeRunMove(HuntService huntService, Player player) throws Exception {
        Method runMove = HuntService.class.getDeclaredMethod("runMove", Player.class);
        runMove.setAccessible(true);
        runMove.invoke(huntService, player);
    }

    private static void invokeRunShoot(HuntService huntService, Player player) throws Exception {
        Method runShoot = HuntService.class.getDeclaredMethod("runShoot", Player.class);
        runShoot.setAccessible(true);
        runShoot.invoke(huntService, player);
    }

    private static Cave[] createDefaultCaves() {
        Cave[] caves = new Cave[20];
        for (int i = 0; i < caves.length; i++) {
            caves[i] = new Cave(new int[]{(i + 1) % 20, (i + 2) % 20, (i + 19) % 20});
        }
        return caves;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static class CapturingDispatcher extends CommentaryDispatcher {
        private CommentarySnapshot lastSnapshot;

        private CapturingDispatcher(Output output) {
            super(snapshot -> Optional.empty(), output, 50);
        }

        @Override
        public void dispatch(CommentarySnapshot snapshot) {
            this.lastSnapshot = snapshot;
        }

        public CommentarySnapshot lastSnapshot() {
            return lastSnapshot;
        }
    }

    private static class TestInput implements Input {
        private final Queue<String> values = new ArrayDeque<>();

        public void addInput(String value) {
            values.add(value);
        }

        @Override
        public String readln(String prompt) {
            return values.remove();
        }
    }

    private static class TestOutput implements Output {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void println(String message) {
            messages.add(message);
        }
    }

    private static class FixedRandom implements RandomGenerator {
        private final int value;

        private FixedRandom(int value) {
            this.value = value;
        }

        @Override
        public int nextInt() {
            return value;
        }

        @Override
        public int nextInt(int bound) {
            return value;
        }

        @Override
        public long nextLong() {
            return 0;
        }
    }
}
