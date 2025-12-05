pipeline {
    agent any

    environment {
        ANSIBLE_CRED = 'ansible-agent-key'  
    }

    stages {

        stage('Запуск Ansible Deploy Playbook') {
            steps {
                sshagent(credentials: [ANSIBLE_CRED]) {
                    sh '''
                        set -e

                        # Запускаем playbook деплоя на ansible-agent
                        ssh -o StrictHostKeyChecking=no ansible@ansible-agent "
                            ansible-playbook -i /ansible/hosts.ini /ansible/deploy_php.yml
                        "
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'PHP проект успешно развернут на тестовом сервере!'
        }
        failure {
            echo 'Ошибка при деплое PHP проекта!'
        }
    }
}
