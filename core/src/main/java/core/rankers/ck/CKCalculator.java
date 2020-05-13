package core.rankers.ck;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKClassResult;

import java.util.HashMap;

/**
 * Implements the Chidamber & Kemerer metrics suite.
 */
public class CKCalculator {

    /**
     * Coupling between objects.
     */
    private HashMap<String, Integer> cbo;

    /**
     * Depth inheritance tree.
     */
    private HashMap<String, Integer> dit;

    /**
     * Weight Method Class.
     */
    private HashMap<String, Integer> wcm;

    /**
     * Response for a Class.
     */
    private HashMap<String, Integer> rfc;

    /**
     * Constructor.
     */
    public CKCalculator() {
        cbo = new HashMap<>();
        dit = new HashMap<>();
        wcm = new HashMap<>();
        rfc = new HashMap<>();
    }

    /**
     * Store the results of CK.
     *
     * @param result The results of CK.
     */
    public void storeResults(CKClassResult result) {
        final String className = result.getClassName().replace("Anonymous", "");
        cbo.put(className, result.getCbo());
        dit.put(className, result.getDit());
        wcm.put(className, result.getNumberOfMethods());
        rfc.put(className, result.getRfc());
    }

    /**
     * Calculate the CK metrics on a set of files.
     *
     * @param sourcePath Path to the directory with all the source files.
     */
    public void calculateCKMetrics(final String sourcePath) {
        new CK(false, 0, false).calculate(sourcePath, result -> {
            storeResults(result);
        });
    }

    public int getCbo(String className) {
        return cbo.getOrDefault(className, 0);
    }

    public int getDit(String className) {
        return dit.getOrDefault(className, 0);
    }

    public int getWcm(String className) {
        return wcm.getOrDefault(className, 0);
    }

    public int getRfc(String className) {
        return rfc.getOrDefault(className, 0);
    }

}
