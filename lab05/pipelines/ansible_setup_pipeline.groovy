pipeline {
    agent any

    environment {
        ANSIBLE_CRED = 'ansible-agent-key'  
    }

    stages {
        stage('Запуск Ansible Playbook') {
            steps {
                sshagent(credentials: [ANSIBLE_CRED]) {
                    sh '''
                        set -e

                        ssh -o StrictHostKeyChecking=no ansible@ansible-agent "
                            cd /ansible &&
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
