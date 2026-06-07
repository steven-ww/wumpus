package za.co.sww.game.wumpus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import za.co.sww.game.wumpus.domain.Cave;
import za.co.sww.game.wumpus.service.CavesService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Verifies that {@link CavesService#buildCaves()} produces a valid dodecahedron graph:
 * 20 caves, each with exactly 3 distinct neighbours, all edges symmetric, and
 * exactly 30 unique edges in total.
 */
class CaveTest {

    static CavesService cavesService;

    @BeforeAll
    static void beforeAll() {
        cavesService = new CavesService();
    }

    @Test
    void buildsTwentyCaves() {
        Cave[] caves = cavesService.buildCaves();
        assertEquals(20, caves.length, "a dodecahedron has 20 vertices");
    }

    @Test
    void everyCaveHasThreeDistinctNeighboursAndNoSelfLoop() {
        Cave[] caves = cavesService.buildCaves();
        for (int i = 0; i < caves.length; i++) {
            int[] links = caves[i].getLinkedCaves();
            assertEquals(3, links.length, "cave " + i + " must have 3 tunnels");

            Set<Integer> distinct = new HashSet<>();
            for (int neighbour : links) {
                assertTrue(neighbour >= 0 && neighbour < caves.length,
                        "cave " + i + " has out-of-range neighbour " + neighbour);
                assertFalse(neighbour == i,
                        "cave " + i + " must not link to itself");
                distinct.add(neighbour);
            }
            assertEquals(3, distinct.size(),
                    "cave " + i + " must have 3 distinct neighbours: " + Arrays.toString(links));
        }
    }

    @Test
    void allTunnelsAreSymmetric() {
        Cave[] caves = cavesService.buildCaves();
        for (int i = 0; i < caves.length; i++) {
            for (int neighbour : caves[i].getLinkedCaves()) {
                boolean linkedBack = contains(caves[neighbour].getLinkedCaves(), i);
                assertTrue(linkedBack,
                        "cave " + neighbour + " must link back to cave " + i);
            }
        }
    }

    @Test
    void hasExactlyThirtyUniqueEdges() {
        Cave[] caves = cavesService.buildCaves();
        Set<String> edges = new HashSet<>();
        for (int i = 0; i < caves.length; i++) {
            for (int neighbour : caves[i].getLinkedCaves()) {
                int lo = Math.min(i, neighbour);
                int hi = Math.max(i, neighbour);
                edges.add(lo + "-" + hi);
            }
        }
        assertEquals(30, edges.size(), "a dodecahedron has 30 edges");
    }

    private static boolean contains(int[] values, int target) {
        for (int value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}
