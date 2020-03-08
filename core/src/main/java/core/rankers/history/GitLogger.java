package core.rankers.history;

import lumutator.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Wrapper for the `git log` command.
 */
public class GitLogger {

    /**
     * Map that store how recent a commit is.
     * Commit hash -> n-th most recent commit.
     */
    private HashMap<String, Integer> commitHistory;

    /**
     * Constructor.
     */
    public GitLogger() throws IOException {
        // Populate the `commitHistory` map, i.e. mark how recent a commit is.

        Configuration config = Configuration.getInstance();
        commitHistory = new HashMap<>();
        try {
            Process process = Runtime.getRuntime().exec("git log --pretty=format:%H", null, new File(config.get("projectDir")));
            process.waitFor();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            int recent = 0;   // how recent the commit is (0 = most recent)

            // Iterate through every commit, starting with the most recent one
            while ((line = stdInput.readLine()) != null) {
                commitHistory.put(line, recent);
                recent++;   // go to next commit
            }

        } catch (InterruptedException e) {
            throw new RuntimeException("Failed running the `git log` command");
        }
    }

    /**
     * Get the changes count cost; this cost is dependant on two factors:
     * - How many times this line has been modified in the commit history.
     * - How recent the last commit was that modified this line.
     * <p>
     * To be precise:
     * - #changes = number of modifications to the line
     * - #commits = number of commits in the history
     * - nth_recent_commit = how recent the commit is (0 = most recent commit)
     * => COST = #changes * ((#commits - nth_recent_commit) / #commits)
     *
     * @param file   Name of the file.
     * @param lineNr The number of the line in the file.
     * @return The number of times a line has been modified in the commit history.
     */
    public ChangesCountCost getChangesCountCost(String file, int lineNr) throws IOException {
        Configuration config = Configuration.getInstance();

        // Execute the `git log -L` command, which traces the evolution of a line
        final String logCommand = "git log --follow -L " + lineNr + ",+1:" + file + " -- " + file;
        int changes = -1;     // don't count the commit that added the line
        int recent = -1;    // how recent the commit is (0 = most recent)

        try {
            // TODO: use ProcessBuilder instead (see Study)
            Process process = Runtime.getRuntime().exec(logCommand, null, new File(config.get("projectDir")));
            process.waitFor();

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = stdInput.readLine()) != null) {
                if (line.startsWith("commit")) {
                    if (recent == -1) { // most recent commit is always shown first
                        recent = commitHistory.get(line.substring(7));
                    }
                    changes++;
                }
            }

            // TODO: temporary fix: sometimes the log command does not return anything...
            if (changes <= 0) {
                return new ChangesCountCost(0, recent, 0.0);
            } else {
                // COST = #changes * ((#commits - nth_recent_commit) / #commits)
                final double cost = changes * ((double) (commitHistory.size() - recent) / commitHistory.size());
                return new ChangesCountCost(changes, recent, cost);
            }

        } catch (InterruptedException e) {
            throw new RuntimeException("Failed running the `git log` command");
        }
    }

}
