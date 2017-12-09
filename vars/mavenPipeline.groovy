def call(body) {
	def rtMaven = ''
    def buildInfo = ''
    def server = ''

    // evaluate the body block, and collect configuration into the object
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    pipeline {
        agent any

        options {
            buildDiscarder(logRotator(numToKeepStr: '3'))
        }

        parameters { 
            booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Check if you want to skip tests') 
        }

        stages {
            stage('Checkout Git repository') {
	            steps {
                    git branch: pipelineParams.branch, credentialsId: pipelineParams.scmCredentials, url: pipelineParams.scmUrl
                }
            }

            stage('Maven Build') {
                steps {	
                    script {
                	    server = Artifactory.server "jfrog-artifactory"
                        tMaven = Artifactory.newMavenBuild()
                        rtMaven.deployer server: server, releaseRepo: 'company-release', snapshotRepo: 'company-snapshot'
                        rtMaven.tool = 'Maven 3.3.9'
                        buildInfo = rtMaven.run pom: '${POMPATH}', goals: 'clean install'
                    }
                }
            }

            stage('Upload') {              
                steps {
                    script {
                        server.publishBuildInfo buildInfo
                    }
                }
            }
        }

        post {
            always {
                cleanWs()
                slackNotifier(currentBuild.currentResult)
            }
        }
    }
}
