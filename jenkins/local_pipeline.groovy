pipeline {
    agent any
    environment {
        BRANCH_NAME = 'develop'
        DOCKER_USER_NAME = 'namgungjaeseon'
        DOCKER_IMAGE = 'thred-dev'
        DOCKER_TAG = 'dev'
        DOCKER_REPO = "${env.DOCKER_USER_NAME}/${env.DOCKER_IMAGE}"
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: "${env.BRANCH_NAME}", credentialsId: 'github_access_token', url: 'https://github.com/thred-app/thred-server'
            }
        }
        stage('Docker Build & Push') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'dockerAccessTokenId') {
                        sh """
                            docker build --platform=linux/amd64 -t ${env.DOCKER_REPO}:${env.DOCKER_TAG} .
                            docker push ${env.DOCKER_REPO}:${env.DOCKER_TAG}
                        """
                    }
                }

            }
        }
        stage('SSH EC2') {
            steps {
                sshPublisher(publishers: [sshPublisherDesc(configName: 'thred-server',
                        transfers: [sshTransfer(
                                sourceFiles: "docker/thred-compose.yaml",
                                removePrefix: "docker",
                                remoteDirectory: '/thred/docker',
                                execCommand: """
                                                docker system prune -af
                                                docker compose -f thred/docker/thred-compose.yaml pull app
                                                docker compose -f thred/docker/thred-compose.yaml up -d app
                                             """)])])
            }
        }
        stage('Health Check') {
            steps {
                script {
                    def retries = 10
                    def success = false
                    for (int i = 0; i < retries; i++) {
                        try {
                            def response = sh(script: "curl -fs https://thred.site/actuator/health",
                                    returnStdout: true).trim();
                            if (response.contains('"status":"UP"')) {
                                echo "스프링 부트 실행 성공"
                                success = true
                                break
                            }
                        } catch (Exception e) {
                            echo "스프링 부트 실행 준비중... (${i + 1}/${retries})"
                        }
                        sleep time: 15, unit: 'SECONDS'
                    }
                    if (!success) {
                        error("Health check failed: Spring Boot is DOWN")
                    }
                }
            }
        }
    }
    post {
        success {
            discordSend(
                    customUsername: 'Jenkins',
                    description: "프로젝트(${env.BRANCH_NAME})가 성공적으로 배포되었습니다.",
                    footer: '배포 시스템 by Jenkins',
                    link: env.BUILD_URL,
                    result: currentBuild.currentResult,
                    scmWebUrl: 'https://github.com/thred-app/thred-server/',
                    showChangeset: true,
                    title: '배포 성공',
                    webhookURL: 'https://discord.com/api/webhooks/1408218429638443048/wqv_1JamJwsNi-x2gnJQiBufDCZV4P-jD_LYF9acjny-f5F2Bi01ao6-wZp7i4LMSwvl'
            )
        }
        failure {
            sshPublisher(publishers: [
                    sshPublisherDesc(
                            configName: 'thred-server',
                            transfers: [
                                    sshTransfer(
                                            execCommand: """
                                docker compose -f thred/docker/thred-compose.yaml stop app
                                docker compose -f thred/docker/thred-compose.yaml rm -f app
                            """
                                    )
                            ]
                    )
            ])
            discordSend(
                    customUsername: 'Jenkins',
                    description: "프로젝트(${env.BRANCH_NAME}) 배포가 실패했습니다.",
                    footer: '배포 시스템 by Jenkins',
                    link: env.BUILD_URL,
                    result: currentBuild.currentResult,
                    scmWebUrl: 'https://github.com/thred-app/thred-server/',
                    showChangeset: true,
                    title: '배포 실패',
                    webhookURL: 'https://discord.com/api/webhooks/1408218429638443048/wqv_1JamJwsNi-x2gnJQiBufDCZV4P-jD_LYF9acjny-f5F2Bi01ao6-wZp7i4LMSwvl'
            )

        }
    }
}
