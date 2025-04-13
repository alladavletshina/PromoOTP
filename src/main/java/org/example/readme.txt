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


Limpa_5@rambler.ru
KYt-49L-2W7-TKn
