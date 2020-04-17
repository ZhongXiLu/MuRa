package core;

import lumutator.Mutant;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Export mutant scores to a csv file.
 */
public class MutantExporter {

    /**
     * Export mutant scores to a csv file.
     *
     * @param mutants List of mutants to be exported.
     */
    public static void exportMutantsToCSV(List<Mutant> mutants) throws IOException {
        exportMutantsToCSV(mutants, "mutants.csv");
    }

    /**
     * Export mutant scores to a csv file.
     *
     * @param mutants  List of mutants to be exported.
     * @param fileName Name of the csv file.
     */
    public static void exportMutantsToCSV(List<Mutant> mutants, String fileName) throws IOException {
        PrintWriter writer = new PrintWriter(fileName, StandardCharsets.UTF_8);

        if (!mutants.isEmpty()) {
            List<Coefficient> coeffs = ((RankedMutant) mutants.get(0)).getRankCoefficients();
            for (Coefficient coeff : coeffs) {
                writer.print(coeff.getRanker() + ",");
            }
            writer.println();
        }

        for (Mutant mutant : mutants) {
            List<Coefficient> coeffs = ((RankedMutant) mutant).getRankCoefficients();
            for (Coefficient coeff : coeffs) {
                writer.print(coeff.getValue() + ",");
            }
            writer.println();
        }

        writer.close();
    }

}
