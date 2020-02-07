package core.rankers.impact;

import core.TestEnvironment;
import lumutator.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for {@link CoverageRunner}.
 */
public class CoverageRunnerTest extends TestEnvironment {

    /**
     * The coverage runner to be tested.
     */
    private CoverageRunner coverageRunner;

    /**
     * Set up the coverage runner.
     */
    @Before
    public void setUp() throws IOException {
        Configuration config = Configuration.getInstance();
        coverageRunner = new CoverageRunner(config.get("classFiles"));
    }

    /**
     * Test the {@link CoverageRunner#getAllTestNames(String)} method.
     */
    @Test
    public void testGetAllTestNames() {
        try {
            Configuration config = Configuration.getInstance();
            Method method = coverageRunner.getClass().getDeclaredMethod("getAllTestNames", String.class);
            method.setAccessible(true);
            String allTestNames = (String) method.invoke(coverageRunner, config.get("testDir"));
            assertTrue(allTestNames.contains("bank.BankTest"));
            assertTrue(allTestNames.contains("bank.CustomerTest"));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Should not happen
            fail();
        }
    }

    /**
     * Test running the coverage.
     */
    @Test
    public void testRunCoverage() {
        try {
            Map<String, Integer> coverage = coverageRunner.runCoverage();
            assertEquals(65, (int) coverage.get("bank/Customer"));
            assertEquals(123, (int) coverage.get("bank/Bank"));
        } catch (IOException e) {
            // Should not happen
            fail();
        }
    }
}