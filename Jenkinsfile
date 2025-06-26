pipeline {
  agent any

  environment {
    SONARQUBE = 'MySonar'
  }

  stages {
    stage('Build Backend') {
      steps {
        sh './mvnw clean install'
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv("${SONARQUBE}") {
          sh './mvnw sonar:sonar'
        }
      }
    }
  }
}