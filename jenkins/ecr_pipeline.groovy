pipeline {
  agent any

  environment {
    GIT_CREDENTIALS = "gitAccessTokenId"
    GIT_URL = "https://github.com/thred-app/thred-server"
    BRANCH = "develop"
    IMAGE_VERSION = "latest"
    AWS_CREDENTIALS = "awsAccessKeyId"
    AWS_REGION = "ap-northeast-2"
    ECR_REPOSITORY_URL = "535002874832.dkr.ecr.ap-northeast-2.amazonaws.com"
    ECR_NAME_SPACE = "app"
    ECR_REPOSITORY_NAME = "thred"
    ECS_CLUSTER_NAME = "thred_cluster"
    ECS_SERVICE_NAME = "thred_service"

  }

  stages {

    stage('Git Clone') {
      steps {
        echo 'Cloning Repository'
        git branch: "${BRANCH}", credentialsId: "${GIT_CREDENTIALS}", url: "${GIT_URL}"
      }
    }
    stage('Gradle Build') {
      steps {
        script {
          echo 'Build gradle'
          sh 'chmod +x ./gradlew'
          sh './gradlew clean'
          sh './gradlew build -x test'
        }
      }
    }
    stage('Docker Build') {
      steps {
        script{
          sh "docker build -t app/thred ."
        }
      }
    }
    stage('AWS ECR Login') {
      steps {
        script {
          withAWS(credentials: "${AWS_CREDENTIALS}", region: "${AWS_REGION}") {
            ecrLogin()
            withDockerRegistry(credentialsId: "ecr:${AWS_REGION}:${AWS_CREDENTIALS}", url: "https://${ECR_REPOSITORY_URL}") {
              sh "docker tag app/thred:${IMAGE_VERSION} ${ECR_REPOSITORY_URL}/${ECR_NAME_SPACE}/${ECR_REPOSITORY_NAME}:${IMAGE_VERSION}"
              sh "docker push ${ECR_REPOSITORY_URL}/${ECR_NAME_SPACE}/${ECR_REPOSITORY_NAME}:${IMAGE_VERSION}"
            }
          }

        }
      }
    }
    stage('AWS ECS Deploy'){
      steps{
        script{
          withAWS(credentials: "${AWS_CREDENTIALS}", region: "${AWS_REGION}") {
            sh"""
                aws ecs update-service --region ${AWS_REGION} --cluster ${ECS_CLUSTER_NAME} --service ${ECS_SERVICE_NAME} --force-new-deployment
              """
          }
        }
      }
    }
  }
}

