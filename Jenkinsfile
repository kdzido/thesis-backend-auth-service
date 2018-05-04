// Declarative Continuous Deployment Pipeline

pipeline {
    agent {
        node { label 'docker-enabled' }
    }
    options {
        timestamps()
    }

    environment {
        PIPELINE_BUILD_ID = "${GIT_BRANCH}-${BUILD_NUMBER}"
        DOCKERHUB_CREDS = credentials("dockerhub")
        // implicit DOCKERHUB_CREDS_USR
        // implicit DOCKERHUB_CREDS_PSW
    }

    stages {
        stage('Unit') {
            steps {
                withEnv(["COMPOSE_FILE=docker-compose-test.yml"]) {
                    sh 'mkdir -p backend-auth-service/build/dockerfile'   // dir must exist for docker-compose
                    sh 'docker-compose run --rm unit'
                    sh 'docker-compose build app'
                }
            }
        }
        stage('Staging') {
            steps {
                withEnv(["COMPOSE_FILE=docker-compose-test.yml"]) {
                    sh 'docker-compose run --rm staging'
                }
            }
        }
        stage("Publish") { // Local Docker registry
            steps {
                sh "docker tag thesis-authservice:snapshot localhost:5000/thesis-authservice"
                sh "docker tag thesis-authservice:snapshot localhost:5000/thesis-authservice:${env.BUILD_NUMBER}"
                sh "docker push localhost:5000/thesis-authservice"
                sh "docker push localhost:5000/thesis-authservice:${env.BUILD_NUMBER}"
            }
        }
    }

    post {
        always {
            // TODO handle non-existing backend-auth-service/build/dockerfile
            withEnv(["COMPOSE_FILE=docker-compose-test.yml"]) {
                sh "docker-compose down"
            }
        }
    }

}
