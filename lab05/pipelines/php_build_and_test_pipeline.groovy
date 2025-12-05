pipeline {
    agent any

    environment {
        SSH_CRED = 'ssh-agent-key'
        PHP_REPO = 'https://github.com/sonimoo/project_for_lab04.git'
    }

    stages {
        stage('Сборка и тесты на SSH-агенте') {
            steps {
                sshagent(credentials: [SSH_CRED]) {
                    sh '''
                        set -e

                        echo "--- КЛОНИРОВАНИЕ ИЛИ ОБНОВЛЕНИЕ ПРОЕКТА ---"
                        ssh -o StrictHostKeyChecking=no jenkins@ssh-agent "
                            if [ ! -d php-app ]; then
                                git clone ${PHP_REPO} php-app;
                            else
                                cd php-app && git pull --rebase;
                            fi
                        "

                        echo "--- УСТАНОВКА COMPOSER ---"
                        ssh -o StrictHostKeyChecking=no jenkins@ssh-agent "
                            cd php-app &&
                            if [ ! -f composer.phar ]; then
                                php -r \\"copy('https://getcomposer.org/installer', 'composer-setup.php');\\" &&
                                php composer-setup.php &&
                                rm composer-setup.php;
                            fi &&
                            php composer.phar install --no-interaction
                        "

                        echo "--- ЗАПУСК PHPUnit ---"
                        ssh -o StrictHostKeyChecking=no jenkins@ssh-agent "
                            cd php-app &&
                            if [ -x vendor/bin/phpunit ]; then
                                ./vendor/bin/phpunit tests;
                            else
                                php vendor/bin/phpunit tests;
                            fi
                        "
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "PHP проект успешно собран и протестирован!"
        }
        failure {
            echo "Ошибка при сборке или запуске тестов!"
        }
    }
}
