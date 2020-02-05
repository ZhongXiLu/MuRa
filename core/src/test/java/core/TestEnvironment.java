package core;

import lumutator.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Contains a list of mutants from a small banking application.
 */
public class TestEnvironment {

    /**
     * Compile and test the banking application.
     */
    @BeforeAll
    public static void setUpBeforeClass() {
        try {
            Configuration.getInstance().initialize(TestEnvironment.class.getClassLoader().getResource("bank_config.xml").getFile());
            Configuration config = Configuration.getInstance();

            Process process = Runtime.getRuntime().exec("mvn test", null, new File(config.get("projectDir")));
            process.waitFor();
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Clean up the working directory (bank application).
     */
    @AfterAll
    public static void tearDownAfterClass() {
        try {
            // Clean up
            Process process2 = Runtime.getRuntime().exec("mvn clean", null, new File(Configuration.getInstance().get("projectDir")));
            process2.waitFor();
        } catch (Exception e) {
            fail();
        }
    }
}
