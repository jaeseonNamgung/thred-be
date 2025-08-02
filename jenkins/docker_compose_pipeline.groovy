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
                        sh 'docker build -t namgungjaeseon/thred_app:dev .'
                        sh 'docker push namgungjaeseon/thred_app:dev'
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
