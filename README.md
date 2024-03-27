# GitHub to JIRA Sync

This repository provides some experimental tooling to help automate synchronising issues between GitHub and a JIRA
instance so that any issues filed on our public repositories automatically gets reflected into our private JIRA for
tracking alongside our whole product backlog.

# Build

This is a Java based tool built with Maven, it requires Java 17 and a recent Maven 3.x to build:

```bash
$ mvn clean install
```

# Run

The `jira-sync` script serves as the entrypoint for invoking the tool, running without any arguments will show help e.g.

```bash
$ ./jira-sync
NAME
        jira-sync - Provides a CLI for synchronising between JIRA and GitHub

SYNOPSIS
        jira-sync <command> [ <args> ]

COMMANDS
        help
            Shows help about this CLI

        issues cross-links
            Command that calculates the cross-links between GitHub and JIRA

        issues jira-types
            Finds the available JIRA Issue Types for the specified JIRA Project

        issues remote-links
            Retrieves the remote links associated with a JIRA Issue

        issues to-jira
            Command for synchronising GitHub Issues to JIRA

EXIT CODES
        This command returns one of the following exit codes:

            0   Success
            1   Failure
            2   Help Shown
            3   Command Options Invalid
```

You can see help for a specific command by running `jira-sync help <command>` e.g. `jira-sync help issues to-jira` would
show the help for the `to-jira` sub-command of the `issues` command group.

## Authentication

The tool communicates with both JIRA and GitHub so requires credentials for both depending on which command you are
running e.g. some commands only communicate with JIRA so only need JIRA credentials.

For JIRA the tool needs to know the Base URL for your JIRA instance e.g. `https://yourcompany.atlassian.net`, a username
e.g. `you@yourcompany.com` and a REST API Token (which can be created from within Atlassian Account Settings for the
user when running on JIRA Cloud).  The first two are supplied via the `--jira-url` and `--jira-user` options.  To supply
the API token you have two choices, either place the API token in a file and supply the `--jira-token-file` option with
a path to that file, or place it into an environment variable and supply the `--jira-token-env` option with the name of
the environment variable.

So a minimum example options set to communicate with a JIRA instance might look like so:

```
--jira-url https://yourcompany.atlassian.net \
--jira-user you@yourcompany.com \
--jira-token-file /path/to/jira-token
```

For GitHub an API token is required, this token needs to be able to read issues from the repositories whose issues you
wish to sync but requires no further configuration.  Similar to the JIRA API Token there are multiple ways to supply this token:

- The `--github-token-file` option to supply a path to a file containing the token
- OR the `--github-token-env` option to supply the name of the environment variable that contains the token

## Synchronising GitHub Issues to JIRA

There are three steps to synchronising a GitHub repositories issues to JIRA:

1. Computing the existing cross-links between GitHub and JIRA
2. Setup the JIRA Issue mapping rules
3. Synchronising the issue(s)

The first two steps needs to only be done once and both result in a YAML file that can then be supplied to, and is
automatically updated by the 3rd step when appropriate.

### Step 1

