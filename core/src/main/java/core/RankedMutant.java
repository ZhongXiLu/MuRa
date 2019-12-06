package core;

import lumutator.Mutant;

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
    private List<Coefficient> rankCoefficients;

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
            for (Coefficient coeff : rankCoefficients) {
                score += coeffWeight * coeff.getValue();
            }
            return score;
        }
    }

    /**
     * Add a new rank coefficient to this mutant.
     *
     * @param coefficient The new coefficient.
     */
    public void addRankCoefficient(Coefficient coefficient) {
        rankCoefficients.add(coefficient);
    }

    /**
     * Get all the rank coefficients.
     *
     * @return All the rank coefficients.
     */
    public List<Coefficient> getRankCoefficients() {
        return rankCoefficients;
    }
}
