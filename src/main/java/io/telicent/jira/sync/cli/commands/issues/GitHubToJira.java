package io.telicent.jira.sync.cli.commands.issues;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.github.rvesse.airline.annotations.AirlineModule;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.NotBlank;
import com.github.rvesse.airline.annotations.restrictions.Required;
import io.atlassian.util.concurrent.Promise;
import io.telicent.jira.sync.cli.commands.JiraGitHubSyncCommand;
import io.telicent.jira.sync.cli.options.CrossLinkOptions;
import io.telicent.jira.sync.cli.options.JiraIssueTypeMappingOptions;
import io.telicent.jira.sync.client.AsynchronousIssueCommentsClient;
import io.telicent.jira.sync.client.AsynchronousRemoteLinksClient;
import io.telicent.jira.sync.client.EnhancedJiraRestClient;
import io.telicent.jira.sync.client.model.*;
import io.telicent.jira.sync.utils.GitHubUtils;
import io.telicent.jira.sync.utils.JiraUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.*;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHIssueStateReason;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.*;

@Command(name = "to-jira", description = "Command for synchronising GitHub Issues to JIRA")
public class GitHubToJira extends JiraGitHubSyncCommand {

    @Option(name = "--github-issue-id", title = "GitHubIssueID", description = "Specifies the ID of a single GitHub Issue that you wish to sync to JIRA.  If omitted all issues from the GitHub repository are sync'd to JIRA.")
    private int ghIssueId = 0;

    @Option(name = "--github-repository", title = "GitHubRepository", description = "Specifies the GitHub Repository whose issue you want to sync to JIRA")
    @Required
    private String ghRepo;

    @Option(name = "--jira-repository-field", title = "JiraCustomFieldId", description = "Specifies the ID of a custom JIRA field that can be used to store the name of the GitHub repository from which an issue was sync'd.")
    @NotBlank
    private String jiraRepositoryField;

    @AirlineModule
    private JiraIssueTypeMappingOptions jiraIssueTypeMappingOptions = new JiraIssueTypeMappingOptions();

    @AirlineModule
    private CrossLinkOptions crossLinkOptions = new CrossLinkOptions();

    @Option(name = "--include-comments", description = "When specified also sync's any GitHub issue comments across into JIRA issue comments.")
    private boolean includeComments = false;

    @Option(name = "--include-closed", description = "When specified also sync's any GitHub issues that are already closed on the GitHub side.  Note that the sync'd JIRA issue will not be closed so this should be used with caution.")
    private boolean includeClosed = false;

    @Option(name = "--close-after-sync", description = "When specified any GitHub issue that is sync'd will be closed on the GitHub side.")
    private boolean closeAfterSync = false;

    @Option(name = "--skip-existing", description = "When specified skips sync'ing issues that have already been sync'd to JIRA, this means any changes on the GitHub issue are not reflected in the JIRA but reduces the number of spurious JIRA updates.")
    private boolean skipExisting = false;

    @Option(name = "--dry-run", description = "When specified print what would happen without actually performing the actions i.e. preview what the results of running the command would be.")
    private boolean dryRun = false;

    @Option(name = "--extra-labels", description = "Specifies extra labels that are added to the JIRA issue in addition to the GitHub labels already present on the GitHub issues.")
    private List<String> extraLabels = new ArrayList<>();

