// add new comment about build proces and its status to a Jira tickets if VC changes contains links to any Jira ticket
def addComments() {
    // jiraIssues is a Map kie this: ['TEST-1, TEST-2']
    def jiraIssueKeys = jiraIssueSelector(issueSelector: [$class: 'DefaultIssueSelector'])
    echo "jiraIssueKeys: ${jiraIssueKeys}"
    // ['TEST-1, TEST-2'] -> square brackets are removed, split using comma, trim each issue key and remove empty ones
    //per each ticket mentioned in commit messages
    for (jiraIssueKey in jiraIssueKeys) {
        def comment = "${currentBuild.currentResult}: Integrated in Jenkins build ${JOB_NAME} #${BUILD_NUMBER}, (See ${BUILD_URL})"
        currentBuild.changeSets.each { changeSet ->
            def browser = changeSet.browser
            changeSet.each { change ->
                if (change.msg.toLowerCase().contains(jiraIssueKey.toLowerCase())) {
                    def link = browser.getChangeSetLink(change)
                    comment = "${comment}\n\n${change.msg} (${change.author.toString()}: [${link}])"

                    for (file in change.affectedFiles) {
                        comment += "\n* (${file.editType.name.toLowerCase()}) ${file.path}"
                    }
                }
            }
        }
        // post results into Jira
        jiraComment body: comment, issueKey: "${jiraIssueKey}"
    }
}
