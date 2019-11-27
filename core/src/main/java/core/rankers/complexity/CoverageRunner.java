package core.rankers.complexity;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionDataStore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Analyzes a given class file and computes its complexity.
 * Adapted from: https://github.com/jacoco/jacoco/blob/master/org.jacoco.examples/src/org/jacoco/examples/ClassInfo.java.
 */
public final class CoverageRunner implements ICoverageVisitor {

    /**
     * The JaCoCo Analyzer.
     */
    private final Analyzer analyzer;

    /**
     * Map that stores the complexities of each method.
     */
    private HashMap<String, Integer> complexities;

    /**
     * Constructor: initialize the analyzer.
     */
    public CoverageRunner() {
        analyzer = new Analyzer(new ExecutionDataStore(), this);
        complexities = new HashMap<>();
    }

    /**
     * Run coverage on a list of class files.
     *
     * @param classFiles List of all the class files.
     * @throws IOException If a class file does not exist.
     */
    public void runCoverage(final List<File> classFiles) throws IOException {
        for (final File file : classFiles) {
            analyzer.analyzeAll(file);
        }
    }

    /**
     * Analyzed class coverage data is emitted to this method.
     * (https://www.jacoco.org/jacoco/trunk/doc/api/org/jacoco/core/analysis/ICoverageVisitor.html#visitCoverage)
     *
     * @param coverage The coverage data of a class.
     */
    public void visitCoverage(final IClassCoverage coverage) {
        final String className = coverage.getName().replace('/', '.');
        for (IMethodCoverage methodCoverage : coverage.getMethods()) {
            final String methodName = className + "." + methodCoverage.getName();
            final int cc = methodCoverage.getComplexityCounter().getTotalCount();
            complexities.put(methodName, cc);
        }
    }

    /**
     * Get the complexity of a certain method.
     *
     * @param method Name of the method including its package name.
     * @return The cyclomatic complexity of this method, -1 if method cannot be found.
     */
    public int getComplexity(String method) {
        return complexities.getOrDefault(method, -1);
    }

}