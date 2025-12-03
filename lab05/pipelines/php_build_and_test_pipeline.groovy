pipeline {
    agent any

    environment {
        SSH_CRED_ID = 'ssh-agent-key'        // ID credentials в Jenkins (мы уже создавали)
        SSH_HOST    = 'jenkins@ssh-agent'    // пользователь и контейнер ssh-agent
        PHP_REPO    = 'https://github.com/sonimoo/project_for_lab04'  // ← СЮДА свой URL
        APP_DIR     = 'php-app'              // папка проекта на ssh-agent
    }

    stages {
        stage('Сборка и тесты на SSH-агенте') {
            steps {
                sshagent(credentials: [SSH_CRED_ID]) {
                    sh """
                        set -e

                        # Клонируем/обновляем репозиторий на ssh-agent
                        ssh -o StrictHostKeyChecking=no ${SSH_HOST} ' \
                            if [ ! -d "${APP_DIR}" ]; then \
                                git clone ${PHP_REPO} ${APP_DIR}; \
                            else \
                                cd ${APP_DIR} && git pull --rebase; \
                            fi \
                        '

                        # Устанавливаем зависимости через Composer
                        ssh -o StrictHostKeyChecking=no ${SSH_HOST} ' \
                            cd ${APP_DIR} && \
                            if [ ! -f composer.phar ]; then \
                                php -r "copy(\\"https://getcomposer.org/installer\\", \\"composer-setup.php\\");" && \
                                php composer-setup.php && \
                                rm composer-setup.php; \
                            fi && \
                            php composer.phar install --no-interaction \
                        '

                        # Запускаем модульные тесты
                        ssh -o StrictHostKeyChecking=no ${SSH_HOST} ' \
                            cd ${APP_DIR} && \
                            if [ -x vendor/bin/phpunit ]; then \
                                ./vendor/bin/phpunit; \
                            else \
                                php vendor/bin/phpunit; \
                            fi \
                        '
                    """
                }
            }
        }
    }

    post {
        success {
            echo 'Тесты прошли успешно на ssh-agent'
        }
        failure {
            echo 'Ошибка при сборке или тестировании PHP проекта'
        }
    }
}
