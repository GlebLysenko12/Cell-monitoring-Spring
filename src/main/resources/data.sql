-- 1. Сначала создаем таблицы, если они не существуют
-- Таблица users
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'LAB_TECH',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Таблица experiments
CREATE TABLE IF NOT EXISTS experiments (
                                           id BIGSERIAL PRIMARY KEY,
                                           name VARCHAR(200) NOT NULL,
    description TEXT,
    cell_type VARCHAR(100) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',

    -- EnvironmentParams
    target_temperature DECIMAL(5,2),
    target_humidity DECIMAL(5,2),
    target_co2 DECIMAL(5,2),
    measurement_interval INTEGER,

    -- ShootingParams
    shoot_interval_minutes INTEGER,
    number_of_positions INTEGER,
    magnification DECIMAL(5,2),
    camera_type VARCHAR(100),

    -- BoundaryValues
    confluency_threshold_high DECIMAL(5,2),
    confluency_threshold_low DECIMAL(5,2),
    cell_count_alert INTEGER,
    size_deviation_max DECIMAL(5,2),

    -- Foreign key
    user_id BIGINT,

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
    );

-- Таблица cell_metrics
CREATE TABLE IF NOT EXISTS cell_metrics (
                                            id BIGSERIAL PRIMARY KEY,
                                            timestamp TIMESTAMP NOT NULL,
                                            confluency DECIMAL(6,2) NOT NULL,
    cell_count INTEGER NOT NULL,
    avg_cell_size DECIMAL(8,2),
    cell_density DECIMAL(10,4),

    temperature DECIMAL(5,2),
    humidity DECIMAL(5,2),
    co2_level DECIMAL(5,2),

    experiment_id BIGINT NOT NULL,

    CONSTRAINT fk_experiment_metrics FOREIGN KEY (experiment_id)
    REFERENCES experiments(id) ON DELETE CASCADE
    );

-- Таблица image_data
CREATE TABLE IF NOT EXISTS image_data (
                                          id BIGSERIAL PRIMARY KEY,
                                          filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    capture_time TIMESTAMP NOT NULL,
    image_number INTEGER NOT NULL,

    analysis_result_json TEXT,
    confluency DECIMAL(6,2),
    cell_count INTEGER,
    avg_cell_size DECIMAL(8,2),

    experiment_id BIGINT NOT NULL,

    CONSTRAINT fk_experiment_images FOREIGN KEY (experiment_id)
    REFERENCES experiments(id) ON DELETE CASCADE,
    CONSTRAINT unique_image_per_experiment UNIQUE (experiment_id, image_number)
    );

-- 2. Очистка данных (только если таблицы существуют)
DO $$
BEGIN
    -- Проверяем существование таблиц перед очисткой
    IF EXISTS (SELECT FROM pg_tables WHERE tablename = 'cell_metrics') THEN
DELETE FROM cell_metrics;
END IF;

    IF EXISTS (SELECT FROM pg_tables WHERE tablename = 'image_data') THEN
DELETE FROM image_data;
END IF;

    IF EXISTS (SELECT FROM pg_tables WHERE tablename = 'experiments') THEN
DELETE FROM experiments;
END IF;

    IF EXISTS (SELECT FROM pg_tables WHERE tablename = 'users') THEN
DELETE FROM users;
END IF;
END $$;

-- 3. Вставка тестовых данных
-- Вставка тестового пользователя
INSERT INTO users (id, username, email, password, full_name, role, created_at)
VALUES
    (1, 'lab_technician', 'lab@example.com', '$2a$10$YourHashedPassword', 'Иван Петров', 'LAB_TECH', NOW())
    ON CONFLICT (id) DO NOTHING;

