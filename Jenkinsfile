pipeline {
    options {
        buildDiscarder logRotator(artifactNumToKeepStr: '5', numToKeepStr: '10')
    }

    environment {
        product_name = 'cdc'
        module_name = 'harvester'
        image_tag = "${DOCKER_ARTIFACT_REGISTRY}/${product_name}-${module_name}:${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    }

    agent {
        label 'jnlp-himem'
    }

    stages {
        // Building on main
        stage('Pull SDK Docker Image') {
            agent {
                docker {
                    image 'eclipse-temurin:21'
                    args '-v $HOME/.m2:/.m2'
                    reuseNode true
                }
            }
            stages {
                stage('Build Project') {
                    steps {
                        withMaven {
                            sh './mvnw clean verify'
                        }
                    }
                    when { branch 'main' }
                }
                // Not running on main - test only (for PRs and integration branches)
                stage('Test Project') {
                    steps {
                        withMaven {
                            sh './mvnw clean test'
                        }
                    }
                    when { not { branch 'main' } }
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
        stage('Build and Push Docker Image') {
            steps {
                sh "gcloud auth configure-docker ${ARTIFACT_REGISTRY_HOST}"
                withMaven {
                    sh "./mvnw jib:build -Dimage=${IMAGE_TAG}"
                }
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
