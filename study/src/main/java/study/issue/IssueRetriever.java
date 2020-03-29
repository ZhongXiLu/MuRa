package study.issue;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Interface that retrieves issues on GitHub.
 */
public class IssueRetriever {

    /**
     * Base url for the api of GitHub.
     */
    private final static String baseUrl = "https://api.github.com";

    /**
     * Get all the closed bug reports of a repository.
     * A bug report is an issue that is closed in a commit or pull request and labeled as a 'bug'.
     *
     * @param gitDirectory The .git/ directory.
     * @param repoOwner    The name of the owner of the repository.
     * @param repoName     The name of the repository.
     * @param bugLabel     The label on GitHub for an issue that identifies a bug.
     */
    public static List<Issue> getAllClosedBugReports(File gitDirectory, String repoOwner, String repoName, String bugLabel) throws IOException {
        final Repository repo = new FileRepositoryBuilder().setGitDir(gitDirectory).build();
        RevWalk walk = new RevWalk(repo);
        DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
        diffFormatter.setRepository(repo);
        diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
        diffFormatter.setDetectRenames(true);

        List<Issue> closedBugReports = new ArrayList<>();
        try {
            // (1) Retrieve all the closed issues
            // TODO: get ALL issues instead of `&per_page=100`?
            final URL queryUrl = new URL(
                    baseUrl + "/repos/" + repoOwner + "/" + repoName + "/issues?state=closed&labels=" + bugLabel + "&per_page=100"
            );
            String response = RequestSender.sendGetRequest(queryUrl);
            JSONArray issues = new JSONArray(response);

            // (2) Filter out all the bug reports, i.e. issues that have been closed in a commit or pull request
            for (int i = 0; i < issues.length(); i++) {
                try {
                    JSONObject issueJSON = issues.getJSONObject(i);
                    Issue issue = new Issue(issueJSON.getInt("number"));

                    // Pull requests
                    if (issueJSON.has("pull_request")) {
                        final URL pullUrl = new URL(issueJSON.getJSONObject("pull_request").getString("url"));
                        response = RequestSender.sendGetRequest(pullUrl);
                        final JSONObject pull = new JSONObject(response);

                        if (pull.getBoolean("merged")) {    // merged into master branch
                            issue.commitBeforeFix = pull.getJSONObject("base").getString("sha");
                            issue.commitFix = pull.getString("merge_commit_sha");
                            issue.fixedLines = getChangedLines(repo, walk, diffFormatter, issue.commitBeforeFix, pull.getString("merge_commit_sha"));
                            closedBugReports.add(issue);
                            continue;
                        }
                    }

                    // Events: closed commits
                    response = RequestSender.sendGetRequest(new URL(issueJSON.getString("events_url")));
                    JSONArray events = new JSONArray(response);
                    for (int e = 0; e < events.length(); e++) {
                        JSONObject event = events.getJSONObject(e);
                        // Closed issue in commit (i.e. fixed)
                        if (event.getString("event").equals("closed") && !event.isNull("commit_id")) {
                            final URL commitUrl = new URL(event.getString("commit_url"));
                            response = RequestSender.sendGetRequest(commitUrl);
                            final JSONObject commit = new JSONObject(response);
                            final JSONArray parents = commit.getJSONArray("parents");
                            // Avoid multiple-way merge commits => more complex and takes more time...
                            if (parents.length() == 1) {
                                issue.commitBeforeFix = parents.getJSONObject(0).getString("sha");
                                issue.commitFix = commit.getString("sha");
                                issue.fixedLines = getChangedLines(repo, walk, diffFormatter, issue.commitBeforeFix, commit.getString("sha"));
                                closedBugReports.add(issue);
                                continue;
                            }
                        }
                    }

                } catch (MissingObjectException e) {
                    // It is possible for issues to reference commits from other branches and even repo's
                    continue;
                }
            }

            diffFormatter.close();
            walk.close();
            repo.close();

            return closedBugReports;

        } catch (IOException e) {
            throw new RuntimeException("Failed retrieving issues from GitHub: " + e.getMessage());
        }
    }

    /**
     * Get a map that contains information about all the lines that were modified in between two commits.
     *
     * @param repo           The repository.
     * @param walk           The RevWalk.
     * @param diffFormatter  The diff formatter.
     * @param prevCommitHash The hash of the old previous commit.
     * @param curCommitHash  The hash of the new commit.
     * @return Map that contains information about all the lines that were modified.
     */
    private static HashMap<String, Set<Integer>> getChangedLines(
            Repository repo, RevWalk walk, DiffFormatter diffFormatter, String prevCommitHash, String curCommitHash
    ) throws IOException {

        HashMap<String, Set<Integer>> changedLines = new HashMap<>();

        RevCommit curCommit = walk.parseCommit(repo.resolve(prevCommitHash));
        RevCommit prevCommit = walk.parseCommit(repo.resolve(curCommitHash));

        for (DiffEntry diff : diffFormatter.scan(prevCommit.getTree(), curCommit.getTree())) {
            Set<Integer> lines = new HashSet<>();

            for (Edit edit : diffFormatter.toFileHeader(diff).toEditList()) {
                // Only consider replaces
                if (edit.getType() == Edit.Type.REPLACE) {
                    for (int i = edit.getBeginA() + 1; i <= edit.getBeginA() + edit.getLengthA(); i++) {
                        lines.add(i);
                    }
                }
            }

            changedLines.put(diff.getOldPath(), lines);
        }

        return changedLines;
    }

}
