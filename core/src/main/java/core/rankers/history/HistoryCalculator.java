package core.rankers.history;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Calculates the number of times a line has been changed in the past history.
 */
public class HistoryCalculator {

    /**
     * The git repository.
     */
    private Repository repo;

    /**
     * Constructor.
     *
     * @param gitDirectory The .git/ directory.
     */
    public HistoryCalculator(File gitDirectory) throws IOException {
        repo = new FileRepositoryBuilder().setGitDir(gitDirectory).build();
    }

    public ChangesHistory calculateChangedHistory() throws IOException {
        Ref head = repo.exactRef("refs/heads/master");  // only consider commits on master branch

        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(repo);
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
        diffFormatter.setDetectRenames(true);

        ChangesHistory history = new ChangesHistory();

        try (RevWalk walk = new RevWalk(repo)) {
            RevCommit prevCommit = null;
            walk.markStart(walk.parseCommit(head.getObjectId()));
            walk.sort(RevSort.REVERSE); // chronological order (from oldest commit to most recent commit)

            for (RevCommit commit : walk) {
                if (prevCommit != null) {
                    for (DiffEntry diff : diffFormatter.scan(prevCommit.getTree(), commit.getTree())) {
                        List<Edit> shifts = new ArrayList<>();
                        List<Edit> replaces = new ArrayList<>();

                        if (diff.getChangeType() == DiffEntry.ChangeType.RENAME) {
                            // Directly rename file
                            history.renameFile(diff.getOldPath(), diff.getNewPath());
                        }
                        for (Edit edit : diffFormatter.toFileHeader(diff).toEditList()) {
                            // Only consider the "REPLACE" edits
                            if (edit.getType() == Edit.Type.REPLACE) {
                                // One or more lines got modified => increment counter
                                replaces.add(edit);
                            } else if (edit.getType() == Edit.Type.INSERT || edit.getType() == Edit.Type.DELETE) {
                                // Shift line numbers if an edit occurred before a previously recorded line number
                                shifts.add(edit);
                            }
                        }

                        // First shift previous line numbers
                        for (Edit edit : shifts) {
                            history.shiftLineNumbers(diff.getNewPath(), edit);
                        }
                        // Then add new modification (i.e. increment counter)
                        for (Edit edit : replaces) {
                            history.incrementCount(diff.getNewPath(), edit.getBeginB(), edit.getLengthB());
                        }
                    }
                }
                prevCommit = commit;
            }
            walk.dispose();
        }

        return history;
    }

}
