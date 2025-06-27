pipeline {
    agent any

    environment {
        SONARQUBE = 'SonarQube'               // Jenkins SonarQube server name
        DOCKER_IMAGE = 'back:latest'         // Docker image tag to build and push
        NEXUS_REGISTRY = 'localhost:5000'    // Nexus Docker registry URL
        NEXUS_CREDENTIALS_ID = 'nexus-creds' // Jenkins credentials ID for Nexus login (username/password)
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                configFileProvider([configFile(fileId: 'nexus-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh 'chmod +x mvnw'
                    sh './mvnw clean package -s $MAVEN_SETTINGS'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${env.SONARQUBE}") {
                    configFileProvider([configFile(fileId: 'nexus-settings', variable: 'MAVEN_SETTINGS')]) {
                        sh './mvnw sonar:sonar -Dsonar.projectKey=backend -s $MAVEN_SETTINGS'
                    }
                }
            }
        }

        stage('Deploy JAR to Nexus') {
            steps {
                configFileProvider([configFile(fileId: 'nexus-settings', variable: 'MAVEN_SETTINGS')]) {
                    sh './mvnw deploy -s $MAVEN_SETTINGS'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t back:latest ."
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    def imageName = "${env.NEXUS_REGISTRY}/${env.DOCKER_IMAGE}"

                    // Tag the image with Nexus registry URL
                    sh "docker tag ${env.DOCKER_IMAGE} ${imageName}"

                    // Secure login and push
                    withCredentials([usernamePassword(credentialsId: env.NEXUS_CREDENTIALS_ID, usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
                        sh """
                            echo \$NEXUS_PASS | docker login ${env.NEXUS_REGISTRY} -u \$NEXUS_USER --password-stdin
                            docker push ${imageName}
                        """
                    }
                }
            }
        }
    }
}
