pipeline {
    agent any

    stages {
        stage('Сборка и тесты на SSH-агенте') {
            steps {
                // Используем SSH-credentials с ID "jenkins"
                sshagent(credentials: ['jenkins']) {
                    sh '''
                        set -e

                        echo "==== Клонируем или обновляем PHP-проект на ssh-agent ===="
                        ssh -o StrictHostKeyChecking=no jenkins@ssh-agent \
                            "if [ ! -d \"php-app\" ]; then \
                                git clone https://github.com/sonimoo/project_for_lab04.git php-app; \
                             else \
                                cd php-app && git pull --rebase; \
                             fi"

                        echo "==== Устанавливаем зависимости через Composer ===="
                        ssh -o StrictHostKeyChecking=no jenkins@ssh-agent \
                            "cd php-app && \
                             if [ ! -f composer.phar ]; then \
                                 php -r \"copy('https://getcomposer.org/installer', 'composer-setup.php');\" && \
                                 php composer-setup.php && \
                                 rm composer-setup.php; \
                             fi && \
                             php composer.phar install --no-interaction"

                        echo "==== Подготавливаем helpers.php для тестов ===="
                        ssh -o StrictHostKeyChecking=no jenkins@ssh-agent \
                            "cd php-app && \
                             if [ ! -f helpers.php ] && [ -f src/helpers.php ]; then \
                                 cp src/helpers.php helpers.php; \
                             fi"

                        echo "==== Запускаем PHPUnit-тесты ===="
                        ssh -o StrictHostKeyChecking=no jenkins@ssh-agent \
                            "cd php-app && \
                             if [ -x vendor/bin/phpunit ]; then \
                                 ./vendor/bin/phpunit tests; \
                             else \
                                 php vendor/bin/phpunit tests; \
                             fi"
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'PHP проект успешно собран и протестирован '
        }
        failure {
            echo 'Ошибка при сборке или тестировании PHP проекта '
        }
    }
}
