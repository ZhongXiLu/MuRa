package core;

import lumutator.Configuration;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;

import static org.junit.Assert.fail;

/**
 * Contains a list of mutants from a small banking application.
 */
public class TestEnvironment {

    /**
     * Compile and test the banking application.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        try {
            Configuration.getInstance().initialize(TestEnvironment.class.getClassLoader().getResource("bank_config.xml").getFile());
            Configuration config = Configuration.getInstance();

            // Small hack to get junit jar added to classpath
            String[] classpathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
            for (String classpathEntry : classpathEntries) {
                if (classpathEntry.contains("junit")) {
                    config.set("classPath", config.get("classPath") + ":" + classpathEntry);
                }
            }

            Process process = Runtime.getRuntime().exec("mvn test", null, new File(config.get("projectDir")));
            process.waitFor();
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Clean up the working directory (bank application).
     */
    @AfterClass
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
