package core.rankers.history;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link ChangesCountCost}.
 */
public class ChangesCountCostTest {

    /**
     * Simple test to test the constructor.
     */
    @Test
    public void testConstructor() {
        ChangesCountCost ccc = new ChangesCountCost(7, 3, 12.345);
        assertEquals(7, ccc.changes);
        assertEquals(3, ccc.recent);
        assertEquals(12.345, ccc.cost, 0.001);
    }

}