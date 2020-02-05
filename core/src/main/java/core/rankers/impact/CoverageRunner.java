package core.rankers.impact;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;

/**
 * Calculate the coverage on a set of files from a project.
 * Adapted from: https://github.com/jacoco/jacoco/blob/master/org.jacoco.examples/src/org/jacoco/examples/CoreTutorial.java.
 */
public final class CoverageRunner {

    public CoverageRunner() {
    }

    public void runCoverage(final List<File> sourceClassFiles, final String testClassFilesDir) throws IOException {
        final IRuntime runtime = new LoggerRuntime();
        final RuntimeData data = new RuntimeData();
        try {
            runtime.startup(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Instrumenter instrumenter = new Instrumenter(runtime);
        OutputStream outputStream = new ByteArrayOutputStream();

        // Instrument all the source class files
        for (final File sourceFile : sourceClassFiles) {
            InputStream inputStream = new FileInputStream(sourceFile);
            instrumenter.instrument(inputStream, outputStream, sourceFile.getName());
        }

        // Load test class files of other project
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        URL[] urls = new URL[]{new File(testClassFilesDir).toURI().toURL()};
        ClassLoader newClassLoader = new URLClassLoader(urls, originalClassLoader);
        Thread.currentThread().setContextClassLoader(newClassLoader);

        // Run JUnit Tests
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClasspathRoots(Collections.singleton(Paths.get(testClassFilesDir))))
                .filters(includeClassNamePatterns(".*"))
                .build();

        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        summary.printTo(new PrintWriter(System.out));
        summary.printFailuresTo(new PrintWriter(System.out));

        // Restore original classloader
        Thread.currentThread().setContextClassLoader(originalClassLoader);

        // At the end of test execution we collect execution data and shutdown the runtime:
        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        data.collect(executionData, sessionInfos, false);
        runtime.shutdown();

        // Together with the original class definition we can calculate coverage information:
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        for (final File sourceFile : sourceClassFiles) {
            analyzer.analyzeAll(sourceFile);
        }
        // TODO: coverageBuilder.getClasses();
    }

}