The first step is achieved by running the `jira-sync issues cross-links` command, supplying the details of the JIRA
project you want to sync up, in addition to the [Authentication](#authentication) options already discussed e.g.

```bash
$ ./jira-sync issues cross-links --jira-url https://yourcompany.atlassian.net \
  --jira-user you@yourcompany.com \
  --jira-token-file /path/to/jira-token \
  --jira-project-key EXAMPLE \
  --cross-links-file example.yaml
```

This will scan the issues in the JIRA project `EXAMPLE` and update the provided cross links file (`example.yaml`) with
any pre-existing links between the JIRA project and GitHub issues that have been created by this tool.

Note that this step doesn't need to know about GitHub repositories because it is only looking at JIRA to find issues it
has previously sync'd over.  This file helps to ensure that when we run [Step 3](#step-3) we update pre-existing JIRA
issues where appropriate and only create new JIRA issues where one doesn't yet exist.

As noted if you keep this file around somewhere then there is no need to run this step every time, you can just supply
the file to [Step 3](#step-3) and it automatically keeps it up to date as it sync's new issues.

### Step 2

In order for the tool to know what type of JIRA issues to create you need to establish some mapping rules that tell it
how a GitHub issue should be mapped into a JIRA issue.  The first stage of this is to run the `jira-sync issues
jira-types` command to see what issue types your JIRA project has available:

```bash
$ ./jira-sync issues jira-types --jira-url https://yourcompany.atlassian.net \
  --jira-user you@yourcompany.com \
  --jira-token-file /path/to/jira-token \
  --jira-project-key EXAMPLE \
Issue Type ID: 10000
Name: Epic
Description:
A collection of related bugs, stories, and tasks.

Issue Type ID: 10008
Name: Feature
Description:
Functionality or a feature expressed as a user goal.

Issue Type ID: 10009
Name: Task
Description:
A small, distinct piece of work.

Issue Type ID: 10010
Name: Sub-task
Description:
A small piece of work that's part of a larger task.

Issue Type ID: 10011
Name: Bug
Description:
A problem or error.

Issue Type ID: 10012
Name: UI/UX
Description:
A UI/UX design task.

Issue Type ID: 10013
Name: Documentation
Description:
An update to, or creation of, documentation.

Issue Type ID: 10018
Name: Projects
Description:
A project or programme through which multiple epics & features will be delivered
```

This provides you with details of the JIRA Issue Types available, the Issue Type ID being the key piece of information
you need to define your mapping rules.  Mapping rules are defined in a simple YAML format e.g.

```yaml
defaultJiraIssueType: 10009
rules:
  - type: label
    labels:
      - bug
    jiraIssueType: 10011
  - type: title
    searchTerms:
      - feature
      - epic
    jiraIssueType: 10000
```

The above rules says the following:

- Issues that have the label `bug` on them are mapped to JIRA Issue Type `10011`
- Issues with `feature`/`epic` in the title are mapped to JIRA Issue Type `10000`
- Any other issue is given the default JIRA Issue Type `10009`

Label and search terms are both matched case insensitively i.e. `feature` would match `Feature`, `FEATURE` or any other
variant thereof.  Rules are matched in the order given with the first matching rules `jiraIssueType` being used, only if
no rules match an issue is the `defaultJiraIssueType` used.

Currently only the rule types seen above are supported, future updates of this tool may provide more rules as we gain
practical experience of using this tool.

Once you've created your YAML rules file, which we'll refer to as `mapping-rules.yaml`, you can provide it to [Step
3](#step-3) via the `--jira-issue-mappings` option.

**NB** Alternatively, if you want all GitHub issues to map to a single JIRA Issue type regardless you can omit this step
entirely and instead supply the `--jira-issue-type` option with the single issue type value.

### Step 3

In this step we need to know both JIRA details, and a GitHub repository whose issue(s) you wish to sync.  We sync issues
using the `jira-sync issues to-jira` command.  This command can either sync a single issue or all the issues in your
GitHub repository according to your preference.  It uses the cross links file created in [Step 1](#step-1) to ensure it
doesn't repeatedly create new JIRA issues for GitHub issues that have already been sync'd.

```bash
$ ./jira-sync issues to-jira --jira-url https://yourcompany.atlassian.net \
  --jira-user you@yourcompany.com \
  --jira-token-file /path/to/jira-token \
  --jira-project-key EXAMPLE \
  --cross-links-file example.yaml \
  --github-repository your-org/your-repo \
  --github-token-file /path/to/github-token \
  --cross-links-file example.yaml \
  --jira-issue-mapping mapping-rules.yaml
```

This will then go through the GitHub issues in the given repository (assuming you supplied a GitHub API Token with
sufficient permissions) and synchronise them over to the targeted JIRA project in your JIRA instance.  Where issues have
been previously sync'd to JIRA (as identified by the cross links file) then the corresponding JIRA issue is updated.

If you wish to only sync a single issue you can do so by supplying the `--github-issue-id` option with the ID of the
issue in question e.g. `--github-issue-id 123` would sync only Issue 123 from your GitHub repository.

# FAQs

## What gets sync'd to JIRA?

Currently the title and the content of a GitHub issue gets sync'd across to JIRA.  The title will populate the Summary
field of the issue, and the content the description field.  The tool uses some of Atlassian's newer tooling to attempt
to convert Markdown in the GitHub content into richly formatted text in JIRA, **but** not all Markdown syntax translates
so you **MAY** see some weird formatting.

The assignee/reporter of an issue is not sync'd, nor are any GitHub labels, GitHub comments etc.  Also if a GitHub issue
transitions, i.e. closes, the corresponding JIRA does not transition currently.  All these things **MAY** be supported
in the future subject to demand.

## Are issues always updated?

Yes, unless you supply the `--skip-existing` option.

## Whose JIRA/GitHub Credentials should I used?

As detailed in [Authentication](#authentication) you need both JIRA and GitHub credentials to use this tool.  We would
recommend that once you get past the experimentation stage (where you might use personal credentials) for actual day to
day usage you create a "bot" account on both the JIRA and GitHub sides and use those credentials.

Particularly on the JIRA side this gives you an easy way to filter for issues created by this tool by filtering on the
reporter field.

## Where should I store the cross links file?

If you are only running this tool locally then store it locally.

If you are automating the running of this tool, e.g. on CI/CD infrastructure, then store it somewhere persistent and
read and write it before and after the build.

## What if I lose my cross links file?

The tool takes advantage of internal metadata for JIRA remote links to allow it to detect the links it has previously
created during sync operations.  Provided JIRA users are not removing those links or modifying them this allows the
information to be recovered.

Therefore, you can always use the command shown in [Step 1](#step-1) to recreate a lost cross links file if necessary.

## Why not use the Atlassian JIRA GitHub App?

Because it doesn't offer this functionality.  It only provides for automatic linking of commits, issues and PRs that
mention a JIRA project key, e.g. EXAMPLE-1234, to the associated JIRA issue.  There is no ability to automatically sync
issues from the GitHub side to JIRA.

## Who not use X instead?

We looked at a number of pre-existing GitHub to JIRA sync tools but they were almost exclusively paid applications, or
their feature set did not meet our fairly minimalist requirements.  We only wanted to reflect any GitHub issue that was
created into our JIRA instance for tracking purposes, not maintain full sync between the two systems.

## Can I use this in a GitHub Actions workflow?

Yes, please see our [shared JIRA Sync
workflow](https://github.com/telicent-oss/shared-workflows/blob/main/.github/workflows/jira-sync.yml) for an example of 
one possible way to do that.
