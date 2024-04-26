# GitHub to JIRA Sync

## 0.3.0

- Add new `issues type-mappings` command for previewing how your defined JIRA Issue Mappings will be applied to the
  issues in a GitHub repository

## 0.2.0

- `issues to-jira` command has significant enhancements:
    - Issues and Comments are now prepended with a preamble indicating the GitHub User who authored the Issue/Comment, a
      link back to the original Issue/Comment, and the creation and update dates
    - GitHub labels are now translated into JIRA labels
    - Closed GitHub issues are now ignored by default unless `--include-closed` is specified
    - GitHub issues are now processed in ascending numerical order, so your oldest GitHub issues will receive smaller
      JIRA keys
    - In dry-run mode inject a placeholder `PROJECT-?` JIRA issue key for issues that would have been newly created
    - Optionally synchronise comments via the `--include-comments`
    - Optionally close GitHub issues after sync'ing them to JIRA via the `--close-after-sync` option
    - Optionally populate a JIRA field with the GitHub repository reference via the `--jira-repository-field <FieldId>`
      option
    - Optionally append extra labels to the JIRA issues via the `--extra-labels <Label1>,<Label2>` option
- `issues cross-links` command upgraded to cope with sync'd comments as well as issues
- Additional utility commands
    - `issues jira-comments` command for listing comments on a given JIRA Issue
    - `issues jira-fields` for listing the custom fields available on your JIRA instance
- Internal improvements
    - Improved JSON parsing and generation for Atlassian Document Format values
    - New REST clients for managing issue comments

## 0.1.1

- Fixed a bug that could occur if trying to sync against a project that does not exist, or to which the provided 
  JIRA credentials have no access
- Standardise reporting of JIRA REST Errors across JIRA related commands

## 0.1.0

- Initial release
- Basic functionality for sync'ing GitHub issues across to JIRA