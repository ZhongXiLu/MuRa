package study;

import core.Coefficient;
import core.MuRa;
import core.RankedMutant;
import core.rankers.complexity.ComplexityRanker;
import core.rankers.history.HistoryRanker;
import core.rankers.impact.ImpactRanker;
import core.rankers.usage.UsageRanker;
import lumutator.Configuration;
import lumutator.Mutant;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import study.issue.Issue;
import study.issue.IssueRetriever;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;

import static pitest.Parser.getMutantsWithMutantType;

/**
 * Main class that performs the case study.
 * Currently only works with Maven projects.
 */
public class Study {

    public static void main(String[] args) {

        try {
            Options options = new Options();

            Option configOption = new Option("c", "config", true, "Configuration file");
            configOption.setRequired(true);
            options.addOption(configOption);
            options.addOption(
                    new Option("m", "mutants", true, "PITest output directory (usually this is /target/pit-reports)")
            );
            Option repoOwnerOption = new Option("o", "owner", true, "The name of the owner of the repository");
            repoOwnerOption.setRequired(true);
            options.addOption(repoOwnerOption);
            Option repoNameOption = new Option("r", "repo", true, "The name of the repository");
            repoNameOption.setRequired(true);
            options.addOption(repoNameOption);
            options.addOption(
                    new Option("l", "label", true, "The label on GitHub for an issue that identifies a bug")
            );

            CommandLineParser parser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            CommandLine cmd;

            // Parse command line arguments
            try {
                cmd = parser.parse(options, args);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp("MuRa", options);
                System.exit(1);
                return;
            }

            // Initialize configuration
            Configuration.getInstance().initialize(cmd.getOptionValue("config"));
            Configuration config = Configuration.getInstance();

            // Create log file
            FileUtils.touch(new File(config.get("projectDir") + "/MuRa.log"));

            // Get all closed bug reports
            final List<Issue> bugReports = IssueRetriever.getAllClosedBugReports(
                    new File(".git/"), cmd.getOptionValue("owner"), cmd.getOptionValue("repo"),
                    cmd.hasOption("label") ? cmd.getOptionValue("label") : "bug"
            );

            int i = 1;
            for (Issue bugReport : bugReports) {
                System.out.println("Bug report " + i + "/" + bugReports.size() + ": " + bugReport.id);

                // Switch to commit before the bug report was fixed
                Process process = Runtime.getRuntime().exec(
                        "git checkout -f " + bugReport.commitBeforeFix, null, new File(config.get("projectDir"))
                );
                process.waitFor();

                // Reset config parameters
                Configuration.getInstance().initialize(cmd.getOptionValue("config"));

                // Setup the configuration
                ConfigurationSetup.addPITest(new File(config.get("projectDir") + "/pom.xml"));
                ConfigurationSetup.addClassPath(config);

                // Run mutation testing with PITest
                ProcessBuilder processBuilder = new ProcessBuilder(
                        "mvn", "clean", "test",
                        "-Dfeatures=+EXPORT", "org.pitest:pitest-maven:mutationCoverage"
                );
                processBuilder.directory(new File(config.get("projectDir")));
                processBuilder.redirectErrorStream(true);
                process = processBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((reader.readLine()) != null) {
                }  // read output from buffer, otherwise buffer might get full
                process.waitFor();
                reader.close();

                if (process.exitValue() != 0) {
                    // Tests fail in this specific version of the program
                    System.out.println("Tests failed for issue " + bugReport.id);
                    i++;
                    continue;   // just skip to the next bug report
                }

                // Parse PITest mutants
                List<Mutant> mutants = getMutantsWithMutantType(cmd.hasOption("mutants") ?
                                cmd.getOptionValue("mutants") :
                                Paths.get(config.get("projectDir"), "target", "pit-reports").toString(),
                        true, RankedMutant.class
                );

                List<Mutant> bugRelatedMutants = RankingEvaluator.getMutantsRelatedToBugReport(mutants, bugReport);
                if (bugRelatedMutants.isEmpty()) {
                    // No mutants are related to the current bug report, so we cannot score the ranking
                    System.out.println("No bug related mutants for issue " + bugReport.id);
                    i++;
                    continue;   // just skip to the next bug report
                }

                // Small optimization:
                // If all the bug related mutants have no impact, skip calculating the impact for
                // all the other mutants, since the weight for the impact eventually will be 0
                ImpactRanker.rank(bugRelatedMutants, config.get("classFiles"));
                boolean hasImpact = false;

                // Check if any of the mutants have impact
                for (Mutant mutant : bugRelatedMutants) {
                    List<Coefficient> coeffs = ((RankedMutant) mutant).getRankCoefficients();
                    for (Coefficient coeff : coeffs) {
                        if (coeff.getRanker().equals("Impact")) {
                            if (Double.compare(coeff.getValue(), 0.0) != 0) {
                                hasImpact = true;
                            }
                        }
                    }
                    ((RankedMutant) mutant).clearCoefficients();
                }

                if (!hasImpact) {
                    // Rank mutants without impact
                    ComplexityRanker.rank(mutants, config.get("classFiles"));
                    UsageRanker.rank(mutants, config.get("classFiles"));
                    HistoryRanker.rank(mutants);
                } else {
                    // Otherwise, just call MuRa
                    MuRa.rankMutants(mutants);
                }

                // Find optimal configuration (i.e. weight for each ranking method) and evaluate ranking
                RankingEvaluator.evaluateRanking(mutants, bugReport, config.get("projectDir") + "/MuRa.log");

                i++;
            }

            // Go back to current commit
            Process process = Runtime.getRuntime().exec("git checkout master", null, new File(config.get("projectDir")));
            process.waitFor();

        } catch (Exception e) {
            System.out.println("MuRa caught an exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

}
