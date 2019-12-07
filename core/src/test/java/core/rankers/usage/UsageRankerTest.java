package core.rankers.usage;

import core.RankedMutant;
import core.rankers.RankingEnvironment;
import lumutator.Mutant;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for {@link UsageRanker}.
 */
public class UsageRankerTest extends RankingEnvironment {

    /**
     * Test ranking a list of mutants.
     */
    @Test
    public void testRanking() {
        // Rank based on complexity
        UsageRanker.rank(mutants, getClass().getClassLoader().getResource("bank/target/classes").getFile());

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