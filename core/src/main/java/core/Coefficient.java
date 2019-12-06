package core;

/**
 * Represents a coefficient for a {@link RankedMutant}.
 */
public class Coefficient {

    /**
     * Name of the ranking method.
     */
    private String ranker;

    /**
     * Value of the coefficient.
     */
    private Double value;

    /**
     * Explanation of the coefficient.
     */
    private String explanation;

    /**
     * Constructor.
     *
     * @param ranker       Name of the ranking method.
     * @param value        Value of the coefficient.
     */
    public Coefficient(String ranker, Double value) {
        this(ranker, value, "");
    }

    /**
     * Constructor.
     *
     * @param ranker       Name of the ranking method.
     * @param value        Value of the coefficient.
     * @param explanation  Some optional explanation.
     */
    public Coefficient(String ranker, Double value, String explanation) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("Coefficient value should be in [0,1]");
        }
        this.ranker = ranker;
        this.value = value;
        this.explanation = explanation;
    }

    /**
     * Get the name of the ranking method.
     *
     * @return Name of the ranking method.
     */
    public String getRanker() {
        return ranker;
    }

    /**
     * Get the value of the coefficient.
     *
     * @return Value of the coefficient.
     */
    public Double getValue() {
        return value;
    }

    /**
     * Get the explanation of the coefficient.
     *
     * @return Explanation of the coefficient.
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * Set a new value of the coefficient. Useful in case we want to normalize the values.
     *
     * @param value New value of the coefficient.
     */
    public void setValue(Double value) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException("Coefficient value should be in [0,1]");
        }
        this.value = value;
    }
}
