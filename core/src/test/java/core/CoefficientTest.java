package core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link Coefficient}.
 */
public class CoefficientTest {

    /**
     * Test all the getters and setters.
     */
    @Test
    public void testGettersAndSetters() {

        Coefficient c = new Coefficient("Method A", 0.5, "Explanation");
        assertEquals("Method A", c.getRanker());
        assertEquals(0.5, c.getValue(), 0.001);
        assertEquals("Explanation", c.getExplanation());

        Coefficient c2 = new Coefficient("", 1.0, "");
        assertEquals("", c2.getRanker());
        assertEquals(1.0, c2.getValue(), 0.001);
        assertEquals("", c2.getExplanation());
        c2.setValue(0.0);
        assertEquals(0.0, c2.getValue(), 0.001);

        try {
            new Coefficient("Method A", -1.0);
            new Coefficient("Method A", 2.0);
            fail();
        } catch (IllegalArgumentException e) {
            // Raises exception = what is expected
            assertEquals("Coefficient value should be in [0,1]", e.getMessage());
        }
        try {
            c2.setValue(-1.1);
            fail();
        } catch (IllegalArgumentException e) {
            // Raises exception = what is expected
            assertEquals("Coefficient value should be in [0,1]", e.getMessage());
        }
    }
}