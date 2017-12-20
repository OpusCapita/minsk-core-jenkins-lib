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
        def scmInfo = checkout scm
        def branchBuildUrl = (!env.BUILD_URL.endsWith('/'))?:env.BUILD_URL.substring(0, env.BUILD_URL.length() - 1)
        def repoBuildUrl = (branchBuildUrl.lastIndexOf('/') <= 0)?branchBuildUrl:branchBuildUrl.substring(0, branchBuildUrl.lastIndexOf('/'))
        message = "${env.BUILD_USER_ID}'s build (<${env.BUILD_URL}|${env.BUILD_DISPLAY_NAME}>) in <${repoBuildUrl}|${gitHubUtils.extractRepositoryOwnerAndName(scmInfo.GIT_URL)}> (<${branchBuildUrl}}|${env.BRANCH_NAME}>)"
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
