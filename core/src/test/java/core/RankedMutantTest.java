package core;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

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
        mutant.addRankCoefficient(new Coefficient("Method A", 1.0));
        mutant.addRankCoefficient(new Coefficient("Method A", 0.0));
        mutant.addRankCoefficient(new Coefficient("Method A", 0.123));
        assertEquals(3, mutant.getRankCoefficients().size());
        assertEquals(1.0, mutant.getRankCoefficients().get(0).getValue(), 0.001);
        assertEquals("Method A", mutant.getRankCoefficients().get(0).getRanker());
    }

    /**
     * Test the {@link RankedMutant#getScore()} method.
     */
    @Test
    public void testGetScore() {
        assertEquals(1.0, mutant.getScore(), 0.001);

        mutant.addRankCoefficient(new Coefficient("Method A", 1.0));
        assertEquals(1.0, mutant.getScore(), 0.001);
        mutant.addRankCoefficient(new Coefficient("Method A", 0.0));
        assertEquals(0.5, mutant.getScore(), 0.001);
        mutant.addRankCoefficient(new Coefficient("Method A", 0.123));
        assertEquals(0.374, mutant.getScore(), 0.001);
    }
}