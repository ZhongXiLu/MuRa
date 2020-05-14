package core;

import lumutator.Mutant;

import java.util.ArrayList;
import java.util.List;

/**
 * Reverse a parameter.
 * If the score for a parameter is x, then the reverse will become 1 -x.
 */
public class ParameterReverser {

    /**
     * Reverse all the parameters in the mutants and add them as a new parameters.
     * The new parameters are labeled as "1-P" where parameter P will be reversed.
     *
     * @param mutants The mutants of which the parameters need to be reversed.
     */
    public static void reverseParameters(List<Mutant> mutants) {

        for (Mutant mutant : mutants) {
            RankedMutant rankedMutant = (RankedMutant) mutant;
            List<Coefficient> reverseParams = new ArrayList<>();
            for (Coefficient coeff : rankedMutant.getRankCoefficients()) {
                reverseParams.add(
                        new Coefficient("_" + coeff.getRanker(), 1 - coeff.getValue(), coeff.getExplanation())
                );
            }
            for (Coefficient coeff : reverseParams) {
                rankedMutant.addRankCoefficient(coeff);
            }
        }

    }

}
