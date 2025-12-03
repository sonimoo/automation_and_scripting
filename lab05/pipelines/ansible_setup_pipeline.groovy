pipeline {
    agent any

    environment {
        ANSIBLE_CRED = 'ansible-agent-key'  
    }

    stages {

        stage('Настройка тестового сервера через Ansible') {
            steps {
                sshagent(credentials: [ANSIBLE_CRED]) {
                    sh '''
                        set -e

                        # Заходим на ansible-agent и запускаем playbook
                        ssh -o StrictHostKeyChecking=no ansible@ansible-agent "
                            cd /home/ansible/ansible &&
                            ansible-playbook -i hosts.ini setup_test_server.yml
                        "
                    '''
                }
            }
        }
    }

    post {
        failure {
            echo 'Ошибка при выполнении Ansible playbook'
        }
        success {
            echo 'Ansible playbook успешно выполнен!'
        }
    }
}
