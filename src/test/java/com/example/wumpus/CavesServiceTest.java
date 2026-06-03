package com.example.wumpus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CavesService#initializeCaves}: it must mark exactly
 * {@link CavesService#PIT_COUNT} distinct caves as bottomless pits.
 */
class CavesServiceTest {

    private final CavesService service = new CavesService();

    private static int countPits(Cave[] caves) {
        int count = 0;
        for (Cave cave : caves) {
            if (cave.isBottomLessPit()) {
                count++;
            }
        }
        return count;
    }

    private static int countBats(Cave[] caves) {
        int count = 0;
        for (Cave cave : caves) {
            if (cave.hasBats()) {
                count++;
            }
        }
        return count;
    }

    @Test
    void setsExactlyTwoPitsAndTwoBats() {
        Cave[] caves = service.buildCaves();
        service.initializeCaves(caves, RandomGenerator.getDefault());
        assertEquals(CavesService.PIT_COUNT, countPits(caves));
        assertEquals(CavesService.BAT_COUNT, countBats(caves));
    }

    @Test
    void freshCavesHaveNoPits() {
        Cave[] caves = service.buildCaves();
        assertEquals(0, countPits(caves), "buildCaves must not pre-set any pits");
    }

    @Test
    void freshCavesHaveNoBats() {
        Cave[] caves = service.buildCaves();
        assertEquals(0, countPits(caves), "buildCaves must not pre-set any bats");
    }

    @Test
    void pitsAreDistinctEvenWhenRandomRepeatsTheSameNumber() {
        // This RNG always returns 0 from nextInt(bound). With a naive retry
        // approach that could loop forever or place fewer than two pits; with
        // sampling-without-replacement it must still yield two distinct pits.
        RandomGenerator alwaysZero = new ConstantRandom(0);

        Cave[] caves = service.buildCaves();
        service.initializeCaves(caves, alwaysZero);

        assertEquals(CavesService.PIT_COUNT, countPits(caves),
                "must place two distinct pits even if the RNG repeats a value");
    }

    @Test
    void batsAreDistinctEvenWhenRandomRepeatsTheSameNumber() {
        // This RNG always returns 0 from nextInt(bound). With a naive retry
        // approach that could loop forever or place fewer than two pits; with
        // sampling-without-replacement it must still yield two distinct pits.
        RandomGenerator alwaysZero = new ConstantRandom(0);

        Cave[] caves = service.buildCaves();
        service.initializeCaves(caves, alwaysZero);

        assertEquals(CavesService.PIT_COUNT, countBats(caves),
                "must place two distinct bats even if the RNG repeats a value");
    }

    @Test
    void everyPitIndexIsWithinRange() {
        Cave[] caves = service.buildCaves();
        service.initializeCaves(caves, new java.util.Random(7));
        for (int i = 0; i < caves.length; i++) {
            if (caves[i].isBottomLessPit() || caves[i].hasBats()) {
                assertTrue(i >= 0 && i < CavesService.CAVE_COUNT);
            }
        }
    }

    /**
     * A {@link RandomGenerator} whose {@code nextInt(bound)} always returns the
     * same value, used to exercise the duplicate-handling path deterministically.
     */
    private static final class ConstantRandom implements RandomGenerator {
        private final int value;

        ConstantRandom(int value) {
            this.value = value;
        }

        @Override
        public int nextInt(int bound) {
            return value % bound;
        }

        @Override
        public long nextLong() {
            return value;
        }
    }

    @Test
    void oneCaveShouldHaveTheWumpus() {
        Cave[] caves = service.buildCaves();
        service.initializeCaves(caves, RandomGenerator.getDefault());
        int wumpusCount = 0;
        for (Cave cave : caves) {
            if (cave.hasWumpus()) {
                wumpusCount++;
            }
        }
        assertEquals(1, wumpusCount, "exactly one cave should have the Wumpus");
    }

    @Test
    void oneCaveShouldHaveThePlayer() {
        Cave[] caves = service.buildCaves();
        service.initializeCaves(caves, RandomGenerator.getDefault());
        int playerCount = 0;
        for (Cave cave : caves) {
            if (cave.hasPlayer()) {
                playerCount++;
            }
        }
        assertEquals(1, playerCount, "exactly one cave should have the Player");
    }
}