    @Override
    public int run() {
        try {
            GitHub gitHub = this.gitHubOptions.connect();
            GHRepository repository = gitHub.getRepository(this.ghRepo);
            if (repository == null) {
                throw new RuntimeException(
                        "GitHub Repository " + this.ghRepo + " does not exist, or your provided GitHub Credentials do not permit access to it");
            }
            CrossLinks crossLinks = this.crossLinkOptions.loadCrossLinks();

            try (EnhancedJiraRestClient jiraRestClient = this.jiraOptions.connect()) {
                if (this.ghIssueId > 0) {
                    GHIssue issue = repository.getIssue(this.ghIssueId);
                    if (issue == null) {
                        throw new RuntimeException(
                                "GitHub Repository " + this.ghRepo + " does not contain Issue ID " + this.ghIssueId);
                    } else if (issue.isPullRequest()) {
                        System.out.println(
                                "GitHub Repository " + this.ghRepo + " Issue ID " + this.ghIssueId + " is a PR for which sync is not supported");
                    } else {
                        this.syncOneIssue(crossLinks, jiraRestClient, issue);
                    }
                } else {
                    System.out.println("Syncing GitHub Issues from repository " + this.ghRepo);
                    List<GHIssue> issues = new ArrayList<>(
                            repository.getIssues(this.includeClosed ? GHIssueState.ALL : GHIssueState.OPEN));
                    Collections.sort(issues, Comparator.comparingInt(GHIssue::getNumber));
                    for (GHIssue issue : issues) {
                        if (issue.isPullRequest()) {
                            System.out.println("Skipping PR #" + issue.getNumber());
                            continue;
                        }
                        System.out.println("Syncing Issue #" + issue.getNumber());
                        this.syncOneIssue(crossLinks, jiraRestClient, issue);
                    }
                }
                return 0;
            }
        } catch (RestClientException e) {
            this.reportJiraRestError(e);
            return 1;
        } catch (GHFileNotFoundException e) {
            throw new RuntimeException(
                    "GitHub Repository " + this.ghRepo + " does not exist, or your provided GitHub Credentials do not permit access to it");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void syncOneIssue(CrossLinks crossLinks, EnhancedJiraRestClient jiraRestClient, GHIssue issue) throws
            IOException {
        // Skip sync for closed issues unless --include-closed was set
        if (issue.getState() == GHIssueState.CLOSED && !this.includeClosed) {
            System.out.println("Skipping Issue #" + issue.getNumber() + " as it is already closed");
            return;
        }

        String gitHubIssueId = this.ghRepo + "/" + issue.getNumber();
        String jiraKey = crossLinks.getGitHubToJira().getLinks().get(gitHubIssueId);

        // Skip sync if it is already in JIRA and --skip-existing was set
        if (StringUtils.isNotBlank(jiraKey) && this.skipExisting) {
            System.out.println(
                    "Skipping Issue #" + issue.getNumber() + " which syncs to existing JIRA Issue " + jiraKey + " as --skip-existing was set");

            // Might be new comments even if the issue itself has been sync'd across
            this.syncIssueComments(crossLinks, jiraRestClient, issue, jiraKey);
            return;
        }

        // Prepare the JIRA Issue content
        IssueRestClient issues = jiraRestClient.getIssueClient();
        StringBuilder issuePreamble =
                GitHubUtils.buildPreamble(issue.getUser(), issue.getCreatedAt(), issue.getUpdatedAt(), "filed an issue",
                                          issue.getHtmlUrl().toString());
        IssueInputBuilder issueBuilder = new IssueInputBuilder().setProjectKey(this.jiraOptions.getProjectKey())
                                                                .setFieldInput(
                                                                        new FieldInput(IssueFieldId.DESCRIPTION_FIELD,
                                                                                       JiraUtils.translateMarkdownToAdf(
                                                                                               issuePreamble,
                                                                                               issue)))
                                                                .setSummary(issue.getTitle())
                                                                .setFieldInput(new FieldInput(IssueFieldId.LABELS_FIELD,
                                                                                              GitHubUtils.translateLabels(
                                                                                                      issue, this.extraLabels)));
        // Only set the Issue Type on new issues as previously sync'd issues may have had their issue types changed on
        // the JIRA side since we created them, and we don't want to overwrite that
        if (StringUtils.isBlank(jiraKey)) {
            long jiraIssueType = this.jiraIssueTypeMappingOptions.getJiraIssueType(issue);
            System.out.println("GitHub issue " + gitHubIssueId + " will be synced as JIRA Issue Type " + jiraIssueType);
            issueBuilder = issueBuilder.setIssueTypeId(jiraIssueType);
        }
        // TODO Copy assignee where relevant

        if (StringUtils.isNotBlank(this.jiraRepositoryField)) {
            issueBuilder = issueBuilder.setFieldInput(
                    new FieldInput(this.jiraRepositoryField, this.ghRepo));
        }


        IssueInput input = issueBuilder.build();
        if (StringUtils.isNotBlank(jiraKey)) {
            System.out.println("GitHub Issue " + gitHubIssueId + " syncs to existing JIRA Issue " + jiraKey);
            if (!this.dryRun) {
                Promise<Void> updated = issues.updateIssue(jiraKey, input);
                updated.claim();
                System.out.println("Updated JIRA Issue " + jiraKey);
            } else {
                System.out.println("[DRY RUN] Would have updated JIRA Issue " + jiraKey);
            }
        } else {
            // Need to create a new JIRA Issue
            if (!this.dryRun) {
                Promise<BasicIssue> creation = issues.createIssue(input);
                BasicIssue created = creation.claim();
                System.out.println("Created new JIRA Issue " + created.getKey());

                // Update the cross-links with the new links
                updateCrossLinks(crossLinks, gitHubIssueId, created.getKey());

                // Need the JIRA Key later for creating the remote link from JIRA back to the original GitHub Issue
                jiraKey = created.getKey();
            } else {
                System.out.println("[DRY RUN] Would have created a new JIRA Issue");
                jiraKey = this.jiraOptions.getProjectKey() + "-?";
            }

            // TODO If there's a JIRA Key mentioned in the issue automatically add issue links?
        }

        // Always create/update the remote link in case someone has modified it incorrectly
        AsynchronousRemoteLinksClient remoteLinksClient = jiraRestClient.getRemoteLinksClient();
        RemoteLinkInput remoteLink = RemoteLinkInput.builder()
                                                    .url(issue.getHtmlUrl().toString())
                                                    .title("GitHub Issue " + gitHubIssueId)
                                                    .globalId("github:" + gitHubIssueId)
                                                    .build();
        if (!this.dryRun) {
            Promise<BasicRemoteLink> createdLinkPromise =
                    remoteLinksClient.createOrUpdateRemoteLink(jiraKey, remoteLink);
            BasicRemoteLink createdLink = createdLinkPromise.claim();
            System.out.println("Associated GitHub Issue with JIRA Issue via Remote Link ID " + createdLink.getId());
        } else {
            System.out.println("[DRY RUN] Would have created/updated a Remote Link from JIRA to the GitHub Issue");
        }

        // TODO If the GH Issue is closed apply a suitable transition to the JIRA Issue if it isn't also closed?

        // Sync Issue Comments if enabled
        this.syncIssueComments(crossLinks, jiraRestClient, issue, jiraKey);

        // Close original GitHub issue if configured
        if (this.closeAfterSync && issue.getState() == GHIssueState.OPEN) {
            if (!this.dryRun) {
                GHIssueComment comment = issue.comment(
                        GitHubUtils.buildCloseComment(this.jiraOptions.getBaseUrl(), jiraKey));
                syncOneComment(crossLinks, issue, jiraKey, comment, jiraRestClient.getCommentsClient());
                issue.close(GHIssueStateReason.COMPLETED);
                System.out.println("Closed GitHub Issue #" + issue.getNumber());
            } else {
                System.out.println("[DRY RUN] Would have closed GitHub Issue #" + issue.getNumber());
            }
        }
    }

    private void updateCrossLinks(CrossLinks crossLinks, String gitHubId, String jiraId) throws IOException {
        // NB - We don't update the last sync'd ID as ComputeCrossLinks does because there might be issue creation
        //      happening on the JIRA side which we haven't sync'd the other way yet
        crossLinks.getGitHubToJira().setLinks(gitHubId, jiraId);
        crossLinks.getJiraToGitHub().setLinks(jiraId, gitHubId);
        this.crossLinkOptions.saveCrossLinks();
    }

    private void syncIssueComments(CrossLinks crossLinks, EnhancedJiraRestClient jira, GHIssue issue,
                                   String jiraIssueKey) throws IOException {
        if (!this.includeComments) {
            return;
        }

        AsynchronousIssueCommentsClient commentsClient = jira.getCommentsClient();

        System.out.println(
                "Syncing comments for GitHub Issue " + issue.getNumber() + " to JIRA Issue " + jiraIssueKey + "...");
        for (GHIssueComment ghComment : issue.listComments()) {
            syncOneComment(crossLinks, issue, jiraIssueKey, ghComment, commentsClient);
        }
        System.out.println(
                "All comments for GitHub Issue " + issue.getNumber() + " synced to JIRA Issue " + jiraIssueKey);
    }

    private void syncOneComment(CrossLinks crossLinks, GHIssue issue, String jiraIssueKey, GHIssueComment ghComment,
                                AsynchronousIssueCommentsClient commentsClient) throws IOException {
        String gitHubCommentId = this.ghRepo + "/" + issue.getNumber() + "/comments/" + ghComment.getId();
        String jiraCommentId = crossLinks.getGitHubToJira().getLinks().get(gitHubCommentId);
        if (StringUtils.isNotBlank(jiraCommentId) && this.skipExisting) {
            System.out.println(
                    "Skipping Issue #" + issue.getNumber() + " Comment " + ghComment.getId() + " which syncs to existing JIRA Comment " + jiraCommentId + " as --skip-existing was set");
            return;
        }

        StringBuilder commentPreamble =
                GitHubUtils.buildPreamble(ghComment.getUser(), ghComment.getCreatedAt(), ghComment.getUpdatedAt(),
                                          "commented",
                                          ghComment.getHtmlUrl().toString());
        CommentInput commentInput =
                new CommentInput(JiraUtils.translateMarkdownToAdfDocument(commentPreamble + ghComment.getBody()),
                                 List.of(new CommentProperty(JiraUtils.COMMENT_PROPERTY_KEY,
                                                             Map.of(JiraUtils.GITHUB_COMMENT_ID_PROPERTY,
                                                                    gitHubCommentId))), null);
        if (!this.dryRun) {
            Promise<Comment> commentCreation = StringUtils.isNotBlank(jiraCommentId) ?
                                               commentsClient.updateComment(jiraIssueKey, jiraCommentId.substring(
                                                                                    jiraCommentId.lastIndexOf('/') + 1),
                                                                            commentInput) :
                                               commentsClient.addComment(jiraIssueKey, commentInput);
            Comment created = commentCreation.claim();
            updateCrossLinks(crossLinks, gitHubCommentId, JiraUtils.getJiraCommentId(jiraIssueKey, created));
            System.out.println((StringUtils.isNotBlank(jiraCommentId) ? "Updated" :
                                "Created") + " a JIRA Comment on JIRA " + jiraIssueKey + " for GitHub Comment " + ghComment.getId());
        } else {
            System.out.println(
                    "[DRY RUN] Would have created/updated an Issue Comment on JIRA Issue " + jiraIssueKey + " for GitHub Comment " + ghComment.getId());
        }
    }

}
