package core.rankers.impact;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import lumutator.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jacoco.agent.AgentJar;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Calculate the coverage on a set of files from a project.
 * Adapted from: https://github.com/jacoco/jacoco/blob/master/org.jacoco.examples/src/org/jacoco/examples/CoreTutorial.java.
 */
public final class CoverageRunner {

    /**
     * All the class files.
     */
    private List<File> classFiles;

    /**
     * Map containing the covered instruction count of each class.
     */
    private Map<String, Integer> classCoveredInstructionCounts;

    /**
     * Map containing the line coverage of each class.
     */
    private Map<String, Double> classLineCoverages;

    /**
     * String with all the tests classes separated by a space.
     */
    private String allTestNames;

    /**
     * Path to the JaCoCo agent jar file.
     */
    private File agentJar;

    /**
     * Constructor.
     *
     * @param classesDir Directory containing all the class files of the source.
     */
    public CoverageRunner(final String classesDir) throws IOException {
        Configuration config = Configuration.getInstance();
        classCoveredInstructionCounts = new HashMap<>();
        classLineCoverages = new HashMap<>();
        allTestNames = getAllTestNames(config.get("testDir"));
        classFiles = (List<File>) FileUtils.listFiles(new File(classesDir), new String[]{"class"}, true);
        agentJar = AgentJar.extractToTempLocation();
    }

    /**
     * Calculate the coverage of the system using the tests.
     *
     * @return Map containing the covered instruction count of each class, null if running the coverage was not successful (e.g. failed due to timeout).
     */
    public Map<String, Integer> runCoverage() throws IOException {
        Configuration config = Configuration.getInstance();
        classCoveredInstructionCounts = new HashMap<>();
        File execFile = new File("jacoco.exec");

        // Run all tests using the JaCoCo Agent
        final long start = System.currentTimeMillis();
        final String testCommand = "java -javaagent:" + agentJar.getCanonicalPath()
                + "=destfile=" + execFile.getCanonicalPath()
                + ",append=false -cp " + config.get("classPath") + " "
                + config.get("testRunner") + " " + allTestNames;

        try {
            Process process = Runtime.getRuntime().exec(testCommand);
            if (config.hasParameter("timeout")) {
                readFromBuffer(new BufferedReader(new InputStreamReader(process.getInputStream())));
                readFromBuffer(new BufferedReader(new InputStreamReader(process.getErrorStream())));
                if (!process.waitFor(Integer.parseInt(config.get("timeout")), TimeUnit.SECONDS)) {
                    // Mutation might cause a deadlock
                    process.destroy();
                    return null;
                }
            }
            // Set timeout automatically based on the original test runtime, if none was set
            if (!config.hasParameter("timeout")) {
                readFromBuffer(new BufferedReader(new InputStreamReader(process.getInputStream())));
                readFromBuffer(new BufferedReader(new InputStreamReader(process.getErrorStream())));
                process.waitFor();
                final long end = System.currentTimeMillis();
                config.set("timeout", String.valueOf((int) Math.ceil((end - start) * 1.5 / 1000f) + 1));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed calculating coverage while running the tests");
        }
        analyzeCoverage(execFile);
        return classCoveredInstructionCounts;
    }

    public Map<String, Double> getClassLineCoverages() {
        return classLineCoverages;
    }

    /**
     * Read from buffer.
     *
     * @param reader The buffered reader.
     */
    private void readFromBuffer(BufferedReader reader) {
        final Runnable runnable = () -> {
            try {
                try {
                    while ((reader.readLine()) != null) {
                    }
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
            }
        };
        final Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Get all the coverage information using the execution file and the class files.
     *
     * @param execFile The execution file from running all the tests.
     * @throws IOException If the execution file cannot be found.
     */
    private void analyzeCoverage(File execFile) throws IOException {
        ExecFileLoader execFileLoader = new ExecFileLoader();
        execFileLoader.load(execFile);

        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);

        for (final File file : classFiles) {
            analyzer.analyzeAll(file);
        }

        for (IClassCoverage classCoverage : coverageBuilder.getClasses()) {
            classCoveredInstructionCounts.put(classCoverage.getName(), classCoverage.getInstructionCounter().getCoveredCount());
            for (IMethodCoverage methodCoverage : classCoverage.getMethods()) {
                classLineCoverages.put(classCoverage.getName().replace("/", ".") + "." + methodCoverage.getName(), methodCoverage.getLineCounter().getCoveredRatio());
            }
        }
    }

    /**
     * Get all the test classes in the original project including its package.
     *
     * @param testDirectory Directory that contains all the test files.
     * @return String with all the tests classes separated by a space.
     */
    private String getAllTestNames(String testDirectory) {
        StringBuilder stringBuilder = new StringBuilder();

        List<File> testFiles = (List<File>) FileUtils.listFiles(
                new File(testDirectory),
                new RegexFileFilter("(?i)^(.*?test.*?)"),       // only match test files
                DirectoryFileFilter.DIRECTORY
        );

        for (File file : testFiles) {
            try {
                CompilationUnit compilationUnit = JavaParser.parse(file);
                final String classToDebug = compilationUnit.getPackage().getName() + "." + FilenameUtils.removeExtension(file.getName());
                stringBuilder.append(" ");
                stringBuilder.append(classToDebug);

            } catch (ParseException | IOException e) {
                // Should not be possible
            }
        }

        return stringBuilder.toString();
    }

}