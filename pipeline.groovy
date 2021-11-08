import java.text.SimpleDateFormat
import hudson.tasks.test.AbstractTestResultAction
import hudson.model.Actionable

def call(body) {
    def config = [: ]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
def applicationName = config.applicationName?:''
def srcpath = config.srcpath ?:''
def buildNode = config.buildNode ?:''
def dateFormat = new SimpleDateFormat("yyyyMMddHHmm")
def gitDateFomat = new SimpleDateFormat("yyyy-MM-dd")
def gitCurrentDate = gitDateFomat.format(new Date())
def date = dateFormat.format(new Date())
def sanityTotalTests = '0'
def sanityFailedTests = '0'
def sanityPassedTests = '0'
def executeSonar = config.executeSonar ?: 'YES'
def sonarPublishProfile = config.sonarPublishProfile?:''
def MSBuildpath = config.MSBuildpath?:''
def nunit = config.nUnit ?: ''
def nunit_Console = config.nunitConsole ?: ''
def sonarRunner = ""
def sonarMSBuild = ""
currentBuild.displayName = "Build-#" + currentBuild.id
properties([disableConcurrentBuilds(),gitLabConnection('Gitlab')])
        node("${buildNode}") {
            currentBuild.result = 'SUCCESS'  
            
            stage('Checkout') {
                updateGitlabCommitStatus name: STAGE_NAME, state: 'running'
                bat "attrib -r -s /s /d"
                cleanWs()
                step([$class: 'WsCleanup'])
                checkout scm
                gitUrl = bat(returnStdout: true, script: '@git config remote.origin.url').trim()
                def commitID = ''
                commiterName = bat(returnStdout: true, script: '@git log -1 --format=%%aE').trim()
                commitID = bat(returnStdout: true, script: '@git rev-parse --short HEAD').trim()
                totalCommitsOfUser = bat(returnStdout: true, script: "@git rev-list --count HEAD --author=${commiterName}").trim()
                totalCommitsInBranch = bat(returnStdout: true, script: '@git rev-list --count HEAD').trim()
                appName = "${applicationName}"  
			    branch = "${BRANCH_NAME}"
                updateGitlabCommitStatus name: STAGE_NAME, state: 'success'
            }
            stage('Installing Dependency') {
                updateGitlabCommitStatus name: STAGE_NAME, state: 'running'
                pipelineStage = "${STAGE_NAME}"
                bat "java -version"
                bat "javac -version"
                installingDependency {
                  xcopy_Path = "${xcopyPath}"
                  nuget_ToolPath = "${nugetPath}"
                  src_path = "${srcpath}"      
              } 
                updateGitlabCommitStatus name: STAGE_NAME, state: 'success'
            } 
           stage('Build') {
                updateGitlabCommitStatus name: STAGE_NAME, state: 'running'
                pipelineStage = "${STAGE_NAME}"
                echo "${MSBuildpath}"
                dotNetBuild {
                    PublishProfile = "${publishProfile}"
                    MS_BuildPath = "${MSBuildpath}"
                    exclude_Path = "${excludeFilesFromDeployment}"
                    publish_Path = "${publishPath}"
                }

                updateGitlabCommitStatus name: STAGE_NAME, state: 'success'
	stage("Recording TestResults"){
                //              if(!("${BRANCH_NAME}" ==~ "^Master-[0-9]*")){
                //         updateGitlabCommitStatus name: STAGE_NAME, state: 'running'
                //             }
                //               pipelineStage = "${STAGE_NAME}"
                //         unitTesting1 {
                  
                //                   NUnit = "${nunit}"
                //         }          
                //         updateGitlabCommitStatus name: STAGE_NAME, state: 'success'
                // }

                
            }
            if ("${executeSonar}" == 'YES') {
                stage('Sonar Scan') {
                    updateGitlabCommitStatus name: STAGE_NAME, state: 'running'
                    pipelineStage = "${STAGE_NAME}"
                    bat '''sqlplus --version'''
                    bat '''whoami'''  
                    sonar {
                        sonar_projectName = "${sonarProjectName}"
                        open_Cover = "${openCover}"
						nunitConsole = "${nunit_Console}"
                        report_Generator = "${reportGenerator}"
                        MS_BuildPath = "${sonarMSBuild}"
                        sonar_Runner = "${sonarRunner}"
                        exclude_Path = "${excludePathSonar}"
                        publish_Path = "${sonarPublishProfile}"
                        src_path = "${srcpath}"
                    }
                    updateGitlabCommitStatus name: STAGE_NAME, state: 'success'
                } 
    
                stage("Sonar Quality gate Check") {
                    updateGitlabCommitStatus name: STAGE_NAME, state: 'running'
                    pipelineStage = "${STAGE_NAME}"
                    try {
                        timeout(time: 1, unit: 'HOURS') {
                            def qualityGate = waitForQualityGate()
                            if (qualityGate.status != 'OK') {
                                error "Pipeline aborted due to quality gate failure: ${qualityGate.status}"
                            }
                        }  //End of timeout
                    } catch (Exception err) {
                        error "Failing build From jenkins in sonarqulaity gate evaluation"
                    }
                    updateGitlabCommitStatus name: STAGE_NAME, state: 'success'
                } //End of QualityGate check stage
                
            }

            stage('Zipping Artifacts') {
              updateGitlabCommitStatus name: STAGE_NAME, state: 'running'
              pipelineStage = "${STAGE_NAME}"
              zippingArtifact{
                  publish_Path = "${publishPath}"
                  dest_zipfolder = "${destzipfolder}"
              }
                updateGitlabCommitStatus name: STAGE_NAME, state: 'success'
            } 



