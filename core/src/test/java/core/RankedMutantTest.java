package core;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link RankedMutant}.
 */
public class RankedMutantTest {

    /**
     * The ranked mutant to test.
     */
    private RankedMutant mutant;

    /**
     * Set up two mutants.
     */
    @Before
    public void setUp() {
        mutant = new RankedMutant(
                new File("originalFile.java"),
                new File("originalFile.class"),
                "some.package.Class",
                "someMethod",
                "(I)V",
                123,
                "MathMutator",
                "Some notes."
        );
    }

    /**
     * Test adding and getting rank coefficients.
     */
    @Test
    public void testRankCoefficients() {
        assertEquals(0, mutant.getRankCoefficients().size());
        mutant.addRankCoefficient(1.0, "Ranker A");
        mutant.addRankCoefficient(0.0, "Ranker B");
        mutant.addRankCoefficient(0.123, "Ranker C");
        assertEquals(3, mutant.getRankCoefficients().size());
        assertEquals(1.0, (double) mutant.getRankCoefficients().get(0).getLeft(), 0.001);
        assertEquals("Ranker A", mutant.getRankCoefficients().get(0).getRight());

        try {
            mutant.addRankCoefficient(-1, "");
            mutant.addRankCoefficient(2, "");
            fail();
        } catch (IllegalArgumentException e) {
            // Raises exception = what is expected
            assertEquals("Coefficient should be in [0,1]", e.getMessage());
            assertEquals(3, mutant.getRankCoefficients().size());
        }
    }

    /**
     * Test the {@link RankedMutant#getScore()} method.
     */
    @Test
    public void testGetScore() {
        assertEquals(1.0, mutant.getScore(), 0.001);

        mutant.addRankCoefficient(1.0, "Ranker A");
        assertEquals(1.0, mutant.getScore(), 0.001);
        mutant.addRankCoefficient(0.0, "Ranker B");
        assertEquals(0.5, mutant.getScore(), 0.001);
        mutant.addRankCoefficient(0.123, "Ranker C");
        assertEquals(0.374, mutant.getScore(), 0.001);
    }
}