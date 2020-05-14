package core.rankers.ck;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;

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
    private HashMap<String, Integer> wmc;

    /**
     * Response for a Class.
     */
    private HashMap<String, Integer> rfc;

    /**
     * Number of Children.
     */
    private HashMap<String, Integer> noc;

    /**
     * Constructor.
     */
    public CKCalculator() {
        cbo = new HashMap<>();
        dit = new HashMap<>();
        wmc = new HashMap<>();
        rfc = new HashMap<>();
        noc = new HashMap<>();
    }

    /**
     * Calculate the CK metrics on a set of files.
     *
     * @param sourcePath Path to the directory with all the source files.
     */
    public void calculateCKMetrics(final String sourcePath) {
        CKReport report = new CK().calculate(sourcePath);

        for (CKNumber result : report.all()) {
            if (result.isError()) continue;
            final String className = result.getClassName().replace("Anonymous", "");
            cbo.put(className, result.getCbo());
            dit.put(className, result.getDit());
            wmc.put(className, result.getWmc());
            rfc.put(className, result.getRfc());
            noc.put(className, result.getNoc());
        }
    }

    public int getCbo(String className) {
        return cbo.getOrDefault(className, 0);
    }

    public int getDit(String className) {
        return dit.getOrDefault(className, 0);
    }

    public int getWmc(String className) {
        return wmc.getOrDefault(className, 0);
    }

    public int getRfc(String className) {
        return rfc.getOrDefault(className, 0);
    }

    public int getNoc(String className) {
        return noc.getOrDefault(className, 0);
    }

}
