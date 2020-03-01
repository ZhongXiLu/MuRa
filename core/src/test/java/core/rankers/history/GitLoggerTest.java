package core.rankers.history;

import core.TestEnvironment;
import lumutator.Configuration;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link GitLogger}.
 */
public class GitLoggerTest {

    /**
     * Make sure the .git directory is present and also initialize the configuration.
     */
    @BeforeClass
    public static void beforeClass() throws IOException {
        final File oldGitDir = new File(GitLoggerTest.class.getClassLoader().getResource("bank/.gitted/").getFile());
        final File newGitDir = new File(GitLoggerTest.class.getClassLoader().getResource("bank/").getFile() + ".git/");
        FileUtils.moveDirectory(oldGitDir, newGitDir);

        Configuration.getInstance().initialize(TestEnvironment.class.getClassLoader().getResource("bank_config.xml").getFile());
    }

    /**
     * Make sure the .git directory is removed again.
     */
    @AfterClass
    public static void afterClass() throws IOException {
        final File oldGitDir = new File(GitLoggerTest.class.getClassLoader().getResource("bank/.git/").getFile());
        final File newGitDir = new File(GitLoggerTest.class.getClassLoader().getResource("bank/").getFile() + ".gitted/");
        FileUtils.moveDirectory(oldGitDir, newGitDir);
    }

    /**
     * Test the {@link GitLogger#getChangesCount(String, int)} method.
     */
    @Test
    public void getChangesCount() {
        try {
            assertEquals(0, GitLogger.getChangesCount("src/main/java/bank/Customer.java", 1));
            assertEquals(0, GitLogger.getChangesCount("src/main/java/bank/Customer.java", 58));
            assertEquals(1, GitLogger.getChangesCount("src/main/java/bank/Customer.java", 67));
            assertEquals(2, GitLogger.getChangesCount("src/main/java/bank/Customer.java", 32));
            assertEquals(-1, GitLogger.getChangesCount("src/main/java/bank/Customer.java", 999));

        } catch (IOException e) {
            // Should not happen
            fail();
        }
    }

}