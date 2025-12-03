pipeline {
    agent any

    environment {
        PHP_REPO = "https://github.com/sonimoo/project_for_lab04.git"
    }

    stages {

        stage('Clone PHP Project') {
            steps {
                echo "Клонируем репозиторий PHP проекта..."
                sh """
                rm -rf php-app
                git clone ${PHP_REPO} php-app
                """
            }
        }

        stage('Deploy to Test Server with Ansible') {
            steps {
                echo "Размещаем проект на тестовом сервере через Ansible..."

                sshagent(credentials: ['ssh-agent-key']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no jenkins@ssh-agent "
                            cd /home/jenkins/ansible &&
                            ansible-playbook -i hosts.ini deploy_php_app.yml
                        "
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "PHP проект успешно размещён на тестовом сервере!"
        }
        failure {
            echo "Ошибка при размещении PHP проекта!"
        }
    }
}
