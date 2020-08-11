package pitest;

import core.MuRa;
import core.RankedMutant;
import core.ReportGenerator;
import lumutator.Configuration;
import lumutator.Mutant;
import org.apache.commons.cli.*;

import java.nio.file.Paths;
import java.util.List;

import static pitest.Parser.getMutantsWithMutantType;

/**
 * Main class: uses MuRa with PITest.
 */
public class PITest {

    public static void main(String[] args) {

        try {
            Options options = new Options();

            Option configOption = new Option("c", "config", true, "Configuration file");
            configOption.setRequired(true);
            options.addOption(configOption);
            options.addOption(
                    new Option("m", "mutants", true, "PITest output directory (usually this is /target/pit-reports)")
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

            // Parse PITest mutants
            List<Mutant> mutants = getMutantsWithMutantType(cmd.hasOption("mutants") ?
                            cmd.getOptionValue("mutants") :
                            Paths.get(config.get("projectDir"), "target", "pit-reports").toString(),
                    true, RankedMutant.class
            );

            // Call MuRa
            MuRa.rankMutants(mutants);

            // Generate report
            ReportGenerator.generateReport(mutants);

        } catch (Exception e) {
            System.out.println("MuRa caught an exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

}
