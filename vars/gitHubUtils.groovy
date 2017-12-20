/* Possible URLS:
 * - https://github.com/OpusCapita/minsk-core-jenkins-utils.git
 * - git@github.com:OpusCapita/minsk-core-jenkins-utils.git
 * - https://github.com/OpusCapita/minsk-core-jenkins-utils/commit/28eed320e8bb644d24ec4c30d220803a6b69b664
 */
 def extractRepositoryOwnerAndName(gitHubRepoUrl) {
     def result = gitHubRepoUrl;
     // cut one of the prefixes if present
     if (gitHubRepoUrl.toLowerCase().startsWith('https://github.com/')) {
         result = result.substring('https://github.com/'.length())
     } else if (gitHubRepoUrl.toLowerCase().startsWith('git@github.com:')) {
       	result = result.substring('git@github.com:'.length())
     }
     // cut suffix if present
     if (result.toLowerCase().endsWith('.git')) {
       	result = result.substring(0, result.length() - '.git'.length())
     }
     // cut commit path if present
     def firstSlashIndex = result.indexOf('/');
     if (firstSlashIndex >=0) {
       def secondSlashIndex = result.indexOf('/', firstSlashIndex + 1);
       if (secondSlashIndex >= 1) {
         result = result.substring(0, secondSlashIndex - 1)
       }
     }
     return result
 }
