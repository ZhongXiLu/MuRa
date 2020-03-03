package core.rankers.history;

/**
 * Data class that stores the changes count cost.
 */
public class ChangesCountCost {

    /**
     * Number of changes to a line.
     */
    public int changes;

    /**
     * How recent the commit is.
     */
    public int recent;

    /**
     * The final cost.
     */
    public double cost;

    /**
     * Constructor.
     *
     * @param changes Number of changes to a line.
     * @param recent  How recent the commit is.
     * @param cost    The final cost.
     */
    public ChangesCountCost(int changes, int recent, double cost) {
        this.changes = changes;
        this.recent = recent;
        this.cost = cost;
    }

}
