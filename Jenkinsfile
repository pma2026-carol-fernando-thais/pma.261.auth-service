pipeline {
    agent any
    environment {
        SERVICE = 'auth'
        NAME = "thaisrs3/${env.SERVICE}"
        AWS_REGION = 'us-east-2'
        EKS_CLUSTER = 'eks-store2'
    }
    stages {
        stage('SCM') {
            steps {
                checkout scm
            }
        }
        stage('Dependecies') {
            steps {
                build job: 'auth', wait: true
            }
        }
        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }
        stage('Vulnerability Scan') {
            steps {
                sh 'mvn -B org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=11'
            }
        }
        stage('Build & Push Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credential',
                    usernameVariable: 'USERNAME',
                    passwordVariable: 'TOKEN')])
                {
                    sh "docker login -u $USERNAME -p $TOKEN"
                    sh "docker buildx create --use --platform=linux/arm64,linux/amd64 --node multi-platform-builder-${env.SERVICE} --name multi-platform-builder-${env.SERVICE}"
                    sh "docker buildx build --platform=linux/arm64,linux/amd64 --push --tag ${env.NAME}:latest --tag ${env.NAME}:${env.BUILD_ID} -f Dockerfile ."
                    sh "docker buildx rm --force multi-platform-builder-${env.SERVICE}"
                }
            }
        }
        stage('Deploy to K8s') {
            steps {
                withCredentials([
                    string(credentialsId: 'jwt-secret-key', variable: 'JWT_SECRET_KEY'),
                    [$class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: 'aws-credentials',
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']])
                {
                    sh "aws eks update-kubeconfig --region ${env.AWS_REGION} --name ${env.EKS_CLUSTER}"
                    sh "envsubst < k8s/k8s.yaml | kubectl apply -f -"
                }
            }
        }
    }
}
