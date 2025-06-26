pipeline {
  agent any

  environment {
    SONARQUBE = 'MySonar' // Name from Jenkins config
  }

  stages {
    stage('Clone Back Repo') {
      steps {
        git 'https://github.com/safehaddar1/Safe-Haddar-Back-Repo'
      }
    }

    stage('Build Backend') {
      steps {
        sh './mvnw clean install' // ou `mvn clean install` selon ton projet
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv("${SONARQUBE}") {
          sh './mvnw sonar:sonar' // ou `mvn sonar:sonar`
        }
      }
    }
  }
}

