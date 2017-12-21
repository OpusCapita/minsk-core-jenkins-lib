// sends build notification to preconfigured on server side slack channel
// current implementation works with GutHub only
def sendNotification() {
    /*
    uncommented this code if you want notify message to slake between state change FAILURE -> SUCCESS || SUCCESS -> FAILURE
    def previousResult = currentBuild.previousBuild?.result
    notify = previousResult != "FAILURE" && result == "FAILURE") || (result == "SUCCESS" && (previousResult == "FAILURE" || previousResult == "UNSTABLE"))
    */
    def message = ""
    wrap([$class: 'BuildUser']) {
        // retrieving repository information
        def scmInfo = checkout scm
        // extracting branch and whole repo build server job urls if possible
        def regex = "((.+/job/.+/)job/.+/).+/"
        def findings = (url =~ /$regex/)
        if (findings.matches()) {
            branchBuildUrl = findings.group(1)
            repositoryBuildUrl = findings.group(2)
            message = "${env.BUILD_USER_ID}'s build (<${env.BUILD_URL}|${env.BUILD_DISPLAY_NAME}>) in <${repositoryBuildUrl}|${gitHubUtils.extractRepositoryOwnerAndName(scmInfo.GIT_URL)}> (<${branchBuildUrl}}|${env.BRANCH_NAME}>)"
        } else {
            message = "${env.BUILD_USER_ID}'s build (<${env.BUILD_URL}|${env.BUILD_DISPLAY_NAME}>) in ${gitHubUtils.extractRepositoryOwnerAndName(scmInfo.GIT_URL)} (${env.BRANCH_NAME})"
        }

    }
    def buildStatus = currentBuild.currentResult
    message = "${(buildStatus == 'FAILURE')?'Failed':'Success'}: ${message}"
    currentBuild.changeSets.each { changeSet ->
        def browser = changeSet.browser
        changeSet.each { change ->
            def link = browser.getChangeSetLink(change).toString()
            message = "${message}\n- ${change.msg} (<${link}|${link.substring(link.lastIndexOf('/') + 1, link.length()).substring(0, 7)}> by ${change.author.toString()})"
        }
    }
    slackSend message: message, color: (buildStatus == 'FAILURE')?'danger':'good'
}
