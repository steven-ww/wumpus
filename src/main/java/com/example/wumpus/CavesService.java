package com.example.wumpus;

import java.util.random.RandomGenerator;

public class CavesService {

    public static final int CAVE_COUNT = 20;

    public static final int PIT_COUNT = 2;
    public static final int BAT_COUNT = 2;

    public static final int WUMPUS_COUNT = 1;
    public static final int PLAYER_COUNT = 1;

    private int playerStartCaveIndex = 0;

    /**
     * Dodecahedron adjacency, 0-based: cave {@code i} connects to the three
     * caves listed in {@code ADJACENCY[i]}. Every edge is symmetric, every
     * cave has exactly three distinct neighbours, and no cave links to itself.
     */
    private static final int[][] ADJACENCY = {
            {1, 4, 7},      // 0
            {0, 2, 9},      // 1
            {1, 3, 11},     // 2
            {2, 4, 13},     // 3
            {0, 3, 5},      // 4
            {4, 6, 14},     // 5
            {5, 7, 16},     // 6
            {0, 6, 8},      // 7
            {7, 9, 17},     // 8
            {1, 8, 10},     // 9
            {9, 11, 18},    // 10
            {2, 10, 12},    // 11
            {11, 13, 19},   // 12
            {3, 12, 14},    // 13
            {5, 13, 15},    // 14
            {14, 16, 19},   // 15
            {6, 15, 17},    // 16
            {8, 16, 18},    // 17
            {10, 17, 19},   // 18
            {12, 15, 18},   // 19
    };

    /**
     * Builds all caves wired up as a dodecahedron.
     *
     * @return an array of {@link #CAVE_COUNT} caves with symmetric links.
     */
    public Cave[] buildCaves() {
        Cave[] caves = new Cave[CAVE_COUNT];
        for (int i = 0; i < CAVE_COUNT; i++) {
            caves[i] = new Cave(ADJACENCY[i]);
        }
        return caves;
    }

    /**
     * Sets {@link #PIT_COUNT} distinct caves to be bottomless pits using the
     * platform default random generator.
     *
     * @param caves the caves to assign pits to.
     */
    public int initializeCaves(Cave[] caves) {
        return initializeCaves(caves, RandomGenerator.getDefault());
    }

    /**
     * Sets {@link #PIT_COUNT} distinct caves to be bottomless pits.
     *
     * <p>Caves are sampled without replacement, so two pits can never land on
     * the same cave and no retry loop is needed.
     *
     * @param caves the caves to assign pits to.
     * @param random the source of randomness (injected for testability).
     */
    public int initializeCaves(Cave[] caves, RandomGenerator random) {
        // Partial Fisher-Yates shuffle: pick PIT_COUNT distinct indices.
        int[] pool = new int[caves.length];
        for (int i = 0; i < pool.length; i++) {
            pool[i] = i;
        }

        int pitsAllocated = 0;
        int batsAllocated = 0;
        int wumpusAllocated = 0;
        int totalCavesToUpdate = PIT_COUNT + BAT_COUNT + WUMPUS_COUNT + PLAYER_COUNT;
        for (int i = 0; i < totalCavesToUpdate; i++) {
            int j = i + random.nextInt(pool.length - i);
            int picked = pool[j];
            pool[j] = pool[i];
            pool[i] = picked;
            if (pitsAllocated < PIT_COUNT) {
                caves[picked].setIsBottomLessPit(true);
                pitsAllocated++;
            } else  {
                if (batsAllocated < BAT_COUNT) {
                    caves[picked].setHasBats(true);
                    batsAllocated++;
                } else {
                    if (wumpusAllocated < WUMPUS_COUNT) {
                        caves[picked].setHasWumpus(true);
                        wumpusAllocated++;
                    } else {
                        caves[picked].setHasPlayer(true);
                        playerStartCaveIndex = picked;
                    }

                }
            }

        }
        return playerStartCaveIndex;
    }

}
