pipeline {
    options {
        ansiColor('xterm')
        buildDiscarder logRotator(artifactNumToKeepStr: '5', numToKeepStr: '10')
    }

    environment {
        product_name = "eqb"
        module_name = "harvester"
        image_tag = "${docker_repo}/${product_name}-${module_name}:${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    }

    agent any

    stages {
        // Building on master
        stage('Pull SDK Docker Image') {
            agent {
                docker {
                    image 'maven:3-jdk-11'
                    reuseNode true
                }
            }
            stages {
                stage('Build Project') {
                    steps {
                        withMaven {
                            sh "$MVN_CMD clean install -DbuildNumber=${env.BUILD_NUMBER} -Pdocker-compose"
                        }
                    }
                    when { branch 'master' }
                }
                // Not running on master - test only (for PRs and integration branches)
                stage('Test Project') {
                    steps {
                        withMaven {
                            sh '$MVN_CMD clean test -Pdocker-compose'
                        }
                    }
                    when { not { branch 'master' } }
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
                                sh "$MVN_CMD sonar:sonar -DbuildNumber=${env.BUILD_NUMBER} -Pdocker-compose"
                            }
                        }
                    }
                    when { branch 'master' }
                }
            }
        }
        stage("Get Sonar Quality Gate") {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: false
                }
            }
            when { branch 'master' }
        }
        stage('Build and Push Docker Image') {
            steps {
                sh 'gcloud auth configure-docker'
                withMaven {
                    sh "mvn docker:build docker:push -DbuildNumber=${env.BUILD_NUMBER} -Pdocker-compose -Dimage_tag=${IMAGE_TAG}"
                }
                sh("gcloud container images add-tag ${IMAGE_TAG} ${docker_repo}/${product_name}-${module_name}:${env.BRANCH_NAME}-latest")
            }
            when { branch 'master' }
        }
        stage('Check Requirements and Deployments') {
            steps {
                dir('./infrastructure/gcp/') {
                    build job: 'cessda.eqb.deploy/master', parameters: [string(name: 'harvester_image_tag', value: "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"), string(name: 'module', value: 'harvester')], wait: false
                }
            }
            when { branch 'master' }
        }
    }
}