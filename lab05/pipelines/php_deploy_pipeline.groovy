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

                // ВНИМАНИЕ: используем ключ ansible-agent-key !!!
                sshagent(credentials: ['ansible-agent-key']) {

                    sh '''
                        ssh -o StrictHostKeyChecking=no ansible@ansible-agent \
                        "cd /home/ansible/ansible && \
                         ansible-playbook -i hosts.ini deploy_php_app.yml"
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
