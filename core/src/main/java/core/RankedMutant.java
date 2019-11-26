package core;

import lumutator.Mutant;

import java.io.File;

/**
 * Mutant class that also stores additional information of a ranked mutants, such as their score.
 */
public class RankedMutant extends Mutant {

    /**
     * The current score of the mutant.
     */
    private double score = 1.0;

    /**
     * Default constructor.
     *
     * @param originalFile The original, unmodified file where the mutant was inserted.
     * @param classFile    The compiled class file that contains the mutant.
     * @param mutatedClass The mutated class, including its package.
     * @param lineNr       The line number where the mutant resides.
     * @param mutator      The type of mutator.
     * @param notes        Extra information about the mutant. (can be anything, it's just to help the user in the end)
     */
    public RankedMutant(File originalFile, File classFile, String mutatedClass, int lineNr, String mutator, String notes) {
        super(originalFile, classFile, mutatedClass, lineNr, mutator, notes);
    }

    /**
     * Get the current score of the mutant.
     *
     * @return The score.
     */
    public double getScore() {
        return score;
    }

    /**
     * Set the current score of the mutant.
     *
     * @param score The new score.
     */
    public void setScore(double score) {
        this.score = score;
    }

}
