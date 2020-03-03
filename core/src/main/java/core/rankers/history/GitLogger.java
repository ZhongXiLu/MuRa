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
        final String logCommand = String.format("git log --follow -L %s,+1:%s -- %2$s", lineNr, file);
        int count = -1; // Don't count the commit that added the line

        try {
            Process process = Runtime.getRuntime().exec(logCommand, null, new File(config.get("projectDir")));
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

        // TODO: temporary fix: sometimes the log command does not return anything...
        if (count == -1) {
            count = 0;
        }

        return count;
    }

}
