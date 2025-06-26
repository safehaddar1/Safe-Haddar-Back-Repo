pipeline {
    agent any

    environment {
        SONARQUBE = 'SonarQube'
        DOCKER_IMAGE_NAME = 'back'
        DOCKER_IMAGE_TAG = 'latest'
        DOCKER_REGISTRY = 'nexus:5000'  // Make sure Jenkins can resolve 'nexus' to the Nexus container IP or hostname
        FULL_IMAGE = "${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
        NEXUS_CREDENTIALS_ID = 'nexus-creds'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${env.SONARQUBE}") {
                    sh './mvnw sonar:sonar -Dsonar.projectKey=backend'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ."
                sh "docker tag ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ${FULL_IMAGE}"
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: NEXUS_CREDENTIALS_ID,
                    usernameVariable: 'NEXUS_USER',
                    passwordVariable: 'NEXUS_PASS'
                )]) {
                    sh """
                        echo "\${NEXUS_PASS}" | docker login ${DOCKER_REGISTRY} -u \${NEXUS_USER} --password-stdin
                        docker push ${FULL_IMAGE} || (sleep 5 && docker push ${FULL_IMAGE}) || (sleep 10 && docker push ${FULL_IMAGE})
                        docker logout ${DOCKER_REGISTRY}
                    """
                }
            }
        }
    }

    post {
        always {
            sh """
                docker rmi ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} || true
                docker rmi ${FULL_IMAGE} || true
            """
        }
    }
}
