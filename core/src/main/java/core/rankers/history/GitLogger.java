package core.rankers.history;

import lumutator.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Wrapper for the `git log` command.
 */
public class GitLogger {

    /**
     * Get the number of times a line has been modified in the commit history.
     *
     * @param file   Name of the file.
     * @param lineNr The number of the line in the file.
     * @return The number of times a line has been modified in the commit history.
     */
    public static int getChangesCount(String file, int lineNr) throws IOException {
        Configuration config = Configuration.getInstance();

        // Execute the `git log -L` command, which traces the evolution of a line
        final String testCommand = String.format("git log --follow %s -L %s,+1:%1$s", file, lineNr);
        int count = -1; // Don't count the commit that added the line

        try {
            Process process = Runtime.getRuntime().exec(testCommand, null, new File(config.get("projectDir")));
            process.waitFor();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = stdInput.readLine()) != null) {
                if (line.startsWith("commit")) {
                    count++;
                }
            }

        } catch (InterruptedException e) {
            throw new RuntimeException("Failed running the `git log` command");
        }

        return count;
    }

}
