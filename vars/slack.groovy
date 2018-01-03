// sends build notification to preconfigured on server side slack channel
// current implementation works with GutHub only
def sendNotification() {
    def lastChangesetAuthor = null
    def changesDescription = "";
    currentBuild.changeSets.each { changeSet ->
        def browser = changeSet.browser
        changeSet.each { change ->
            lastChangesetAuthor = change.author.toString()
            def link = browser.getChangeSetLink(change).toString()
            changesDescription = "${changesDescription}\n- ${change.msg} (<${link}|${link.substring(link.lastIndexOf('/') + 1, link.length()).substring(0, 7)}> by ${change.author.toString()})"
        }
    }

    def message = ""
    wrap([$class: 'BuildUser']) {
        def user = lastChangesetAuthor?:env.BUILD_USER_ID
        def whoseBuild = user? "${user}'s" : "anonymous build"
        env.getEnvironment().each { name, value -> println "Name: $name -> Value $value" }
        // retrieving repository information
        def scmInfo = checkout scm
        // extracting branch and whole repo build server job urls if possible
        def regex = "((.+/job/.+/)job/.+/).+/"
        def findings = (env.BUILD_URL =~ /$regex/)
        if (findings.matches()) {
            def branchBuildUrl = findings.group(1)
            def repositoryBuildUrl = findings.group(2)
            message = "${whoseBuild} build (<${env.BUILD_URL}|${env.BUILD_DISPLAY_NAME}>) in <${repositoryBuildUrl}|${gitHubUtils.extractRepositoryOwnerAndName(scmInfo.GIT_URL)}> (<${branchBuildUrl}|${env.BRANCH_NAME}>)"
        } else {
            message = "${whoseBuild} build (<${env.BUILD_URL}|${env.BUILD_DISPLAY_NAME}>) in ${gitHubUtils.extractRepositoryOwnerAndName(scmInfo.GIT_URL)} (${env.BRANCH_NAME})"
        }

    }
    def buildStatus = currentBuild.currentResult
    message = "${(buildStatus == 'FAILURE')?'Failed':'Success'}: ${message}${changesDescription}"

    slackSend message: message, color: (buildStatus == 'FAILURE')?'danger':'good'
}
