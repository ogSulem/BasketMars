# BasketMars — Мобильная баскетбольная игра для Android

**Автор:** Яруллин Марсель, группа 09-253, КФУ  
**Платформа:** Android (minSdk 21 / Android 5.0+, targetSdk 34)  
**Язык:** Java 17  
**Система сборки:** Gradle 8  

---

## Содержание

1. [Описание проекта](#1-описание-проекта)
2. [Архитектура приложения](#2-архитектура-приложения)
3. [Структура проекта](#3-структура-проекта)
4. [Компоненты и модули](#4-компоненты-и-модули)
   - 4.1 [Слой представления (UI)](#41-слой-представления-ui)
   - 4.2 [Игровой движок (GameView)](#42-игровой-движок-gameview)
   - 4.3 [Слой данных (Data Layer)](#43-слой-данных-data-layer)
   - 4.4 [Сетевой слой (Network Layer)](#44-сетевой-слой-network-layer)
   - 4.5 [Музыкальный плеер (MusicPlayer)](#45-музыкальный-плеер-musicplayer)
5. [Игровые режимы](#5-игровые-режимы)
6. [Игровая механика и физика](#6-игровая-механика-и-физика)
7. [Система достижений и инвентарь](#7-система-достижений-и-инвентарь)
8. [Модель данных](#8-модель-данных)
9. [Технологический стек и зависимости](#9-технологический-стек-и-зависимости)
10. [Пользовательский интерфейс](#10-пользовательский-интерфейс)
11. [Сборка и запуск](#11-сборка-и-запуск)
12. [Текущее состояние и ограничения](#12-текущее-состояние-и-ограничения)
13. [Перспективы развития](#13-перспективы-развития)

---

## 1. Описание проекта

**BasketMars** — это мобильная аркадная игра для платформы Android, реализующая механику баскетбольного броска. Игрок управляет мячом при помощи жеста свайп, бросая его в кольцо. Приложение разработано на языке Java с использованием нативного Android SDK и не зависит от сторонних игровых движков.

### Цели и задачи проекта

- Реализовать увлекательную одиночную мобильную игру с несколькими режимами сложности.
- Разработать систему персонализации (скины мяча, кольца, фон).
- Добавить соревновательный аспект: локальный лидерборд и режим дуэли с соперником.
- Применить современные архитектурные практики Android-разработки (Room, RecyclerView, BottomSheetDialog, WebSocket).
- Обеспечить поддержку широкого диапазона устройств (Android 5.0–14).

---

## 2. Архитектура приложения

Приложение построено по **многослойной (layered) архитектуре**, типичной для Android-приложений:

```
┌──────────────────────────────────────────────────────────┐
│                     UI Layer (View)                       │
│  MainActivity · GameActivity · LeaderboardActivity        │
│  InventoryActivity · AchievementsActivity · Settings     │
├──────────────────────────────────────────────────────────┤
│               Game Engine (GameView)                      │
│  Физика мяча · Отрисовка · Управление жестами            │
│  Анимации · Комбо-система · HUD                          │
├──────────────────────────────────────────────────────────┤
│              Network Layer (net/)                         │
│  MatchClient (interface) · OnlineMatchClient (WebSocket)  │
│  DemoDuelClient (AI-бот)                                 │
├──────────────────────────────────────────────────────────┤
│               Data Layer (data/)                          │
│  Room Database · LeaderboardRepository                    │
│  LeaderboardEntry · PlayerStats                          │
└──────────────────────────────────────────────────────────┘
```

**Ключевые принципы:**

- **Разделение ответственности:** каждый слой отвечает только за свои задачи.
- **Repository Pattern:** `LeaderboardRepository` инкапсулирует все операции с БД, скрывает потоки выполнения.
- **Interface-driven networking:** `MatchClient` — интерфейс, позволяющий легко подменять реализации (AI-бот ↔ реальный сервер).
- **Single-process lifecycle management:** `BasketballGameApp` управляет общими ресурсами (БД, музыка).

---

## 3. Структура проекта

```
BasketMars/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/basketballgame/
│   │   │   ├── BasketballGameApp.java        # Application-класс
│   │   │   ├── MainActivity.java             # Главное меню
│   │   │   ├── GameActivity.java             # Экран игры
│   │   │   ├── GameView.java                 # Игровой движок (custom View)
│   │   │   ├── GameMode.java                 # Enum режимов игры
│   │   │   ├── InventoryActivity.java        # Инвентарь (мячи, кольца, фоны)
│   │   │   ├── AchievementsActivity.java     # Достижения
│   │   │   ├── LeaderboardActivity.java      # Таблица лидеров
│   │   │   ├── LeaderboardAdapter.java       # RecyclerView адаптер
│   │   │   ├── SettingsActivity.java         # Настройки
│   │   │   ├── MusicPlayer.java              # Фоновая музыка
│   │   │   ├── ResetProgress.java            # Сброс прогресса
│   │   │   ├── data/
│   │   │   │   ├── AppDatabase.java          # Room база данных
│   │   │   │   ├── LeaderboardEntry.java     # Entity: запись лидерборда
│   │   │   │   ├── LeaderboardDao.java       # DAO: запросы к лидерборду
│   │   │   │   ├── LeaderboardRepository.java# Repository
│   │   │   │   ├── PlayerStats.java          # Entity: статистика игрока
│   │   │   │   └── PlayerStatsDao.java       # DAO: статистика
│   │   │   └── net/
│   │   │       ├── MatchClient.java          # Интерфейс сетевого клиента
│   │   │       ├── OnlineMatchClient.java    # WebSocket-клиент (OkHttp)
│   │   │       └── DemoDuelClient.java       # AI-бот для режима дуэли
│   │   ├── res/
│   │   │   ├── drawable/                     # Графические ресурсы, кнопки, фоны
│   │   │   ├── layout/                       # XML-разметки экранов
│   │   │   ├── raw/                          # MP3-треки фоновой музыки
│   │   │   ├── mipmap-*/                     # Иконки приложения
│   │   │   └── values/                       # Стили и темы
│   │   └── AndroidManifest.xml
│   └── build.gradle                          # Конфигурация модуля
├── build.gradle                              # Корневая конфигурация
├── settings.gradle
└── gradle.properties
```

---

## 4. Компоненты и модули

### 4.1 Слой представления (UI)

#### `BasketballGameApp`
Класс-наследник `Application`. Инициализируется один раз при запуске процесса:
- Создаёт единственный экземпляр `AppDatabase` (Room).
- Создаёт `LeaderboardRepository`.
- Заполняет БД демонстрационными данными при первом запуске (`seedDemoDataIfEmpty`).
- Управляет жизненным циклом фоновой музыки через `ActivityLifecycleCallbacks`: пауза при уходе в фон, возобновление при возврате на передний план.

#### `MainActivity`
Главный экран приложения (точка входа). Содержит:
- Кнопку **«Играть»** — открывает `BottomSheetDialog` выбора режима игры.
- Кнопку **«Лидерборд»** — переход на `LeaderboardActivity`.
- Кнопку **«Инвентарь»** — переход на `InventoryActivity`.
- Кнопку **«Достижения»** — переход на `AchievementsActivity`.
- Кнопку **«Настройки»** — переход на `SettingsActivity`.
- В `onResume` применяет выбранный фоновый градиент из `SharedPreferences`.

#### `GameActivity`
Хост-активити для игрового экрана. Ответственности:
- Создаёт `GameView` и передаёт ему выбранный `GameMode`.
- Добавляет кнопку «Назад» и панель управления музыкой (`music_controls`).
- Инициализирует сетевой клиент (`initOnlineClient`) для режима дуэли.
- Реагирует на завершение игровой сессии (`handleSessionComplete`): сохраняет результат в БД, обновляет статистику, показывает диалог итогов (`showResultsDialog`).
- Сохраняет рекорд при выходе в аркадном режиме (`maybeSaveArcadeRun`).

#### `LeaderboardActivity`
Экран таблицы лидеров:
- Отображает статистику игрока (лучший результат в каждом режиме).
- Переключение между тремя вкладками (Аркада / На время / Дуэли) через `TabLayout`.
- Список ТОП-20 результатов в `RecyclerView`.
- Золото/серебро/бронза для первых трёх мест.

#### `InventoryActivity`
Экран инвентаря с тремя секциями:
- **Мячи**: Обычный / Стрит / Легенда.
- **Сетки (кольца)**: Обычная / Стрит / Легенда.
- **Фоны**: Розовый / Синий / Оранжевый.

Предметы, не разблокированные ещё достижениями, отображаются с полупрозрачным overlay и диагональной полосой. Выбор сохраняется в `SharedPreferences`.

#### `AchievementsActivity`
Экран со списком из 8 достижений. Каждое достижение имеет:
- Пороговый счёт для разблокировки.
- Описание и иконку награды.
- Визуальный статус (открыто / заблокировано с диагональной полосой).

#### `SettingsActivity`
Настройки приложения:
- Имя игрока (сохраняется в `SharedPreferences`).
- Включение/выключение фоновой музыки.
- Включение/выключение вибрации при попадании.
- Сброс всего прогресса (вызывает `ResetProgress.resetAllProgress`).

---

### 4.2 Игровой движок (GameView)

`GameView` — центральный класс приложения, наследник `android.view.View`. Реализует полный игровой цикл через `invalidate()` / `onDraw()`.

#### Физика мяча

| Параметр | Значение | Описание |
|---|---|---|
| `GRAVITY` | 3.5f | Ускорение свободного падения (пикс/кадр²) |
| `FRICTION` | 0.96f | Коэффициент затухания горизонтальной скорости |
| Начальная скорость | Вычисляется из жеста свайп | Вектор `(endX - startX, endY - startY)` |

Алгоритм обновления:
1. Применение гравитации: `velocityY += GRAVITY`
2. Применение трения: `velocityX *= FRICTION`
3. Перемещение: `ballX += velocityX`, `ballY += velocityY`
4. Проверка пересечения с кольцом и табло.

#### Детекция попадания в кольцо

Реализована через вычисление прямоугольника внутреннего отверстия кольца (`rimInnerLeft`, `rimInnerRight`, `rimYTop`, `rimYBottom`). Мяч засчитывается, когда:
- Центр мяча пересёк верхнюю плоскость кольца (`enteredHoop = true`).
- Мяч полностью прошёл через кольцо (центр ниже нижней плоскости).
- Не было столкновения с боковыми стойками.

#### Усложнение в режиме Аркада

По мере роста счёта:
- **Движущееся кольцо** (`movingHoop = true`): скорость `hoopVX` увеличивается.
- **Первый барьер** (`showObstacle`): горизонтально движущийся прямоугольник.
- **Второй барьер** (`showObstacle2`): второй движущийся прямоугольник, скоростью в противоположном направлении.

Пороги активации задаются в методе `updateDifficulty()` внутри `GameView`.

#### Система комбо

- При нескольких последовательных попаданиях без промахов активируется **комбо-серия**.
- При комбо 2+ бонусные очки добавляются автоматически.
- Визуально: жёлтая подпись с числом серии (при 5+ — оранжевая, при 8+ — фиолетовая).
- Счётчик лучшего комбо (`bestComboStreak`) сохраняется в `SharedPreferences`.

#### Анимации и эффекты

| Эффект | Описание |
|---|---|
| Тряска экрана | `shakeUntilMs`, `shakeAmpPx` — кратковременный сдвиг Canvas |
| Тряска кольца | `rimShakeUntilMs` — при ударе мяча о кольцо |
| Вспышка при ударе | `impactFlashUntilMs` — полупрозрачный белый overlay |
| Анимация сетки | 7-узловая пружинная модель (`netDx`, `netVx`, `netDy`, `netVy`) |
| Анимация достижения | Всплывающая плашка с иконкой и текстом |
| Тень мяча | Полупрозрачный эллипс под мячом |
| Обводка мяча | Тонкая белая линия для anti-aliasing эффекта |

#### Призрачный мяч соперника (Online Duel)

В режиме дуэли отображается полупрозрачный «призрак» — позиция мяча соперника, обновляемая через сетевые снепшоты:
- Метод `applyGhostSnapshot(x, y, moving)` обновляет `GhostState`.
- `GhostState` содержит текущие и целевые координаты для плавной интерполяции.
- Счёт соперника обновляется через `updateRemoteScore(score)`.

#### Онбординг

При первом запуске каждого режима показывается подсказка (2.3 секунды):
- Аркада: «Аркада: набирай очки, сложность растёт»
- На время: «На время: 60 сек. Комбо даёт бонус к очкам»
- Дуэль: «Дуэль: обгони соперника за 60 сек»

Факт показа сохраняется в `SharedPreferences` (`onboarding_<MODE>`).

---

### 4.3 Слой данных (Data Layer)

Использует **Android Room** — ORM-библиотека поверх SQLite.

#### `AppDatabase`

Room-база данных версии 2. Содержит две таблицы:

| Таблица | Entity-класс | Описание |
|---|---|---|
| `leaderboard_entries` | `LeaderboardEntry` | Записи рейтинга |
| `player_stats` | `PlayerStats` | Агрегированная статистика |

Миграция: `fallbackToDestructiveMigration()` — при несовместимой схеме данные удаляются (допустимо для учебного проекта).

#### `LeaderboardEntry`

| Поле | Тип | Описание |
|---|---|---|
| `id` | long (PK, auto) | Первичный ключ |
| `mode` | String | Имя `GameMode` (ARCADE / TIMED / ONLINE_DUEL) |
| `score` | int | Количество очков |
| `timestamp` | long | Время сохранения (Unix ms) |
| `playerName` | String | Имя игрока |

Хранится только лучший результат для каждого (режим, имя) — проверяется перед вставкой.

#### `PlayerStats`

| Поле | Тип | Описание |
|---|---|---|
| `id` | long (PK = 1) | Singleton-запись |
| `totalGames` | int | Общее количество игр |
| `arcadeBest` | int | Лучший результат в Аркаде |
| `timedBest` | int | Лучший результат «На время» |
| `duelBest` | int | Лучший результат в Дуэли |
| `duelWins` | int | Победы в Дуэли |
| `duelLosses` | int | Поражения в Дуэли |
| `updatedAt` | long | Время последнего обновления |

#### `LeaderboardRepository`

Инкапсулирует все операции с БД. Использует `ExecutorService` (однопоточный пул) для выполнения запросов в фоновом потоке. Публичные методы:

- `saveScore(mode, score, playerName, onDone)` — сохраняет результат (только если лучше предыдущего).
- `getTopScores(mode, limit, callback)` — возвращает топ-N результатов.
- `updateStats(updater, onDone)` — атомарное обновление статистики.
- `getStats(callback)` — получение статистики.
- `seedDemoDataIfEmpty()` — заполнение 10 демо-игроками при пустой БД.

---

### 4.4 Сетевой слой (Network Layer)

#### `MatchClient` (интерфейс)

Общий контракт для всех реализаций сетевого клиента:

```java
void connect(String target);
void sendSnapshot(float x, float y, boolean moving, int score);
void disconnect();
```

Listener-интерфейс:
- `onConnected()` — соединение установлено.
- `onDisconnected()` — соединение разорвано.
- `onError(message, throwable)` — ошибка.
- `onGhostSnapshot(x, y, moving, remoteScore)` — снепшот позиции соперника.

#### `OnlineMatchClient`

Реализация на основе **WebSocket (OkHttp 4.12.0)**:
- Подключается к серверу по переданному `wsUrl` (настраивается в `SettingsActivity`).
- Каждые ~60 мс отправляет JSON-снепшот:
  ```json
  {"type":"snapshot","x":540.00,"y":1200.00,"moving":true,"score":7}
  ```
- Принимает аналогичные снепшоты от соперника.
- Таймаут подключения: 5 секунд. Таймаут чтения: бесконечный (keep-alive).
- Парсинг JSON через `org.json.JSONObject` (входит в Android SDK, без внешних зависимостей).

#### `DemoDuelClient`

Локальный AI-бот, не требующий сервера:
- Симулирует позицию «призрака» с помощью физики (`ax`, `vx`, `vy`).
- Адаптирует темп набора очков к скорости игрока (`playerScorePerSec`).
- Реализует серии попаданий и промахов (`missStreak`, `scoreStreak`).
- Моделирует «двушки» (очки за один бросок = 2) при хорошем темпе игрока.
- Использует `Handler(Looper.getMainLooper())` для тактов с интервалом ~16 мс.

Алгоритм адаптации бота:
1. Измеряет скорость набора очков игроком каждые 700 мс.
2. Корректирует время между бросками бота (`nextScoreAtMs`).
3. Если игрок опережает на 4+ очка — бот ускоряется; если отстаёт на 4+ — замедляется.

---

### 4.5 Музыкальный плеер (MusicPlayer)

Singleton-класс для управления фоновой музыкой:
- Автоматически находит все MP3-файлы в `res/raw` через рефлексию (`R$raw`).
- Поддерживает плейлист: кнопки «следующий» / «предыдущий» трек.
- При одном треке — зацикливает его.
- Управляется из: `BasketballGameApp` (lifecycle), `GameActivity` (кнопки в HUD), `SettingsActivity` (переключатель).
- Состояние (вкл/выкл) сохраняется в `SharedPreferences` (`musicEnabled`).

Текущий плейлист: `music2.mp3`, `music3.mp3` (2 трека).

---

## 5. Игровые режимы

| Режим | Enum | Длительность | Описание |
|---|---|---|---|
| **Аркада** | `ARCADE` | Бесконечно | Набор очков без ограничений. Сложность растёт с каждым попаданием: движущееся кольцо, барьеры. |
| **На время** | `TIMED` | 60 секунд | Максимальный счёт за отведённое время. По истечении — диалог «Время вышло!» |
| **Дуэль** | `ONLINE_DUEL` | 60 секунд | Соревнование с «призрачным» соперником. Доступно два варианта: AI-бот (`DemoDuelClient`) или реальный игрок через WebSocket (`OnlineMatchClient`). |

### Выбор режима

Из главного меню → кнопка «Играть» → `BottomSheetDialog` с тремя картами режимов (иконки: `ic_mode_arcade`, `ic_mode_timed`, `ic_mode_duel`).

### Итоговый диалог

По завершении «На время» или «Дуэли» показывается `BottomSheetDialog` с результатами:
- Счёт игрока.
- Счёт соперника (только для Дуэли).
- Статус: «Победа!» (зелёный) / «Поражение» (красный) / «Ничья!».
- Кнопки: «Заново» (рестарт той же игры) / «Меню» (возврат).

---

## 6. Игровая механика и физика

### Управление (бросок мяча)

1. Игрок касается экрана в области мяча (`ACTION_DOWN`, фиксируется `startX`, `startY`).
2. Игрок тянет палец вверх (`ACTION_MOVE`, запоминается текущий вектор).
3. При отпускании (`ACTION_UP`) вычисляется вектор броска:
   - `velocityX = (endX - startX) * коэффициент`
   - `velocityY = (endY - startY) * коэффициент` (отрицательный = вверх)
4. Мяч переходит в состояние `isMoving = true`.

### Физическая симуляция

Каждый кадр (`onDraw`):
```
velocityY += GRAVITY          // Гравитация
velocityX *= FRICTION         // Трение
ballX     += velocityX
ballY     += velocityY
```

### Сброс позиции

После того как мяч выходит за границы экрана или после засчитанного/незасчитанного броска, позиция мяча сбрасывается в исходную точку внизу экрана, `canSwipe` устанавливается в `true`.

### Вибрация

При попадании в кольцо генерируется тактильный отклик:
- Android 8.0+ (API 26): `VibrationEffect.createOneShot(80, 255)`.
- Старые версии: `Vibrator.vibrate(80)`.
- Управляется настройкой `vibrationEnabled` в `SharedPreferences`.

---

## 7. Система достижений и инвентарь

### Достижения

Всего 8 достижений, разблокируемых по порогу счёта `achievementLevel`:

| № | Название | Порог | Награда |
|---|---|---|---|
| 1 | Первый бросок! | 5 очков | Мяч «Стрит» |
| 2 | Уличный игрок | 10 очков | Сетка «Стрит» |
| 3 | Стритболер | 15 очков | Мяч «Легенда» |
| 4 | Легенда площадки | 20 очков | Сетка «Легенда» |
| 5 | Мастер броска | 25 очков | Все мячи |
| 6 | Король улиц | 30 очков | Все сетки |
| 7 | Градиент: Синий фон | 35 очков | Синий фон |
| 8 | Градиент: Оранжевый фон | 45 очков | Оранжевый фон |

`achievementLevel` хранится в `SharedPreferences`. Разблокировка происходит в `GameView` при достижении порогового значения счёта.

### Инвентарь (скины)

Каждая категория имеет 3 варианта скинов:

| Категория | Скин 0 | Скин 1 | Скин 2 |
|---|---|---|---|
| **Мяч** | Обычный (`ball.png`) | Стрит (`ball2.png`) | Легенда (`ball3.png`) |
| **Сетка** | Обычная (`hoop.png`) | Стрит (`hoop2.png`) | Легенда (`hoop3.png`) |
| **Фон** | Розовый (`bg_gradient`) | Синий (`bg_gradient2`) | Оранжевый (`bg_gradient3`) |

Скины мяча и сетки в инвентаре отображаются через процедурную генерацию превью (`GameView.renderBallPreview`, `GameView.renderHoopPreview`).

---

## 8. Модель данных

### SharedPreferences (`"basketball"`)

| Ключ | Тип | Значение по умолчанию | Описание |
|---|---|---|---|
| `playerName` | String | `"Игрок"` | Имя игрока |
| `selectedBall` | int | 0 | Выбранный скин мяча (0–2) |
| `selectedHoop` | int | 0 | Выбранный скин кольца (0–2) |
| `selectedBg` | int | 0 | Выбранный фон (0–2) |
| `unlockedBall` | int | 0 | Индекс последнего разблокированного мяча |
| `unlockedHoop` | int | 0 | Индекс последнего разблокированного кольца |
| `unlockedBg` | int | 0 | Индекс последнего разблокированного фона |
| `achievementLevel` | int | 0 | Текущий уровень достижений (0–8) |
| `achievementUnlocked` | boolean | false | Флаг первого достижения |
| `musicEnabled` | boolean | true | Включена ли музыка |
| `vibrationEnabled` | boolean | true | Включена ли вибрация |
| `onlineWsUrl` | String | null | URL WebSocket-сервера (для онлайн-дуэли) |
| `onboarding_ARCADE` | boolean | false | Показан ли онбординг для аркады |
| `onboarding_TIMED` | boolean | false | Показан ли онбординг для режима «На время» |
| `onboarding_ONLINE_DUEL` | boolean | false | Показан ли онбординг для дуэли |

### Room Database (`basketball_leaderboard.db`)

Версия схемы: 2. Файл БД располагается в `/data/data/com.example.basketballgame/databases/`.

**Таблица `leaderboard_entries`:**

```sql
CREATE TABLE leaderboard_entries (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    mode      TEXT,
    score     INTEGER,
    timestamp INTEGER,
    playerName TEXT
);
```

**Таблица `player_stats`:**

```sql
CREATE TABLE player_stats (
    id          INTEGER PRIMARY KEY,   -- всегда = 1
    totalGames  INTEGER,
    arcadeBest  INTEGER,
    timedBest   INTEGER,
    duelBest    INTEGER,
    duelWins    INTEGER,
    duelLosses  INTEGER,
    updatedAt   INTEGER
);
```

---

## 9. Технологический стек и зависимости

| Компонент | Библиотека / Инструмент | Версия | Назначение |
|---|---|---|---|
| Язык | Java | 17 | Основной язык разработки |
| Платформа | Android SDK | 34 (compileSdk) | Целевая платформа |
| Минимум | Android API | 21 (Android 5.0) | Минимальная поддерживаемая версия |
| Сборка | Gradle | 8.x | Система сборки и управления зависимостями |
| UI | AndroidX AppCompat | 1.6.1 | Совместимые компоненты UI |
| UI | Material Components | 1.9.0 | BottomSheetDialog, TabLayout, SwitchMaterial |
| UI | ConstraintLayout | 2.1.4 | Гибкий layout для Leaderboard |
| UI | RecyclerView | 1.3.2 | Список лидерборда |
| Сеть | OkHttp | 4.12.0 | WebSocket-соединение для онлайн-дуэли |
| БД | Room Runtime | 2.6.1 | ORM поверх SQLite |
| БД | Room Compiler | 2.6.1 | Annotation processor для Room |
| Графика | Android Canvas API | — | Вся отрисовка игры (нативный API) |
| Хранение | SharedPreferences | — | Настройки, прогресс, разблокировки |
| Звук | MediaPlayer | — | Воспроизведение фоновых MP3-треков |
| Тактильность | Vibrator / VibratorManager | — | Вибрация при попадании |
| Тесты | JUnit4 | 4.13.2 | Unit-тесты бизнес-логики |

### Разрешения Android

| Разрешение | Файл | Назначение |
|---|---|---|
| `INTERNET` | `AndroidManifest.xml` | WebSocket-соединение для онлайн-дуэли |
| `VIBRATE` | `AndroidManifest.xml` | Тактильная обратная связь при попадании |

---

## 10. Пользовательский интерфейс

### Навигация между экранами

```
MainActivity
  ├── [Играть]        → BottomSheet выбора режима → GameActivity
  ├── [Лидерборд]     → LeaderboardActivity
  ├── [Инвентарь]     → InventoryActivity
  ├── [Достижения]    → AchievementsActivity
  └── [Настройки]     → SettingsActivity

GameActivity
  └── (по завершении) → BottomSheet результатов
        ├── [Заново]  → GameActivity (новая игра того же режима)
        └── [Меню]    → MainActivity
```

### Цветовая схема

Приложение использует единую тёмно-фиолетовую палитру:
- Основной акцент: `#8F5CFF` (фиолетовый)
- Светлый акцент: `#B266FF`
- Фон карточек: `#1A102B` / `#2D193C`
- Текст: `#FFFFFF`
- Золото (рекорды): `#FFD700` / `#FFEE58`

### Фоновые градиенты

| Индекс | Имя ресурса | Цвета |
|---|---|---|
| 0 | `bg_gradient` | Тёмно-фиолетовый градиент (основной) |
| 1 | `bg_gradient2` | Синий градиент |
| 2 | `bg_gradient3` | Оранжево-красный градиент |

### UI в игре (HUD)

В `GameActivity` поверх `GameView` располагаются:
- Кнопка «Назад» (стрелка, левый верхний угол).
- Панель управления музыкой: кнопки «предыдущий», «воспроизведение/пауза», «следующий» (по центру сверху).

Внутри `GameView` (рисуются через Canvas):
- Счёт игрока (крупно, по центру).
- Таймер (режимы TIMED и ONLINE_DUEL).
- Счёт соперника (только ONLINE_DUEL).
- Надпись комбо-серии.
- Текст онбординга при первом запуске.
- Плашка достижения (всплывает на несколько секунд).

---

## 11. Сборка и запуск

### Требования

- **Android Studio** Hedgehog (2023.1.1) или новее.
- **JDK 17** (поставляется с Android Studio).
- **Android SDK** API 34 (установить через SDK Manager).
- **Эмулятор** или физическое устройство с Android 5.0+.

### Шаги сборки

```bash
# 1. Клонируйте репозиторий
git clone https://github.com/ogSulem/BasketMars.git
cd BasketMars

# 2. Откройте проект в Android Studio
# File → Open → выберите папку BasketMars

# 3. Синхронизируйте Gradle
# (Android Studio сделает это автоматически, или: File → Sync Project with Gradle Files)

# 4. Соберите и запустите
# Run → Run 'app' (Shift+F10)
```

### Сборка APK для релиза

```bash
./gradlew assembleRelease
```

APK будет создан в: `app/build/outputs/apk/release/app-release.apk`

### Запуск Unit-тестов

```bash
./gradlew test
```

Отчёт: `app/build/reports/tests/testDebugUnitTest/index.html`

### Подключение онлайн-дуэли

1. Разверните WebSocket-сервер, совместимый с форматом сообщений:
   ```json
   {"type":"snapshot","x":540.00,"y":1200.00,"moving":true,"score":7}
   ```
2. В приложении: **Настройки** → поле **URL сервера** → введите `wss://your-server.com/match`.
3. В режиме дуэли будет использоваться `OnlineMatchClient` вместо `DemoDuelClient`.

---

## 12. Текущее состояние и ограничения

| Аспект | Текущее состояние | Что необходимо для продакшена |
|---|---|---|
| Онлайн-дуэль | Работает с AI-ботом; WebSocket-клиент реализован, сервер — заглушка | Реальный матчмейкинг-сервер |
| Авторизация | Отсутствует | Аутентификация через Google Sign-In SDK |
| Лидерборд | Локальный (Room SQLite) | Облачный (Firebase Firestore / REST API) |
| Защита от читов | Отсутствует | Серверная валидация бросков |
| Push-уведомления | Отсутствуют | Firebase Cloud Messaging |
| App Store | Не опубликовано | Google Play: подписание, политика конфиденциальности |
| Unit-тесты | `GameModeTest`, `PlayerStatsTest` (JUnit4) | Покрытие `DemoDuelClient`, UI-тесты Espresso |
| Аналитика | Отсутствует | Firebase Analytics / Crashlytics |
| Локализация | `strings.xml` на русском | Добавить `res/values-en/strings.xml` |
| Доступность | `contentDescription` на иконках | Проверка TalkBack, масштабирование шрифта |

---

## 12а. Как добавить Firebase (пошагово)

Для перехода к облачному лидерборду, аутентификации и аналитике нужно подключить Firebase. Ниже — минимальный план.

### A. Создание Firebase-проекта

1. Открыть [Firebase Console](https://console.firebase.google.com/).
2. Создать новый проект → ввести название → отключить Google Analytics (или включить).
3. Нажать **«Добавить приложение» → Android**.
4. Ввести package name: `com.example.basketballgame`.
5. Скачать файл `google-services.json` и положить в папку `app/`.

### B. Изменения в Gradle

**`build.gradle` (корневой):**
```groovy
plugins {
    id 'com.google.gms.google-services' version '4.4.1' apply false
}
```

**`app/build.gradle`:**
```groovy
plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services'
}

dependencies {
    // Firebase BOM управляет версиями всех Firebase-зависимостей
    implementation platform('com.google.firebase:firebase-bom:33.1.0')

    implementation 'com.google.firebase:firebase-firestore'   // Облачный лидерборд
    implementation 'com.google.firebase:firebase-auth'        // Аутентификация
    implementation 'com.google.firebase:firebase-analytics'   // Аналитика
    implementation 'com.google.firebase:firebase-crashlytics' // Краш-репорты

    // Google Sign-In (для аутентификации через аккаунт Google)
    implementation 'com.google.android.gms:play-services-auth:21.2.0'
}
```

### C. Облачный лидерборд (Firestore)

Замените вызовы `LeaderboardRepository` на Firestore:

```java
// Сохранить результат
FirebaseFirestore db = FirebaseFirestore.getInstance();
Map<String, Object> entry = new HashMap<>();
entry.put("mode", mode);
entry.put("score", score);
entry.put("playerName", playerName);
entry.put("timestamp", FieldValue.serverTimestamp());

db.collection("leaderboard")
  .add(entry)
  .addOnSuccessListener(ref -> Log.d("Firestore", "Сохранено: " + ref.getId()))
  .addOnFailureListener(e -> Log.e("Firestore", "Ошибка", e));
```

Запрос топ-20:
```java
db.collection("leaderboard")
  .whereEqualTo("mode", mode)
  .orderBy("score", Query.Direction.DESCENDING)
  .limit(20)
  .get()
  .addOnSuccessListener(querySnapshot -> {
      List<DocumentSnapshot> docs = querySnapshot.getDocuments();
      // обработка результатов...
  });
```

### D. Google Sign-In (авторизация)

```java
// В GameActivity / MainActivity
GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id)) // из google-services.json
        .requestEmail()
        .build();
GoogleSignInClient signInClient = GoogleSignIn.getClient(this, gso);

// Запустить экран входа
startActivityForResult(signInClient.getSignInIntent(), RC_SIGN_IN);
```

### E. Firebase Analytics

```java
// В BasketballGameApp.onCreate()
FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(this);

// Логировать событие попадания
Bundle params = new Bundle();
params.putString("mode", "ARCADE");
params.putInt("score", score);
analytics.logEvent("ball_scored", params);
```

---

## 13. Перспективы развития

### Краткосрочные (v1.1)
1. **Реальный онлайн-сервер** — Node.js/Go WebSocket-сервер с матчмейкингом (комнаты по 2 игрока).
2. **Облачный лидерборд** — интеграция с Firebase Firestore (инструкция в разделе 12а).
3. **Расширение тестов** — покрытие `DemoDuelClient` (Robolectric), UI-тесты Espresso.
4. **Оптимизация Canvas** — кэширование `Path` объектов, предварительный рендер в `Bitmap`.

### Среднесрочные (v1.2)
5. **Firebase аутентификация** — Google Sign-In, постоянный профиль игрока в облаке.
6. **Новые режимы** — «Испытание» (ограниченное количество попыток), «Турнир» (несколько раундов).
7. **Расширение инвентаря** — анимированные скины, эффекты частиц при попадании.
8. **Звуковые эффекты** — звук броска, попадания, промаха, комбо.

### Долгосрочные (v2.0)
9. **Публикация в Google Play** — иконки всех размеров, скриншоты, политика конфиденциальности.
10. **Монетизация** — опциональные платные скины (без pay-to-win).
11. **Турнирная система** — соревнования с расписанием и наградами.
12. **Мультиязычность** — EN, TR и другие локализации (основа `strings.xml` уже есть).

---

*Разработано в рамках дипломной работы — КФУ, группа 09-253.*  
*Яруллин Марсель, 2025–2026 г.*
