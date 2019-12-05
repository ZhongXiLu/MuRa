package core.rankers.usage;

import core.TestEnvironment;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link UsageCalculator}.
 */
public class UsageCalculatorTest extends TestEnvironment {

    /**
     * Test the usage count for all the methods in the bank application.
     */
    @Test
    public void getUsageCount() {
        final String classesDir = getClass().getClassLoader().getResource("bank/target/classes").getFile();
        UsageCalculator usageCalculator = new UsageCalculator(classesDir);

        assertEquals(-1, usageCalculator.getUsageCount("bank.Bank.<init>"));
        assertEquals(3, usageCalculator.getUsageCount("bank.Bank.accountNrExists"));
        assertEquals(-1, usageCalculator.getUsageCount("bank.Bank.addCustomer"));
        assertEquals(-1, usageCalculator.getUsageCount("bank.Bank.getLastAddedCustomer"));
        assertEquals(2, usageCalculator.getUsageCount("bank.Bank.getCustomerId"));
        assertEquals(-1, usageCalculator.getUsageCount("bank.Bank.internalTransfer"));

        assertEquals(-1, usageCalculator.getUsageCount("bank.Customer.<init>"));
        assertEquals(-1, usageCalculator.getUsageCount("bank.Customer.getName"));
        assertEquals(3, usageCalculator.getUsageCount("bank.Customer.getAccountNumber"));
        assertEquals(3, usageCalculator.getUsageCount("bank.Customer.getBalance"));
        assertEquals(2, usageCalculator.getUsageCount("bank.Customer.setBalance"));
    }
}