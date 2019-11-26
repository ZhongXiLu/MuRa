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

            // Explicitly initialize configuration (which is needed by the PITest parser)
            Configuration.getInstance().initialize(cmd.getOptionValue("config"));
            Configuration config = Configuration.getInstance();

            // Parse PITest mutants
            List<Mutant> mutants = getMutantsWithMutantType(cmd.hasOption("mutants") ?
                            cmd.getOptionValue("mutants") :
                            Paths.get(config.get("projectDir"), "target", "pit-reports").toString(),
                    false, RankedMutant.class
            );

            // Call MuRa
            List<Mutant> rankedMutants = MuRa.rankMutants(mutants, cmd.getOptionValue("config"));

            // Generate report
            ReportGenerator.generateReport(rankedMutants, ".");

        } catch (Exception e) {
            System.out.println("MuRa caught an exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

}
