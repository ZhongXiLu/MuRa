package core.rankers.usage;

import core.TestEnvironment;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
        List<File> files = (List<File>) FileUtils.listFiles(new File(classesDir), new String[]{"class"}, true);
        UsageCalculator usageCalculator = new UsageCalculator(files);

        assertEquals(0, usageCalculator.getUsageCount("bank.Bank.<init>()V"));
        assertEquals(3, usageCalculator.getUsageCount("bank.Bank.accountNrExists(Ljava/lang/String;)Z"));
        assertEquals(0, usageCalculator.getUsageCount("bank.Bank.addCustomer(Lbank/Customer;)V"));
        assertEquals(0, usageCalculator.getUsageCount("bank.Bank.getLastAddedCustomer()Lbank/Customer;"));
        assertEquals(2, usageCalculator.getUsageCount("bank.Bank.getCustomerId(Ljava/lang/String;)I"));
        assertEquals(0, usageCalculator.getUsageCount("bank.Bank.internalTransfer(Ljava/lang/String;Ljava/lang/String;I)Z"));

        assertEquals(0, usageCalculator.getUsageCount("bank.Customer.<init>(Ljava/lang/String;Ljava/lang/String;I)V"));
        assertEquals(0, usageCalculator.getUsageCount("bank.Customer.getName()Ljava/lang/String;"));
        assertEquals(3, usageCalculator.getUsageCount("bank.Customer.getAccountNumber()Ljava/lang/String;"));
        assertEquals(3, usageCalculator.getUsageCount("bank.Customer.getBalance()I"));
        assertEquals(2, usageCalculator.getUsageCount("bank.Customer.setBalance(I)V"));
    }
}