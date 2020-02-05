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
import org.jacoco.core.tools.ExecFileLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Calculate the coverage on a set of files from a project.
 * Adapted from: https://github.com/jacoco/jacoco/blob/master/org.jacoco.examples/src/org/jacoco/examples/CoreTutorial.java.
 */
public final class CoverageRunner {

    /**
     * Constructor.
     */
    public CoverageRunner() {
    }

    /**
     * Calculate the coverage of the system using the tests.
     *
     * @param classesDir Directory containing all the class files.
     */
    public void runCoverage(final String classesDir) throws IOException {
        Configuration config = Configuration.getInstance();
        AgentJar.extractToTempLocation();

        // Run all tests using the JaCoCo Agent
        final String testCommand = String.format("java -javaagent:javaagent.jar -cp %s %s %s",
                config.get("classPath"),
                config.get("testRunner"),
                getAllTestNames(config.get("testDir"))
        );
        try {
            Process process = Runtime.getRuntime().exec(testCommand, null, new File(config.get("projectDir")));
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed calculating coverage while running the tests");
        }

        analyzeCoverage("jacoco.exec", classesDir);
    }

    /**
     * Get all the coverage information using the execution file and the class files.
     *
     * @param execFile   The execution file from running all the tests.
     * @param classesDir Directory containing all the class files of the source.
     * @throws IOException If the execution file cannot be found.
     */
    private void analyzeCoverage(String execFile, String classesDir) throws IOException {
        ExecFileLoader execFileLoader = new ExecFileLoader();
        execFileLoader.load(new File(execFile));

        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(execFileLoader.getExecutionDataStore(), coverageBuilder);

        analyzer.analyzeAll(new File(classesDir));

        for (IClassCoverage classCoverage : coverageBuilder.getClasses()) {
            System.out.println(classCoverage.getName());
            System.out.println(classCoverage.getInstructionCounter().getCoveredCount());
            System.out.println(classCoverage.getInstructionCounter().getTotalCount());
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
                final String classToDebug = String.format(
                        "%s.%s",
                        compilationUnit.getPackage().getName(),
                        FilenameUtils.removeExtension(file.getName())
                );
                stringBuilder.append(" ");
                stringBuilder.append(classToDebug);

            } catch (ParseException | IOException e) {
                // Should not be possible
            }
        }

        return stringBuilder.toString();
    }

}