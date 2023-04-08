#!/usr/bin/env groovy
@Library('shared-library') _

pipeline {
agent any
options {
    buildDiscarder(logRotator(numToKeepStr:'5' , artifactNumToKeepStr: '5'))
    timestamps()
    }
parameters {
    string(name: 'url', defaultValue: '', description: 'Git URL')
    string(name: 'branch', defaultValue: '', description: 'Git Branch')
}
environment {
      inventoryName    = 'Bommasani'
}
  stages {
    stage('CheckOut') {
      steps {
        echo 'Checking out project from Bitbucket....'
        dir("vamsi") {
          gitCheckout(
            branch: "${params.branch}",
            url: "${params.url}"
          )
        }
      }
    }
stage('SHELL') {
      steps {
        ansiColor('xterm') {
          echo 'Cleaning workspace....'
           sh """
            java -version
            mvn --version
            gradle -version
            ant -version
            ansible --version
            git --version
            terraform -v
            ruby -v
            aws --version
            az --version
            node -v
          """
        }
      }
    }
stage('MAVEN') {
      steps {
        ansiColor('xterm') {
          echo 'Maven Build....'
           sh """
          cd ${WORKSPACE}/vamsi/maven;
          mvn clean install
          """
        }
      }
    }
stage('GRADLE') {
      steps {
        ansiColor('xterm') {
          echo 'Gradle Build....'
           sh """
          cd ${WORKSPACE}/vamsi/gradle;
          ./gradlew clean build
          """
        }
      }
    }
stage('ANT') {
      steps {
        ansiColor('xterm') {
          echo 'Ant Build....'
           sh """
          cd ${WORKSPACE}/vamsi/ant;
          ant -buildfile build.xml
          """
        }
      }
    }
stage('NPM') {
      steps {
        ansiColor('xterm') {
          echo 'NPM Build....'
           sh """
          cd ${WORKSPACE}/vamsi/npm;
          npm install
          npm test
          """
        }
      }
    }
  }//end stages
  post {
      success {
          archiveArtifacts artifacts: "vamsi/ant/build/jar/*.jar"
          archiveArtifacts artifacts: "vamsi/gradle/build/libs/*.jar"
          archiveArtifacts artifacts: "vamsi/maven/target/*.jar"
      }
      failure {
          echo "The build failed."
      }
      cleanup{
        deleteDir()
      }
    }
}
