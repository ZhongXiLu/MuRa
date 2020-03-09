package study;

import core.MuRa;
import core.RankedMutant;
import lumutator.Configuration;
import lumutator.Mutant;
import org.apache.commons.cli.*;
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

            // Get all closed bug reports
            final List<Issue> bugReports = IssueRetriever.getAllClosedBugReports(
                    new File(".git/"), cmd.getOptionValue("owner"), cmd.getOptionValue("repo"),
                    cmd.hasOption("label") ? cmd.getOptionValue("label") : "bug"
            );

            for (Issue bugReport : bugReports) {
                // Switch to commit before the bug report was fixed
                Process process = Runtime.getRuntime().exec(
                        "git checkout -f " + bugReport.commitBeforeFix, null, new File(config.get("projectDir"))
                );
                process.waitFor();

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

                // Parse PITest mutants
                List<Mutant> mutants = getMutantsWithMutantType(cmd.hasOption("mutants") ?
                                cmd.getOptionValue("mutants") :
                                Paths.get(config.get("projectDir"), "target", "pit-reports").toString(),
                        true, RankedMutant.class
                );


                // Rank mutants
                List<Mutant> rankedMutants = MuRa.rankMutants(mutants);

                // Evaluate ranking
                RankingEvaluator.evaluateRanking(mutants, bugReport);
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
