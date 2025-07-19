pipeline {
    agent any

    environment {
        SONARQUBE = 'SonarQube'               // Jenkins SonarQube server name
        DOCKER_IMAGE = 'back:latest'         // Docker image tag to build and push
        NEXUS_REGISTRY = 'localhost:5000'    // Nexus Docker registry URL
        NEXUS_CREDENTIALS_ID = 'nexus-creds' // Jenkins credentials ID for Nexus login (username/password)
        NEXUS_URL = 'http://nexusmain:8081'

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
                    java -version
                    ./mvnw clean package
                '''
            }
        }

        // stage('SonarQube Analysis') {
        //     steps {
        //         withSonarQubeEnv("${env.SONARQUBE}") {
                    
        //               sh './mvnw sonar:sonar -Dsonar.projectKey=backend'
                    
        //         }
        //     }
        // }

        stage('Deploy JAR to Nexus') {
    steps {
        withCredentials([usernamePassword(
            credentialsId: NEXUS_CREDENTIALS_ID,
            usernameVariable: 'NEXUS_USER',
            passwordVariable: 'NEXUS_PASS'
        )]) {
            echo "Authenticating with Nexus..."
            sh '''
                curl -u ${NEXUS_USER}:${NEXUS_PASS} -I ${NEXUS_URL}/service/rest/v1/status
                if [ $? -ne 0 ]; then
                    echo "Authentication failed!"
                    exit 1
                fi
                echo "Authentication successful"
            '''

            echo "Writing temporary Maven settings.xml..."
            writeFile file: 'settings.xml', text: """
<settings>
  <servers>
    <server>
      <id>nexus-snapshots</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
    <server>
      <id>nexus-releases</id>
      <username>${NEXUS_USER}</username>
      <password>${NEXUS_PASS}</password>
    </server>
  </servers>
</settings>
            """

            echo "Deploying JAR to Nexus..."
            sh '''
                chmod +x mvnw
                ./mvnw --version

                ./mvnw deploy -DskipTests --settings settings.xml -X \
                  -DaltDeploymentRepository=nexus-snapshots::default::${NEXUS_URL}/repository/maven-snapshots/ \
                  -DaltReleaseDeploymentRepository=nexus-releases::default::${NEXUS_URL}/repository/maven-releases/
            '''
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
