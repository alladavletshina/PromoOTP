Архитектура сервиса
Слой API (Контроллеры):
UserController — для регистрации и входа пользователей.
AdminController — для управления конфигурацией OTP-кодов и просмотра списка пользователей.
OperationController — для инициализации защищенных операций и проверки OTP-кодов.
Слой сервисов:
UserService — для управления пользователями.
OtpService — для генерации, отправки и проверки OTP-кодов.
OperationService — для работы с защищенными операциями.
Слой DAO:
UserDao — для взаимодействия с таблицей пользователей.
OtpDao — для хранения и управления OTP-кодами.
OperationDao — для работы с защищенными операциями.
Модели данных:
User — представляет пользователя.
OtpCode — хранит информацию об OTP-коде.
Operation — описывает защищенную операцию.
Утилиты:
EmailNotificationService — для отправки OTP-кодов по электронной почте.
SmsSender — для отправки SMS с использованием эмулятора.
TelegramBot — для отправки сообщений через Telegram.
FileSaver — для сохранения OTP-кодов в файл.

База данных

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'USER'))
);

CREATE TABLE otp_config (
    id SERIAL PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL
);


CREATE TABLE otp_codes (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL, -- Добавляем внешний ключ на таблицу users
    operation_id INT,
    otp_code VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'USED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    description TEXT NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE operations (
    id SERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    user_id BIGINT REFERENCES users(id),
    otp_code_id BIGINT REFERENCES otp_codes(id)
);

Администратор - ALLA
Пароль - $2y$12$d.dQdOUBX5PnVU.xfD.CVeB9TQ3.yqSuEuxw9XEVN4Ys5lHOKeM.


limpa_5@rambler.ru
KL1456t8

./startsmppsim.bat

cvbsdjlsslabot

Done! Congratulations on your new bot. You will find it at t.me/cvbsdjlsslabot. You can now add a description, about section and profile picture for your bot, see /help for a list of commands. By the way, when you've finished creating your cool bot, ping our Bot Support if you want a better username for it. Just make sure the bot is fully operational before you do this.

Use this token to access the HTTP API:
7931065669:AAH9ZQcPXkn-x1AM1u6AEUuVMPiGFY7fkQo
Keep your token secure and store it safely, it can be used by anyone to control your bot.

For a description of the Bot API, see this page: https://core.telegram.org/bots/api
