pipeline {
    agent any
    tools {
        gradle 'gradle'
    }
    stages {
        stage('Git Clone') {
            steps {
                git branch: 'develop', credentialsId: 'gitAccessTokenId', url: 'https://github.com/thred-app/thred-server'
            }
        }
        stage('Docker Build And Push') {
            steps {
                script {
                    withDockerRegistry([credentialsId: "dockerAccessTokenId", url: ""]) {
                        sh 'docker buildx create --use'
                        sh 'docker buildx build --platform=linux/amd64,linux/arm64 -t namgungjaeseon/thred_app:dev . --push'
                    }
                }
            }
        }
        stage('Deploy Spring Boot') {
            steps {
                script {
                    dir('docker') {
                        sh '''
                            docker compose -f thred-compose.yaml pull app
                            docker compose -f thred-compose.yaml up -d app --force-recreate
                           '''
                    }
                }
            }
        }
    }
}
