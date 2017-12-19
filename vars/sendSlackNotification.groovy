def sendSlackNotification(currentBuild, env) {
    def result = currentBuild.currentResult
    def notify = true
    /*
    uncommented this code if you want notify message to slake between state change FAILURE -> SUCCESS || SUCCESS -> FAILURE
    def previousResult = currentBuild.previousBuild?.result
    notify = previousResult != "FAILURE" && result == "FAILURE") || (result == "SUCCESS" && (previousResult == "FAILURE" || previousResult == "UNSTABLE"))
    */
    if (notify) {
        def message = ""
        wrap([$class: 'BuildUser']) {
            def ref = currentBuild.changeSets.collect { changeSet ->
                changeSet.browser.getChangeSetLink(changeSet.find { true })?.toString()
            }.find { true }
            if (ref) {
                ref = ref.substring(0, ref.indexOf('/commit/'))
                message = "${env.BUILD_USER_ID}'s build (<${env.BUILD_URL}|${env.BUILD_DISPLAY_NAME}>; push) in <${ref}|${ref - "https://github.com/"}> (${env.BRANCH_NAME})"
            } else {
                message = "${env.BUILD_USER_ID}'s build (<${env.BUILD_URL}|${env.BUILD_DISPLAY_NAME}>) no changes (${env.BRANCH_NAME})"
            }
        }
        message = "${(result == 'FAILURE')?'Failed':'Success'}: ${message}"
        currentBuild.changeSets.each { changeSet ->
            def browser = changeSet.browser
            changeSet.each { change ->
                def link = browser.getChangeSetLink(change).toString()
                message = "${message}\n- ${change.msg} (<${link}|${link.substring(link.lastIndexOf('/') + 1, link.length()).substring(0, 7)}> by ${change.author.toString()})"
            }
        }
        slackSend channel: "#minsk-coreteam-ci", message: message, color: (result == 'FAILURE')?'danger':'good'
    }
}
