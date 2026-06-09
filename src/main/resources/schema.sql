-- ============================================================
-- Schema for beauty_salon_db (идемпотентный)
-- Можно запускать многократно без потери данных
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ------------------------------------------------------------
-- Клиент
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `клиент` (
  `id_Клиента` INT NOT NULL AUTO_INCREMENT,
  `Имя` VARCHAR(255) NULL DEFAULT NULL,
  `Фамилия` VARCHAR(255) NULL DEFAULT NULL,
  `Отчество` VARCHAR(255) NULL DEFAULT NULL,
  `Email` VARCHAR(255) NULL DEFAULT NULL,
  `Телефон` VARCHAR(255) NULL DEFAULT NULL,
  `зашифрованный_телефон` VARCHAR(64) NOT NULL,
  `зашифрованный_email` VARCHAR(64) NULL DEFAULT NULL,
  `telegram_id` VARCHAR(255) NULL DEFAULT NULL,
  `способ_доставки_кода` VARCHAR(50) NULL DEFAULT NULL,
  `согласие_на_обработку_данных` TINYINT(1) NULL DEFAULT 0,
  `дата_согласия` DATETIME NULL DEFAULT NULL,

  PRIMARY KEY (`id_Клиента`),
  UNIQUE INDEX `зашифрованный_телефон_UNIQUE` (`зашифрованный_телефон` ASC) VISIBLE

) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- Услуга
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `услуга` (
  `id_Услуги` INT NOT NULL AUTO_INCREMENT,
  `Название` VARCHAR(45) NULL DEFAULT NULL,
  `Описание` VARCHAR(45) NULL DEFAULT NULL,
  `Стоимость` VARCHAR(45) NULL DEFAULT NULL,
  `Длительность` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id_Услуги`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- Мастер
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `мастер` (
  `id_Мастера` INT NOT NULL AUTO_INCREMENT,
  `id_Услуги` INT NOT NULL,
  `Имя` VARCHAR(45) NULL DEFAULT NULL,
  `Фамилия` VARCHAR(45) NULL DEFAULT NULL,
  `Специализация` VARCHAR(45) NULL DEFAULT NULL,
  `Стаж` VARCHAR(45) NULL DEFAULT NULL,
  `Фото` VARCHAR(45) NULL DEFAULT NULL,
  `Описание` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id_Мастера`),
  INDEX `fk_Мастер_Услуга_idx` (`id_Услуги` ASC) VISIBLE,
  CONSTRAINT `fk_Мастер_Услуга`
    FOREIGN KEY (`id_Услуги`)
    REFERENCES `услуга` (`id_Услуги`)
    ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- Запись
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `запись` (
  `id_Записи` INT NOT NULL AUTO_INCREMENT,
  `id_Клиента` INT NULL DEFAULT NULL,
  `id_Мастера` INT NULL DEFAULT NULL,
  `id_Услуги` INT NULL DEFAULT NULL,
  `Дата` VARCHAR(45) NULL DEFAULT NULL,
  `Время` VARCHAR(45) NULL DEFAULT NULL,
  `Статус` VARCHAR(45) NULL DEFAULT NULL,
  `Пожелания` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`id_Записи`),
  INDEX `fk_Запись_Клиент_idx` (`id_Клиента` ASC) VISIBLE,
  INDEX `fk_Запись_Мастер_idx` (`id_Мастера` ASC) VISIBLE,
  INDEX `fk_Запись_Услуга_idx` (`id_Услуги` ASC) VISIBLE,
  CONSTRAINT `fk_Запись_Клиент`
    FOREIGN KEY (`id_Клиента`)
    REFERENCES `клиент` (`id_Клиента`)
    ON UPDATE CASCADE,
  CONSTRAINT `fk_Запись_Мастер`
    FOREIGN KEY (`id_Мастера`)
    REFERENCES `мастер` (`id_Мастера`)
    ON UPDATE CASCADE,
  CONSTRAINT `fk_Запись_Услуга`
    FOREIGN KEY (`id_Услуги`)
    REFERENCES `услуга` (`id_Услуги`)
    ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- Курс
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `курс` (
  `id_Курса` INT NOT NULL AUTO_INCREMENT,
  `Название` VARCHAR(45) NULL DEFAULT NULL,
  `Описание` VARCHAR(45) NULL DEFAULT NULL,
  `Стоимость` VARCHAR(45) NULL DEFAULT NULL,
  `Длительность` VARCHAR(45) NULL DEFAULT NULL,
  `Дата_начала` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id_Курса`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- Заявка на курс
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `заявка_на_курс` (
  `id_Заявки_курса` INT NOT NULL AUTO_INCREMENT,
  `id_Клиента` INT NULL DEFAULT NULL,
  `id_Курса` INT NULL DEFAULT NULL,
  `Дата` VARCHAR(45) NULL DEFAULT NULL,
  `Статус` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id_Заявки_курса`),
  INDEX `fk_ЗаявкаКурс_Клиент_idx` (`id_Клиента` ASC) VISIBLE,
  INDEX `fk_ЗаявкаКурс_Курс_idx` (`id_Курса` ASC) VISIBLE,
  CONSTRAINT `fk_ЗаявкаКурс_Клиент`
    FOREIGN KEY (`id_Клиента`)
    REFERENCES `клиент` (`id_Клиента`),
  CONSTRAINT `fk_ЗаявкаКурс_Курс`
    FOREIGN KEY (`id_Курса`)
    REFERENCES `курс` (`id_Курса`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- Соискатель
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `соискатель` (
  `id_Соискателя` INT NOT NULL AUTO_INCREMENT,
  `id_Клиента` INT NULL DEFAULT NULL,
  `Вакансия` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id_Соискателя`),
  INDEX `fk_Соискатель_Клиент_idx` (`id_Клиента` ASC) VISIBLE,
  CONSTRAINT `fk_Соискатель_Клиент`
    FOREIGN KEY (`id_Клиента`)
    REFERENCES `клиент` (`id_Клиента`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- Заявка на работу
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `заявка_на_работу` (
  `id_Заявки` INT NOT NULL AUTO_INCREMENT,
  `id_Соискателя` INT NOT NULL,
  `Дата` VARCHAR(45) NULL DEFAULT NULL,
  `Статус` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id_Заявки`),
  INDEX `fk_Заявка_Соискатель_idx` (`id_Соискателя` ASC) VISIBLE,
  CONSTRAINT `fk_Заявка_Соискатель`
    FOREIGN KEY (`id_Соискателя`)
    REFERENCES `соискатель` (`id_Соискателя`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- Отзыв (исправленная колонка status)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `отзыв` (
  `id_Отзыва` INT NOT NULL AUTO_INCREMENT,
  `id_Клиента` INT NULL DEFAULT NULL,
  `Оценка` VARCHAR(45) NULL DEFAULT NULL,
  `Комментарий` VARCHAR(45) NULL DEFAULT NULL,
  `Дата` VARCHAR(45) NULL DEFAULT NULL,
  `статус` VARCHAR(45) NULL DEFAULT 'PENDING',

  PRIMARY KEY (`id_Отзыва`),
  INDEX `fk_Отзыв_Клиент_idx` (`id_Клиента` ASC) VISIBLE,
  CONSTRAINT `fk_Отзыв_Клиент`
    FOREIGN KEY (`id_Клиента`)
    REFERENCES `клиент` (`id_Клиента`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- Пост (блог)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `пост` (
  `id_Поста` INT NOT NULL AUTO_INCREMENT,
  `Заголовок` VARCHAR(255) NULL DEFAULT NULL,
  `Дата` VARCHAR(45) NULL DEFAULT NULL,
  `Описание` TEXT NULL DEFAULT NULL,
  `Фото` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id_Поста`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- ------------------------------------------------------------
-- Audit Log
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `журнал_действий` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `id_Клиента` INT NULL DEFAULT NULL,
  `действие` VARCHAR(64) NOT NULL,
  `детали_действия` VARCHAR(512) NULL DEFAULT NULL,
  `дата_действия` DATETIME NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;


-- ============================================================
-- Образцы данных (только если таблицы пустые)
-- ============================================================
INSERT IGNORE INTO `услуга` (`id_Услуги`, `Название`, `Описание`, `Стоимость`, `Длительность`) VALUES 
(1, 'Кератиновое выпрямление', 'Кератиновое выпрямление волос', '200', '2-3 часа'),
(2, 'Ботокс', 'Ботокс для волос', '180', '2 часа'),
(3, 'Холодное восстановление', 'Холодное восстановление волос', '220', '2-3 часа'),
(4, 'Обучение/Курсы', 'Обучение мастеров', '300', '2 дня');

INSERT IGNORE INTO `курс` (`Название`, `Описание`, `Стоимость`, `Длительность`, `Дата_начала`) VALUES 
('Мастер кератина', 'Курс по кератиновому выпрямлению', '500', '3 дня', 'По запросу'),
('Ботокс для волос', 'Курс по ботоксу', '400', '2 дня', 'По запросу');

INSERT IGNORE INTO `мастер` (`id_Мастера`, `id_Услуги`, `Имя`, `Фамилия`, `Специализация`, `Стаж`, `Фото`, `Описание`) VALUES 
(1, 1, 'Карина', 'URBASH', 'Кератин, Ботокс', '5 лет', 'karina.png', 'Создательница URBASH.hair');

SET FOREIGN_KEY_CHECKS = 1;