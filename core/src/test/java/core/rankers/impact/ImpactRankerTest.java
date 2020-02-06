package core.rankers.impact;

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
 * Tests for {@link ImpactRanker}.
 */
public class ImpactRankerTest extends RankingEnvironment {

    /**
     * Test ranking a list of mutants.
     */
    @Test
    public void testRanking() {
        try {
            // Rank based on complexity
            ImpactRanker.rank(mutants, getClass().getClassLoader().getResource("bank/target/classes").getFile());
        } catch (IOException e) {
            fail();
        }

        List<Double> scores = new ArrayList<>();
        for (Mutant mutant : mutants) {
            RankedMutant rankedMutant = (RankedMutant) mutant;
            assertEquals(1, rankedMutant.getRankCoefficients().size());
            scores.add(rankedMutant.getRawScore());
        }
        scores.sort(Double::compareTo);
        // Check scores for some mutants
        assertEquals(0.0, scores.get(0), 0.001);
        assertEquals(1.0, scores.get(scores.size() - 1), 0.001);
    }
}