-- Вставка 20 тестовых экспериментов
INSERT INTO experiments (
    id, name, description, cell_type, start_date, end_date, status, user_id,
    target_temperature, target_humidity, target_co2, measurement_interval,
    shoot_interval_minutes, number_of_positions, magnification, camera_type,
    confluency_threshold_high, confluency_threshold_low, cell_count_alert, size_deviation_max
) VALUES
      (1, 'Эксперимент с клетками HeLa №1', 'Исследование роста клеток HeLa', 'HeLa', NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days', 'COMPLETED', 1, 37.0, 85.0, 5.0, 30, 60, 4, 20.0, 'Sony Alpha', 85.0, 30.0, 10000, 15.0),
      (2, 'Эксперимент с клетками HEK293 №2', 'Исследование роста клеток HEK293', 'HEK293', NOW() - INTERVAL '9 days', NOW() - INTERVAL '8 days', 'COMPLETED', 1, 37.1, 86.0, 5.0, 30, 60, 4, 20.0, 'Sony Alpha', 85.0, 30.0, 10000, 15.0),
      (3, 'Эксперимент с клетками MCF-7 №3', 'Исследование роста клеток MCF-7', 'MCF-7', NOW() - INTERVAL '8 days', NOW() - INTERVAL '7 days', 'COMPLETED', 1, 37.2, 85.0, 5.5, 30, 60, 4, 20.0, 'Sony Alpha', 85.0, 30.0, 10000, 15.0),
      (4, 'Эксперимент с клетками HeLa №4', 'Исследование роста клеток HeLa', 'HeLa', NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days', 'INTERRUPTED', 1, 37.0, 87.0, 5.0, 30, 60, 4, 20.0, 'Sony Alpha', 85.0, 30.0, 10000, 15.0),
      (5, 'Эксперимент с клетками HEK293 №5', 'Исследование роста клеток HEK293', 'HEK293', NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days', 'COMPLETED', 1, 37.1, 85.0, 5.0, 30, 60, 4, 20.0, 'Sony Alpha', 85.0, 30.0, 10000, 15.0),
      (6, 'Эксперимент с клетками MCF-7 №6', 'Исследование роста клеток MCF-7', 'MCF-7', NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days', 'FAILED', 1, 37.3, 86.0, 5.5, 30, 60, 4, 20.0, 'Sony Alpha', 85.0, 30.0, 10000, 15.0),
      (7, 'Эксперимент с клетками HeLa №7', 'Исследование роста клеток HeLa', 'HeLa', NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days', 'COMPLETED', 1, 37.0, 85.0, 5.0, 30, 60, 4, 20.0, 'Sony Alpha', 85.0, 30.0, 10000, 15.0),
      (8, 'Эксперимент с клетками HEK293 №8', 'Исследование роста клеток HEK293', 'HEK293', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', 'COMPLETED', 1, 37.1, 88.0, 5.0, 30, 60, 4, 20.0, 'Sony Alpha', 85.0, 30.0, 10000, 15.0),
      (9, 'Эксперимент с клетками MCF-7 №9', 'Исследование роста клеток MCF-7', 'MCF-7', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 days', 'ACTIVE', 1, 37.2, 85.0, 5.5, 30, 60, 4, 20.0, 'Sony Alpha', 85.0, 30.0, 10000, 15.0),
      (10, 'Эксперимент с клетками HeLa №10', 'Исследование роста клеток HeLa', 'HeLa', NOW() - INTERVAL '1 days', NOW(), 'ACTIVE', 1, 37.0, 86.0, 5.0, 30, 60, 4, 20.0, 'Sony Alpha', 85.0, 30.0, 10000, 15.0)
    ON CONFLICT (id) DO NOTHING;

-- Вставка метрик для первого эксперимента
INSERT INTO cell_metrics (timestamp, confluency, cell_count, avg_cell_size, cell_density, temperature, humidity, co2_level, experiment_id) VALUES
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '1 hour', 20.5, 1200, 15.2, 12.0, 37.0, 85.0, 5.0, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '2 hour', 25.3, 1500, 15.5, 15.0, 37.1, 85.2, 5.1, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '3 hour', 30.7, 1800, 15.8, 18.0, 37.0, 85.1, 5.0, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '4 hour', 35.2, 2100, 16.0, 21.0, 37.2, 85.3, 5.2, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '5 hour', 40.8, 2500, 16.2, 25.0, 37.1, 85.0, 5.1, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '6 hour', 45.6, 2800, 16.5, 28.0, 37.0, 85.2, 5.0, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '7 hour', 50.3, 3100, 16.8, 31.0, 37.3, 85.1, 5.3, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '8 hour', 55.9, 3500, 17.0, 35.0, 37.2, 85.4, 5.2, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '9 hour', 60.5, 3800, 17.2, 38.0, 37.1, 85.0, 5.1, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '10 hour', 65.8, 4200, 17.5, 42.0, 37.0, 85.3, 5.0, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '11 hour', 70.2, 4500, 17.8, 45.0, 37.2, 85.2, 5.2, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '12 hour', 75.6, 4900, 18.0, 49.0, 37.1, 85.1, 5.1, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '13 hour', 80.3, 5200, 18.2, 52.0, 37.3, 85.4, 5.3, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '14 hour', 85.7, 5600, 18.5, 56.0, 37.2, 85.0, 5.2, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '15 hour', 90.1, 5900, 18.8, 59.0, 37.1, 85.3, 5.1, 1),
                                                                                                                                               (NOW() - INTERVAL '10 days' + INTERVAL '16 hour', 95.5, 6300, 19.0, 63.0, 37.0, 85.2, 5.0, 1)
    ON CONFLICT DO NOTHING;

-- Вставка изображений для первого эксперимента
INSERT INTO image_data (filename, file_path, capture_time, image_number, analysis_result_json, confluency, cell_count, avg_cell_size, experiment_id) VALUES
                                                                                                                                                         ('experiment_1_image_1.tiff', '/data/images/experiment_1/', NOW() - INTERVAL '10 days' + INTERVAL '1 hour', 1, '{"segmentation_quality": 0.95, "artifacts_detected": 1}', 20.5, 1200, 15.2, 1),
                                                                                                                                                         ('experiment_1_image_2.tiff', '/data/images/experiment_1/', NOW() - INTERVAL '10 days' + INTERVAL '4 hour', 2, '{"segmentation_quality": 0.96, "artifacts_detected": 0}', 35.2, 2100, 16.0, 1),
                                                                                                                                                         ('experiment_1_image_3.tiff', '/data/images/experiment_1/', NOW() - INTERVAL '10 days' + INTERVAL '7 hour', 3, '{"segmentation_quality": 0.94, "artifacts_detected": 2}', 50.3, 3100, 16.8, 1),
                                                                                                                                                         ('experiment_1_image_4.tiff', '/data/images/experiment_1/', NOW() - INTERVAL '10 days' + INTERVAL '10 hour', 4, '{"segmentation_quality": 0.97, "artifacts_detected": 1}', 65.8, 4200, 17.5, 1),
                                                                                                                                                         ('experiment_1_image_5.tiff', '/data/images/experiment_1/', NOW() - INTERVAL '10 days' + INTERVAL '13 hour', 5, '{"segmentation_quality": 0.95, "artifacts_detected": 0}', 80.3, 5200, 18.2, 1),
                                                                                                                                                         ('experiment_1_image_6.tiff', '/data/images/experiment_1/', NOW() - INTERVAL '10 days' + INTERVAL '16 hour', 6, '{"segmentation_quality": 0.98, "artifacts_detected": 1}', 95.5, 6300, 19.0, 1)
    ON CONFLICT DO NOTHING;

-- Обновляем sequence для корректной работы автоинкремента
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('experiments_id_seq', (SELECT MAX(id) FROM experiments));
SELECT setval('cell_metrics_id_seq', (SELECT MAX(id) FROM cell_metrics));
SELECT setval('image_data_id_seq', (SELECT MAX(id) FROM image_data));