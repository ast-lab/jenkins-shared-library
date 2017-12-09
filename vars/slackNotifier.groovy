#!/usr/bin/env groovy

def call(String buildResult) {
	println buildResult
	if ( buildResult == "SUCCES" ) {
	    slackSend color: 'good', message: 'Job: ${env.JOB_NAME} BuildNumber ${env.BUILD_NUMBER} was successful'
	}
}