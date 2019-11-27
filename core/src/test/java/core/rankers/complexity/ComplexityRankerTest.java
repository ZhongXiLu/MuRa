package core.rankers.complexity;

import core.RankedMutant;
import core.rankers.RankingEnvironment;
import lumutator.Mutant;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link ComplexityRanker}.
 */
public class ComplexityRankerTest extends RankingEnvironment {

    /**
     * Test ranking a list of mutants.
     */
    @Test
    public void testRanking() {
        // Mutants are unranked
        for (Mutant mutant : mutants) {
            assertEquals(1.0, ((RankedMutant) mutant).getScore(), 0.001);
        }

        try {
            // Do the actual ranking based on complexity
            ComplexityRanker.rank(mutants, getClass().getClassLoader().getResource("bank/target/classes").getFile());
        } catch (IOException e) {
            fail();
        }

        List<Double> scores = new ArrayList<>();
        for (Mutant mutant : mutants) {
            RankedMutant rankedMutant = (RankedMutant) mutant;
            assertEquals(1, rankedMutant.getRankCoefficients().size());
            scores.add(rankedMutant.getScore());
        }
        scores.sort(Double::compareTo);
        // Check scores for some mutants
        assertEquals(0.143, scores.get(0), 0.001);
        assertEquals(1.0, scores.get(scores.size() - 1), 0.001);
    }
}