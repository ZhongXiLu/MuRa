package core.rankers.history;

import org.eclipse.jgit.diff.Edit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ChangesHistory}.
 */
public class ChangesHistoryTest {

    /**
     * The {@link ChangesHistory} to be tested.
     */
    private ChangesHistory history;

    /**
     * Set up a {@link ChangesHistory}.
     */
    @Before
    public void setUp() {
        history = new ChangesHistory();
    }

    /**
     * Test the {@link ChangesHistory#incrementCount(String, int, int)} method.
     */
    @Test
    public void testIncrementCount() {
        // Modify a line 10 times
        for (int i = 0; i <= 10; i++) {
            history.incrementCount("fileA", 10, 1);
            assertEquals(i + 1, history.getChangesCount("fileA", 11));
        }
        // Make sure lines around are not changed
        assertEquals(0, history.getChangesCount("fileA", 10));
        assertEquals(0, history.getChangesCount("fileA", 12));

        // Modify multiple lines at once
        history.incrementCount("fileB", 100, 20);
        for (int i = 0; i < 20; i++) {
            assertEquals(1, history.getChangesCount("fileB", 101 + i));
        }
    }

    /**
     * Test insertions for the {@link ChangesHistory#shiftLineNumbers(String, Edit)} method, i.e. positive shifts.
     */
    @Test
    public void testInsertions() {
        history.incrementCount("fileA", 100, 1);

        history.shiftLineNumbers("fileA", new Edit(10, 10, 10, 11)); // add ONE new line on line 11
        assertEquals(0, history.getChangesCount("fileA", 101));
        assertEquals(1, history.getChangesCount("fileA", 102));
        history.shiftLineNumbers("fileA", new Edit(20, 20, 20, 25)); // add 5 news line on line 21
        assertEquals(1, history.getChangesCount("fileA", 107));
        history.shiftLineNumbers("fileA", new Edit(200, 200, 200, 201)); // add a line AFTER line 107
        assertEquals(1, history.getChangesCount("fileA", 107));
        history.shiftLineNumbers("fileA", new Edit(107, 107, 106, 107)); // add one ON line 107
        assertEquals(1, history.getChangesCount("fileA", 108));
    }

    /**
     * Test deletions for the {@link ChangesHistory#shiftLineNumbers(String, Edit)} method, i.e. negative shifts.
     */
    @Test
    public void testDeletions() {
        history.incrementCount("fileA", 100, 1);

        history.shiftLineNumbers("fileA", new Edit(9, 10, 10, 10)); // delete ONE new line on line 10
        assertEquals(0, history.getChangesCount("fileA", 101));
        assertEquals(1, history.getChangesCount("fileA", 100));
        history.shiftLineNumbers("fileA", new Edit(20, 25, 25, 25)); // delete 5 news line on line 21
        assertEquals(1, history.getChangesCount("fileA", 95));
        history.shiftLineNumbers("fileA", new Edit(200, 201, 201, 201)); // delete a line AFTER line 95
        assertEquals(1, history.getChangesCount("fileA", 95));
        history.shiftLineNumbers("fileA", new Edit(94, 95, 95, 95)); // delete line 95
        assertEquals(0, history.getChangesCount("fileA", 95));
    }

    /**
     * Test the {@link ChangesHistory#renameFile(String, String)} method.
     */
    @Test
    public void testRenameFile() {
        history.incrementCount("fileA", 10, 1);
        assertEquals(1, history.getChangesCount("fileA", 11));
        history.renameFile("fileA", "fileB");
        assertEquals(1, history.getChangesCount("fileB", 11));
        assertEquals(-1, history.getChangesCount("fileA", 11));
    }

    /**
     * Test multiple edits.
     */
    @Test
    public void testMultipleEdits() {
        history.incrementCount("fileA", 1, 1);
        history.incrementCount("fileA", 10, 1);
        history.incrementCount("fileA", 100, 1);
        history.incrementCount("fileB", 1000, 1);

        history.renameFile("fileB", "fileC");
        history.shiftLineNumbers("fileA", new Edit(20, 25, 25, 25));
        history.shiftLineNumbers("fileA", new Edit(5, 5, 5, 6));
        history.shiftLineNumbers("fileA", new Edit(500, 500, 500, 510));
        history.shiftLineNumbers("fileC", new Edit(20, 25, 25, 25));
        history.shiftLineNumbers("fileC", new Edit(500, 500, 500, 510));

        assertEquals(1, history.getChangesCount("fileA", 2));
        assertEquals(1, history.getChangesCount("fileA", 12));
        assertEquals(1, history.getChangesCount("fileA", 97));
        assertEquals(1, history.getChangesCount("fileC", 1006));
    }
}