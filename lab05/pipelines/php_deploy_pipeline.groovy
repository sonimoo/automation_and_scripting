pipeline {
    agent any

    environment {
        PHP_REPO = "https://github.com/sonimoo/project_for_lab04.git"
        PHP_APP_DIR = "php-app"
    }

    stages {
        stage('Clone PHP Project') {
            steps {
                echo "Клонируем репозиторий PHP проекта..."
                sh '''
                    rm -rf ${PHP_APP_DIR}
                    git clone ${PHP_REPO} ${PHP_APP_DIR}
                '''
            }
        }

        stage('Deploy to Test Server with Ansible') {
            steps {
                echo "Размещаем проект на тестовом сервере через Ansible..."

                sshagent(credentials: ['ssh-agent-key']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no jenkins@ansible-agent "
                            cd /home/ansible/ansible &&
                            ansible-playbook -i hosts.ini deploy_php_app.yml
                        "
                    '''
                }
            }
        }
    }

    post {
        failure {
            echo "Ошибка при размещении PHP проекта!"
        }
        success {
            echo "PHP проект успешно развернут на тестовом сервере!"
        }
    }
}
