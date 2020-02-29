package core.rankers.history;

import org.eclipse.jgit.diff.Edit;

import java.util.HashMap;

/**
 * Class that encapsulates the changes history map.
 */
public class ChangesHistory {

    /**
     * The changes history map.
     */
    private HashMap<String, HashMap<Integer, Integer>> changesHistory;

    /**
     * Constructor.
     */
    public ChangesHistory() {
        /*
        - changesHistory = <fileName, linesMap>
            - linesMap = <lineNr, modifiedCount>
         */
        changesHistory = new HashMap<>();
    }

    /**
     * Get the number of times a line has been changed in the past.
     *
     * @param file       The file.
     * @param lineNumber The line number.
     * @return The number of times a line has been changed in the past.
     */
    public int getChangesCount(String file, int lineNumber) {
        if (changesHistory.containsKey(file)) {
            return changesHistory.get(file).getOrDefault(lineNumber, 0);
        }
        return 0;
    }

    /**
     * Add all the modified lines to the changes history.
     * Important: this method does not shift other lines in case this edit adds/removes lines.
     *
     * @param file       The file which was modified.
     * @param editLine   The start line number of the modification.
     * @param editLength The length in lines of the modification.
     */
    public void incrementCount(String file, int editLine, int editLength) {
        changesHistory.putIfAbsent(file, new HashMap<>());

        HashMap<Integer, Integer> linesMap = changesHistory.get(file);
        for (int l = editLine + 1; l <= editLine + editLength; l++) {   // loop over all modified lines
            if (!linesMap.containsKey(l)) {
                linesMap.put(l, 1);     // first time modified
            } else {
                linesMap.put(l, linesMap.get(l) + 1);   // increment counter
            }
        }
    }

    /**
     * Shift the inserted line numbers in case there was an addition or deleting before that line.
     *
     * @param file The file which was modified.
     */
    public void shiftLineNumbers(String file, Edit edit) {
        if (changesHistory.containsKey(file)) {
            HashMap<Integer, Integer> linesMap = changesHistory.get(file);
            HashMap<Integer, Integer> offsetMap = new HashMap<>();  // how much do we need to shift each line number

            // Find the total offset for each line
            for (Integer insertedLine : ((HashMap<Integer, Integer>) linesMap.clone()).keySet()) {
                if (edit.getType() == Edit.Type.INSERT) {
                    // Consider the new version (i.e. version B)
                    if (edit.getBeginB() + 1 <= insertedLine) {
                        // Edit was issued before/on inserted line => shift line number
                        offsetMap.put(insertedLine, offsetMap.getOrDefault(insertedLine, 0) + edit.getLengthB());
                    }
                } else if (edit.getType() == Edit.Type.DELETE) {
                    // Consider the old version (i.e. version A)
                    if (edit.getBeginA() <= insertedLine && edit.getBeginA() + edit.getLengthA() > insertedLine) {
                        // Line got deleted => also delete line in our history
                        linesMap.remove(insertedLine);
                    } else if (edit.getBeginA() < insertedLine) {
                        // Edit was issued before inserted line => shift line number
                        offsetMap.put(insertedLine, offsetMap.getOrDefault(insertedLine, 0) - edit.getLengthA());
                    }
                } else if (edit.getType() == Edit.Type.REPLACE) {
                    if (edit.getBeginA() + edit.getLengthA() < insertedLine) {
                        // Edit was issued before inserted line => see if REPLACE added/deleted lines
                        // E.g.: REPLACE changed 1 line to 3 lines => shift lines after with 2
                        final int diff = edit.getLengthB() - edit.getLengthA();
                        offsetMap.put(insertedLine, offsetMap.getOrDefault(insertedLine, 0) + diff);
                    }
                }
            }

            // Apply the offset for each line
            HashMap<Integer, Integer> newLinesMap = new HashMap<>();

            for (Integer insertedLine : linesMap.keySet()) {    // iterate over original map!
                final int offset = offsetMap.getOrDefault(insertedLine, 0);
                newLinesMap.put(insertedLine + offset, linesMap.get(insertedLine));
            }

            changesHistory.put(file, newLinesMap);
        }
    }

    /**
     * Rename a file to a new one while still preserving all the history.
     *
     * @param oldFile Name of the old file.
     * @param newFile Name of the new file.
     */
    public void renameFile(String oldFile, String newFile) {
        if (changesHistory.containsKey(oldFile)) {
            HashMap<Integer, Integer> linesMap = changesHistory.get(oldFile);
            changesHistory.remove(oldFile);
            changesHistory.put(newFile, linesMap);
        }
    }

}
