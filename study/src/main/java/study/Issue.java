package study;

import java.util.HashMap;
import java.util.Set;

/**
 * Represents a GitHub issue.
 */
public class Issue {

    /**
     * The id of the GitHub issue.
     */
    public int id;

    /**
     * The commit hash of the version before the fix was applied.
     */
    public String commitBeforeFix;

    /**
     * Map containing the lines that were fixed.
     * Filename -> set of line numbers.
     */
    public HashMap<String, Set<Integer>> fixedLines;

    /**
     * Constructor.
     *
     * @param id The id of the GitHub issue.
     */
    public Issue(int id) {
        this.id = id;
        this.fixedLines = new HashMap<>();
    }

}
