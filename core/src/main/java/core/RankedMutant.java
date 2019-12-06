package core;

import lumutator.Mutant;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Mutant class that also stores additional information of a ranked mutants, such as their score.
 */
public class RankedMutant extends Mutant {

    /**
     * All the rank coefficients from all the different rankers.
     * Store some additional explanation as well.
     */
    private List<Pair<Double, String>> rankCoefficients;

    /**
     * Default constructor.
     *
     * @param originalFile       The original, unmodified file where the mutant was inserted.
     * @param classFile          The compiled class file that contains the mutant.
     * @param mutatedClass       The mutated class, including its package.
     * @param mutatedMethod      The mutated method.
     * @param mutatedMethodDescr The mutated method description (e.g. (I)V).
     * @param lineNr             The line number where the mutant resides.
     * @param mutator            The type of mutator.
     * @param notes              Extra information about the mutant. (can be anything, it's just to help the user in the end)
     */
    public RankedMutant(File originalFile, File classFile, String mutatedClass, String mutatedMethod,
                        String mutatedMethodDescr, int lineNr, String mutator, String notes) {
        super(originalFile, classFile, mutatedClass, mutatedMethod, mutatedMethodDescr, lineNr, mutator, notes);
        rankCoefficients = new ArrayList<>();
    }

    /**
     * Get the current score of the mutant (from 0 to 1).
     *
     * @return The score.
     */
    public double getScore() {

        if (rankCoefficients.isEmpty()) {
            return 1.0;

        } else {
            double score = 0.0;
            final double coeffWeight = 1.0 / rankCoefficients.size();
            for (Pair<Double, String> coeff : rankCoefficients) {
                score += coeffWeight * coeff.getLeft();
            }
            return score;
        }
    }

    /**
     * Add a new rank coefficient to this mutant.
     *
     * @param coefficient The coefficient (should be in [0,1]).
     * @param explanation Some explanation (optional).
     */
    public void addRankCoefficient(double coefficient, String explanation) {
        if (coefficient < 0 || coefficient > 1) {
            throw new IllegalArgumentException("Coefficient should be in [0,1]");
        }
        rankCoefficients.add(new ImmutablePair<>(coefficient, explanation));
    }

    /**
     * Get all the rank coefficients.
     *
     * @return All the rank coefficients.
     */
    public List<Pair<Double, String>> getRankCoefficients() {
        return rankCoefficients;
    }
}
