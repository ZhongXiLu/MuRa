package core.rankers.complexity;

import core.TestEnvironment;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for {@link CoverageRunner}.
 */
public class CoverageRunnerTest extends TestEnvironment {

    /**
     * Test calculating the cyclomatic complexity.
     */
    @Test
    public void testCalculatingComplexity() {
        CoverageRunner coverageRunner = new CoverageRunner();
        assertEquals(-1, coverageRunner.getComplexity("bank.Customer.getName"));

        File classesDir = new File(getClass().getClassLoader().getResource("bank/target/classes").getFile());
        List<File> files = (List<File>) FileUtils.listFiles(classesDir, new String[]{"class"}, true);
        try {
            coverageRunner.runCoverage(files);
        } catch (IOException e) {
            // Should not be possible
            fail();
        }

        assertEquals(1, coverageRunner.getComplexity("bank.Customer.getName"));
        assertEquals(7, coverageRunner.getComplexity("bank.Customer.<init>"));
        assertEquals(2, coverageRunner.getComplexity("bank.Customer.setBalance"));
    }
}