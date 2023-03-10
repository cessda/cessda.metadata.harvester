pipeline {
    options {
        buildDiscarder logRotator(artifactNumToKeepStr: '5', numToKeepStr: '10')
    }

    environment {
        product_name = 'eqb'
        module_name = 'harvester'
        image_tag = "${docker_repo}/${product_name}-${module_name}:${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    }

    agent {
        label 'jnlp-himem'
    }

    stages {
        // Building on main
        stage('Pull SDK Docker Image') {
            agent {
                docker {
                    image 'openjdk:17-jdk'
                    reuseNode true
                }
            }
            stages {
                stage('Build Project') {
                    steps {
                        withMaven {
                            sh './mvnw clean install -Pdocker-compose'
                        }
                    }
                    when { branch 'main' }
                }
                // Not running on main - test only (for PRs and integration branches)
                stage('Test Project') {
                    steps {
                        withMaven {
                            sh './mvnw clean test -Pdocker-compose'
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
                                sh './mvnw sonar:sonar -Pdocker-compose'
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
                sh 'gcloud auth configure-docker'
                withMaven {
                    sh "./mvnw docker:build docker:push -Pdocker-compose -Dimage_tag=${IMAGE_TAG}"
                }
                sh "gcloud container images add-tag ${IMAGE_TAG} ${docker_repo}/${product_name}-${module_name}:${env.BRANCH_NAME}-latest"
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
