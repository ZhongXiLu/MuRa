package core.rankers;

import core.RankedMutant;
import core.TestEnvironment;
import lumutator.Mutant;
import org.junit.Before;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static pitest.Parser.getMutantsWithMutantType;

/**
 * Contains a list of mutants from a small banking application.
 */
public class RankingEnvironment extends TestEnvironment {

    /**
     * List of mutants.
     */
    protected List<Mutant> mutants;

    /**
     * Parse the PITest mutants.
     */
    @Before
    public void setUp() {
        try {
            mutants = getMutantsWithMutantType(
                    getClass().getClassLoader().getResource("bank/pit-reports").getFile(),
                    false, RankedMutant.class
            );
        } catch (IOException e) {
            fail();
        }

        // Mutants are unranked
        for (Mutant mutant : mutants) {
            assertEquals(1.0, ((RankedMutant) mutant).getScore(), 0.001);
        }
    }

}
