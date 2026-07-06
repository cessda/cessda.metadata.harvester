pipeline {
    options {
        buildDiscarder logRotator(artifactNumToKeepStr: '5', numToKeepStr: '10')
    }

    environment {
        product_name = 'cdc'
        module_name = 'harvester'
        image_tag = "${DOCKER_ARTIFACT_REGISTRY}/${product_name}-${module_name}:${env.BRANCH_NAME.replaceAll('[^a-z0-9\\.\\_\\-]', '-')}-${env.BUILD_NUMBER}"
    }

    agent {
        label 'jnlp-himem'
    }

    stages {
        // Building on main
        stage('Pull SDK Docker Image') {
            agent {
                docker {
                    image 'eclipse-temurin:25'
                    reuseNode true
                }
            }
            environment {
                HOME = "${WORKSPACE_TMP}"
            }
            stages {
                stage('Build Project') {
                    steps {
                        withMaven {
                            sh './mvnw -Pnative clean verify'
                        }
                    }
                }
                stage('Record Issues') {
                    steps {
                        recordIssues aggregatingResults: true, tools: [errorProne(), java()]
                    }
                }
                stage('Run Sonar Scan') {
                    steps {
                        withSonarQubeEnv('cessda-sonar') {
                            withMaven {
                                sh './mvnw sonar:sonar'
                            }
                        }
                    }
                    when { branch 'main' }
                }
            }
        }
        stage("Get Sonar Quality Gate") {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: false
                }
            }
            when { branch 'main' }
        }
        stage('Build Docker Image') {
            steps {
                withMaven {
                    sh "./mvnw -Pnative spring-boot:build-image-no-fork -Dspring-boot.build-image.imageName=${IMAGE_TAG}"
                }
            }
            when { branch 'main' }
        }
        stage('Push Docker Image') {
            steps {
                sh "gcloud auth configure-docker ${ARTIFACT_REGISTRY_HOST}"
                sh "docker push ${IMAGE_TAG}"
                sh "gcloud artifacts docker tags add ${IMAGE_TAG} ${DOCKER_ARTIFACT_REGISTRY}/${product_name}-${module_name}:${env.BRANCH_NAME}-latest"
            }
            when { branch 'main' }
        }
        stage('Check Requirements and Deployments') {
            steps {
                build job: 'cessda.cdc.aggregator.deploy/main', parameters: [
                        string(name: 'harvesterImageTag', value: "${env.BRANCH_NAME}-${env.BUILD_NUMBER}")
                ], wait: false
            }
            when { branch 'main' }
        }
    }
}
