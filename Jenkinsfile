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
                sh 'chmod +x mvnw'
                sh '''
                    export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
                    export PATH="$JAVA_HOME/bin:$PATH"
                    java -version
                    ./mvnw clean package
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${env.SONARQUBE}") {
                    
                      sh './mvnw sonar:sonar -Dsonar.projectKey=backend'
                    
                }
            }
        }

        stage('Deploy JAR to Nexus') {
            steps {
                
                    sh './mvnw deploy'
                
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
