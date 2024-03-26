package io.telicent.jira.sync.cli.options;

import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.MutuallyExclusiveWith;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.connector.GitHubConnector;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Options for connecting to GitHub
 */
public class GitHubOptions {

    @Option(name = "--github-token-env", title = "EnvVar", description = "Supplies the name of an environment variable from which a GitHub Personal Access Token can be obtained and used to authenticate to the GitHub API")
    @MutuallyExclusiveWith(tag = "github-auth-methods")
    private String envVar;

    @Option(name = "--github-token-file", title = "PatFile", description = "Supplies a file from which a GitHub Personal Access Token can be read and used to authenticate to the GitHub API")
    @MutuallyExclusiveWith(tag = "github-auth-methods")
    private File patFile;

    private GitHub instance = null;

    /**
     * Connects to GitHub based on one of the provided authentication options
     * <p>
     * Caches the created {@link GitHub} instance for the lifetime of the command so repeated calls to this return the
     * already created instance.
     * </p>
     *
     * @return GitHub API Client
     * @throws IOException Thrown if unable to create an API Client instance based on the provided options
     */
    public GitHub connect() throws IOException {
        if (this.instance != null) {
            return this.instance;
        }

        GitHubConnector connector = new OkHttpGitHubConnector(new OkHttpClient());
        if (StringUtils.isNotBlank(envVar)) {
            this.instance = new GitHubBuilder().withOAuthToken(System.getenv(envVar)).withConnector(connector).build();
        } else if (patFile != null) {
            System.out.println("Reading GitHub PAT Token from file " + patFile.getAbsolutePath());
            try (BufferedReader reader = new BufferedReader(new FileReader(this.patFile))) {
                this.instance = new GitHubBuilder().withOAuthToken(reader.readLine()).withConnector(connector).build();
            }
        } else {
            this.instance = GitHubBuilder.fromEnvironment().withConnector(connector).build();
        }

        return this.instance;
    }
}
