# Выпускная квалификационная работа (ВКР)
## Разработка мобильного приложения «BasketMars» на платформе Android

**Студент:** Яруллин Марсель Радикович  
**Группа:** 09-253  
**Вуз:** Казанский федеральный университет (КФУ), Институт вычислительной математики и информационных технологий  
**Научный руководитель:** *(ФИО руководителя)*  
**Год:** 2026  

---

## Аннотация

Данная выпускная квалификационная работа посвящена разработке мобильного игрового приложения **BasketMars** — аркадной баскетбольной игры для платформы Android. Приложение реализует физически корректный бросок мяча в кольцо, четыре игровых режима (в том числе онлайн-матчмейкинг с реальным противником через Firebase Firestore), систему достижений с разблокируемым инвентарём, двуязычный интерфейс, облачный лидерборд и аутентификацию через Google Sign-In. Весь игровой движок, физика и анимации реализованы средствами нативного Android SDK (Java, Canvas API) без применения сторонних игровых фреймворков.

**Ключевые слова:** Android, Java, Canvas API, Firebase Firestore, Room, Google Sign-In, аркадная игра, матчмейкинг, ИИ-бот, архитектурные паттерны.

---

## Оглавление

1. [Введение](#1-введение)
2. [Цель и задачи работы](#2-цель-и-задачи-работы)
3. [Обзор предметной области](#3-обзор-предметной-области)
4. [Описание разработанного приложения](#4-описание-разработанного-приложения)
5. [Архитектура и техническое решение](#5-архитектура-и-техническое-решение)
6. [Реализация игровых режимов](#6-реализация-игровых-режимов)
7. [Физика и игровой движок](#7-физика-и-игровой-движок)
8. [Система достижений и инвентарь](#8-система-достижений-и-инвентарь)
9. [Сетевой слой: ИИ-бот и онлайн PvP](#9-сетевой-слой-ии-бот-и-онлайн-pvp)
10. [Хранение данных и облачная интеграция](#10-хранение-данных-и-облачная-интеграция)
11. [Пользовательский интерфейс](#11-пользовательский-интерфейс)
12. [Тестирование](#12-тестирование)
13. [Результаты и выводы](#13-результаты-и-выводы)
14. [Список использованных источников](#14-список-использованных-источников)

---

## 1. Введение

Мобильные игры занимают доминирующую долю среди скачиваемых приложений в мире: согласно статистике Google Play за 2024 год, на игры приходится более 70% выручки магазина. При этом жанр аркадных игр остаётся одним из наиболее востребованных благодаря простоте управления и высокой повторяемости — игрок может сделать «ещё один бросок» бесконечно.

Данная выпускная квалификационная работа посвящена разработке мобильного приложения **BasketMars** — аркадной баскетбольной игры для платформы Android. Приложение реализует физически корректный бросок мяча в кольцо, несколько игровых режимов, в том числе онлайн-матчмейкинг с реальным игроком через Firebase Firestore, систему достижений, инвентарь скинов, двуязычный интерфейс и режим дуэли против ИИ-бота.

Принципиальное отличие проекта от коммерческих аналогов — отказ от сторонних игровых движков (Unity, Unreal, libGDX). Весь рендеринг, физика, анимации и UI реализованы средствами нативного Android SDK (Java, Canvas API), что позволяет детально изучить архитектурные паттерны Android-разработки и низкоуровневые механизмы отрисовки.

Проект опубликован на GitHub (открытый исходный код) и собирается в виде APK под Android 5.0+ (API 21+). Приложение полностью функционирует в офлайн-режиме; онлайн-возможности (облачный лидерборд, PvP) активируются при наличии рабочего Firebase-проекта.

---

## 2. Цель и задачи работы

**Цель:** разработать полнофункциональное мобильное Android-приложение игрового жанра с применением современных архитектурных практик, включая онлайн-матчмейкинг и облачные сервисы.

**Задачи:**
1. Спроектировать многослойную архитектуру приложения (UI → GameEngine → Data/Network).
2. Реализовать игровой движок на основе `android.graphics.Canvas` без сторонних движков.
3. Разработать физически корректную модель броска (квадратичная кривая Безье, детекция попадания).
4. Создать четыре различных игровых режима: Аркада, На время, Дуэль (ИИ-бот), Онлайн PvP.
5. Реализовать систему достижений с разблокируемым инвентарём (мячи, сетки, фоны).
6. Создать ИИ-бота с человекоподобным поведением (переменная точность, серии, банковские броски).
7. Реализовать онлайн-матчмейкинг через Firebase Firestore: очередь, автотаймаут, real-time синхронизация счёта.
8. Интегрировать локальное хранилище данных (Room/SQLite) и облачный лидерборд (Firebase Firestore).
9. Реализовать аутентификацию через Google Sign-In + Firebase Auth.
10. Добавить поддержку двух языков (русский, английский) с переключением в настройках.
11. Написать unit-тесты для ключевых компонентов.

---

## 3. Обзор предметной области

### 3.1 Рынок мобильных игр

Мобильный игровой рынок демонстрирует устойчивый рост: по данным Sensor Tower, в 2024 году мировая выручка от мобильных игр превысила $100 млрд. Аркадные игры (к которым относится BasketMars) традиционно лидируют по количеству установок. Одним из ключевых трендов последних лет стало добавление онлайн-соревновательных режимов (PvP), которые значительно увеличивают время сессии и лояльность аудитории.

### 3.2 Аналоги

| Приложение | Описание | Отличие от BasketMars |
|---|---|---|
| NBA 2K Mobile | Официальная лицензия NBA, 3D-графика | Платная, тяжёлая, требует постоянный интернет |
| Basketball Stars | Онлайн PvP, 2D | Требует выделенный сервер, зависит от сторонней платформы |
| Real Basketball | Одиночный аркадный бросок | Нет достижений, нет дуэли с ИИ, нет онлайна |
| Dunk Shot | Минималистичный одиночный режим | Нет многопользовательского режима |

**Конкурентное преимущество BasketMars:**
- Работает полностью офлайн (ИИ-бот не требует сервера)
- Онлайн PvP реализован через Firebase без выделенного backend-сервера
- Двуязычный интерфейс (RU/EN)
- Открытый исходный код (GitHub)
- Нативная разработка без движков — лёгкое APK

### 3.3 Технологический контекст

Для разработки Android-приложений доступны несколько технологических стеков:

| Стек | Плюсы | Минусы | Применимость к проекту |
|---|---|---|---|
| **Native Java/Kotlin + Android SDK** | Максимальный контроль, лучшая производительность, малый APK | Больше кода | ✅ Выбран |
| Unity | Мощный движок, 3D | Избыточен для 2D-аркады, APK > 50 МБ | ❌ |
| Flutter (Dart) | Кроссплатформенный | Canvas API менее гибок | ❌ |
| libGDX (Java) | Игровой фреймворк, 2D | Лишняя зависимость | ❌ |

В данном проекте выбран **Native Java** для максимальной гибкости и глубокого изучения платформы.

---

## 4. Описание разработанного приложения

### 4.1 Функциональность

**BasketMars** — аркадная игра, в которой игрок бросает баскетбольный мяч в кольцо жестом свайп. Приложение включает:

- **4 игровых режима:** Аркада (бесконечный), На время (60 сек), Дуэль (против ИИ-бота), **Онлайн PvP** (реальный игрок через Firebase)
- **Инвентарь:** 3 варианта мяча, 3 варианта сетки, 3 фоновых градиента
- **8 достижений:** разблокируются по достижению пороговых значений очков
- **Лидерборд:** хранится локально (Room) с опциональной синхронизацией в облако (Firebase Firestore), 4 вкладки режимов
- **Настройки:** имя игрока, музыка, вибрация, язык (RU/EN), Google-аккаунт
- **Музыкальный плеер:** 2 фоновых MP3-трека с кнопками управления
- **Анимации:** конфетти при победе, дрожание кольца, физика сетки
- **Матчмейкинг:** очередь ожидания соперника с 30-секундным таймаутом и автоматическим откатом на ИИ-бота

### 4.2 Экраны приложения

| Экран | Activity | Назначение |
|---|---|---|
| Главное меню | `MainActivity` | Выбор режима, навигация |
| Матчмейкинг | `MatchmakingActivity` | Поиск соперника (Firestore-очередь, spinner, отмена) |
| Игра | `GameActivity` → `GameView` | Игровой процесс |
| Лидерборд | `LeaderboardActivity` | Таблица рекордов (4 вкладки: Аркада / На время / Дуэли / PvP) |
| Инвентарь | `InventoryActivity` | Выбор скинов мяча, сетки и фона |
| Достижения | `AchievementsActivity` | Прогресс достижений |
| Настройки | `SettingsActivity` | Все пользовательские настройки |

---

## 5. Архитектура и техническое решение

### 5.1 Общая архитектура

Приложение спроектировано по **многослойной (Layered) архитектуре**, разделённой на четыре слоя:

```
┌──────────────────────────────────────────────────────┐
│                     UI Layer                          │
│  Activity · BottomSheet · RecyclerView · MatchmakingUI│
├──────────────────────────────────────────────────────┤
│                 Game Engine Layer                      │
│  GameView (Canvas, Physics, Animation, HUD)           │
├──────────────────────────────────────────────────────┤
│              Network / AI Layer                        │
│  MatchClient (interface)                               │
│  ├── DemoDuelClient    (ИИ-бот, локальный)            │
│  └── OnlinePvpClient   (Firestore real-time PvP)      │
├──────────────────────────────────────────────────────┤
│                  Data Layer                            │
│  Room DB (SQLite) · Firestore · SharedPreferences     │
│  LeaderboardRepository · CloudLeaderboardRepository   │
│  AuthManager (Google Sign-In + Firebase Auth)         │
└──────────────────────────────────────────────────────┘
```

### 5.2 Паттерны проектирования

| Паттерн | Где применён |
|---|---|
| **Repository** | `LeaderboardRepository`, `CloudLeaderboardRepository` — скрывают источник данных (Room vs Firestore) |
| **Singleton** | `AuthManager.getInstance()`, `BasketballGameApp` (Application-класс), `MusicPlayer` |
| **Strategy** | `MatchClient` (интерфейс) — подменяет ИИ-бот (`DemoDuelClient`) на онлайн-клиент (`OnlinePvpClient`) без изменения `GameView` |
| **Observer** | `MatchClient.Listener` — callbacks от сетевого слоя в `GameView` |
| **Factory Method** | `GameMode.fromName()` — безопасное создание enum из строки |
| **Template Method** | `onDraw(Canvas)` в `GameView` — фиксированный порядок слоёв отрисовки |

### 5.3 Жизненный цикл игры

```
MainActivity.showModeDialog()
  ├── startGame(ARCADE/TIMED)  ─────────────► GameActivity.onCreate()
  │                                                └─► GameView.init() → немедленный старт
  ├── startGame(ONLINE_DUEL)  ──────────────► GameActivity.onCreate()
  │                                                └─► GameView.init() → DemoDuelClient.connect()
  │                                                         └─► onConnected() → запуск бота
  └── startActivity(MatchmakingActivity)
        └─► Firestore matchmaking_pool
              ├── [найден соперник] ──────► GameActivity (с roomId + myRole)
              │                                  └─► OnlinePvpClient.connect()
              │                                           └─► score1/score2 real-time sync
              └── [таймаут 30 сек] ───────► GameActivity (ONLINE_DUEL fallback)
```

---

## 6. Реализация игровых режимов

### 6.1 Аркада (ARCADE)

Бесконечный режим с прогрессивным усложнением:
- Кольцо перемещается в случайную позицию после каждого попадания
- Каждые 5 очков `hoopWidth` уменьшается на 4% (минимум 60% от исходного)
- Комбо-система: 3+ попадания подряд — бонусные очки и визуальный эффект
- Промахи не штрафуются, игра бесконечна

### 6.2 На время (TIMED)

60-секундный режим:
- Обратный таймер отображается на HUD
- По окончании — показывается экран результата, новые броски невозможны
- Рекорд сравнивается с лучшим результатом в лидерборде

### 6.3 Дуэль с ботом (ONLINE_DUEL)

60-секундный матч против ИИ-бота:
- Бот подключается через интерфейс `MatchClient` (реализация: `DemoDuelClient`)
- Рядом с кольцом отображается «призрак» бота — анимированный мяч
- После истечения времени определяется результат: победа / поражение / ничья
- При победе — анимация конфетти (`ConfettiView`)
- Статистика дуэлей хранится в `PlayerStats` (`duelWins`, `duelLosses`)

### 6.4 Онлайн PvP (ONLINE_PVP)

60-секундный матч против реального игрока через Firebase Firestore:

**Матчмейкинг (`MatchmakingActivity`):**
1. Игрок помещается в очередь `matchmaking_pool/{userId}` в Firestore
2. Отображается spinner «Поиск соперника…»
3. Firestore-слушатель ожидает появления соперника в коллекции
4. При нахождении соперника создаётся документ матча `matches/{roomId}` со статусом `waiting`
5. Автотаймаут 30 сек: если соперник не найден — запускается режим Дуэль с ботом (без потери игрового опыта)

**Игровой процесс (`OnlinePvpClient`):**
- Каждый игрок знает свой `roomId` и `myRole` (1 или 2)
- Счёт записывается в поля `score1`/`score2` документа матча
- Частота записи ограничена до 1 раза в 800 мс (дросселирование)
- Real-time слушатель `addSnapshotListener` читает счёт соперника и передаёт `GameView` через `onGhostSnapshot()`
- По завершении игры статус документа меняется на `finished`
- Результат сохраняется в `PlayerStats` (`onlinePvpWins`, `onlinePvpLosses`, `onlinePvpBest`) и в лидерборд (вкладка «PvP»)

**Структура документа в Firestore:**
```
matches/{roomId}/
  player1Id:   "uid1"
  player1Name: "Марсель"
  player2Id:   "uid2"
  player2Name: "Tim"
  status:      "waiting" | "playing" | "finished"
  startTime:   epoch_ms
  score1:      0
  score2:      0
```

---

## 7. Физика и игровой движок

### 7.1 Рендеринг

Весь игровой рендеринг реализован в `GameView.onDraw(Canvas canvas)`:
- **~16 мс на кадр** — `postInvalidateOnAnimation()` запускает следующий кадр при помощи Choreographer
- Слоистая отрисовка: фон → кольцо → сетка → мяч → HUD → анимации
- PNG-спрайты для мячей и колец (загружаются с `BitmapFactory`, масштабируются через `Matrix`)
- Процедурный рендеринг для превью в `InventoryActivity` — идентичен игровому

### 7.2 Физика броска

Бросок реализован как **квадратичная кривая Безье**:

```java
// P(t) = (1-t)² * P0 + 2(1-t)t * Pmid + t² * P1
float inv = 1f - t;
ballX = inv*inv*fromX + 2f*inv*t*midX + t*t*targetX;
ballY = inv*inv*fromY + 2f*inv*t*peakY + t*t*targetY;
```

Параметры вычисляются из жеста свайп:
- `(fromX, fromY)` — начальная точка касания
- `(targetX, targetY)` — точка кольца + смещение от вектора свайпа
- `peakY` — вершина дуги: `min(fromY, targetY) - velocity * 0.3f`
- `t` — параметр, растёт от 0 до 1 за время полёта

### 7.3 Детекция попадания

Хитбокс кольца — эллипс в горизонтальной плоскости:

```java
float dx = ballX - hoopCenterX;
float dy = ballY - rimYTop;
boolean inHoop = Math.abs(dx) < hoopWidth * 0.48f
              && Math.abs(dy) < rimHeight * 0.35f
              && velocityY > 0;  // мяч летит вниз
```

Дополнительно проверяется, что мяч не прошёл через стойку кольца (`backboard hit`), — в этом случае добавляется отскок по оси X.

### 7.4 Физика сетки

Сетка реализована как цепочка из N узлов с **spring-симуляцией**:
- Каждый узел имеет позицию `(x, y)` и скорость `(vx, vy)`
- При попадании мяча — импульс к верхним узлам пропорционально скорости мяча
- Затухание: `vy *= 0.88f` на каждом кадре
- Отрисовка: линии между соседними узлами в вертикальном направлении

### 7.5 Вибрация и тактильный отклик

При попадании мяча срабатывает вибратор:
- API 26+: `VibrationEffect.createOneShot(50, amplitude)`
- API < 26: устаревший `vibrator.vibrate(50)` (с проверкой версии API)

---

## 8. Система достижений и инвентарь

### 8.1 Механика достижений

Достижения проверяются в `GameView` после каждого успешного броска:

```java
int achievementLevel = prefs.getInt("achievementLevel", 0);
if (score >= unlockScore[achievementLevel]) {
    achievementLevel++;
    prefs.edit().putInt("achievementLevel", achievementLevel).apply();
    unlockReward(achievementLevel);
}
```

Пороги разблокировки и награды:

| Уровень | Порог (очков) | Название | Награда |
|---|---|---|---|
| 1 | 5 | Первый бросок! | Мяч «Стрит» |
| 2 | 10 | Уличный игрок | Сетка «Стрит» |
| 3 | 15 | Стритболер | Мяч «Легенда» |
| 4 | 20 | Легенда площадки | Сетка «Легенда» |
| 5 | 25 | Мастер броска | Все мячи |
| 6 | 30 | Король улиц | Все сетки |
| 7 | 35 | Градиент: Синий | Синий фон |
| 8 | 45 | Градиент: Оранжевый | Оранжевый фон |

### 8.2 Инвентарь и персонализация

Скины хранятся в `SharedPreferences` (ключи `selectedBall`, `selectedHoop`, `selectedBg`).

Предпросмотр скинов в `InventoryActivity` использует статические методы `GameView.renderBallPreview(Context, int, int)` и `GameView.renderHoopPreview(Context, int, int)` — те же алгоритмы, что и в игре, гарантируя точное визуальное соответствие.

---

## 9. Сетевой слой: ИИ-бот и онлайн PvP

### 9.1 Интерфейс MatchClient

Ключевой абстракцией сетевого слоя является интерфейс `MatchClient`:

```java
public interface MatchClient {
    void connect(String target);
    void sendSnapshot(float x, float y, boolean moving, int score);
    void disconnect();

    interface Listener {
        void onConnected();
        void onDisconnected();
        void onGhostSnapshot(float x, float y, boolean moving, int rivalScore);
        void onError(String message, @Nullable Exception cause);
    }
}
```

Паттерн **Strategy**: `GameView` работает только с интерфейсом, не зная, кто находится по ту сторону — ИИ-бот или реальный игрок через Firestore.

### 9.2 ИИ-бот (DemoDuelClient)

Бот разработан с целью максимально имитировать поведение реального игрока:

**Шанс промаха:**
- Базовый: 47% (бот попадает в среднем ~53% бросков)
- Минимальный floor: 35% (даже в «горячей» форме не чаще ~65%)
- Банковские броски сложнее: +12% к шансу промаха
- Максимальный cap: 88% (бот никогда полностью не «отключается»)

**Серии (streak system):**
- «Горячая серия»: после 2+ попаданий подряд — 55% вероятность войти в «холодную серию»
- «Холодная серия» (`coldStreak`): 4–8 обязательных промахов подряд
- «Разогрев»: после 5+ промахов — 30% вероятность обнуления streak

**Типы бросков:**
| Тип | Доля | Особенность |
|---|---|---|
| Прямой (`SHOT_DIRECT`) | 72% | Дуга от точки броска к кольцу |
| Банковский левый (`SHOT_BANK_LEFT`) | 14% | `botMidX` смещается к левой стенке |
| Банковский правый (`SHOT_BANK_RIGHT`) | 14% | `botMidX` смещается к правой стенке |

**Адаптация темпа:**
- Базовый интервал: 4500 мс
- Адаптируется к темпу игрока через EWMA (экспоненциальное скользящее среднее)
- После промаха: +600–1300 мс паузы
- Диапазон: 1800–5500 мс

### 9.3 Реальный онлайн-клиент (OnlinePvpClient)

`OnlinePvpClient` реализует `MatchClient` поверх Firebase Firestore:

**Ключевые механизмы:**
1. **Подписка (`addSnapshotListener`)**: слушает документ матча — при изменении поля `score2`/`score1` соперника вызывает `listener.onGhostSnapshot()`
2. **Отправка счёта (`update`)**: записывает своё поле (`score1`/`score2`), но не чаще одного раза в 800 мс и только при изменении значения — снижает нагрузку на Firestore
3. **Завершение**: обновляет `status: "finished"`, снимает слушатель, освобождает ресурсы

```java
// Дросселирование записи счёта:
private static final long SCORE_WRITE_INTERVAL_MS = 800;

@Override
public void sendSnapshot(float x, float y, boolean moving, int score) {
    if (!connected || matchRef == null) return;
    if (score == lastWrittenScore) return;
    long now = System.currentTimeMillis();
    if (now - lastWriteMs < SCORE_WRITE_INTERVAL_MS) return;
    lastWrittenScore = score;
    lastWriteMs = now;
    matchRef.update(myScoreField, score);
}
```

---

## 10. Хранение данных и облачная интеграция

### 10.1 SharedPreferences

Быстрый Key-Value store для настроек и состояния:

| Ключ | Тип | Назначение |
|---|---|---|
| `playerName` | String | Имя игрока |
| `musicEnabled` | Boolean | Музыка вкл/выкл |
| `vibrationEnabled` | Boolean | Вибрация вкл/выкл |
| `achievementLevel` | Int | Текущий уровень достижений (0–8) |
| `selectedBall` | Int | Выбранный мяч (0–2) |
| `selectedHoop` | Int | Выбранная сетка (0–2) |
| `selectedBg` | Int | Выбранный фон (0–2) |
| `basketball_locale` | String | Язык приложения ("ru"/"en") |

### 10.2 Room (SQLite)

База данных `AppDatabase` (версия 3) содержит 2 таблицы с поддержкой миграций.

**Таблица `leaderboard_scores`** (`LeaderboardEntry`):
```
id          INTEGER PRIMARY KEY AUTOINCREMENT
mode        TEXT    (ARCADE / TIMED / ONLINE_DUEL / ONLINE_PVP)
score       INTEGER
playerName  TEXT
timestamp   INTEGER (ms)
```

**Таблица `player_stats`** (`PlayerStats`):
```
id              INTEGER PRIMARY KEY = 1  (singleton)
totalGames      INTEGER
arcadeBest      INTEGER
timedBest       INTEGER
duelBest        INTEGER
duelWins        INTEGER
duelLosses      INTEGER
onlinePvpBest   INTEGER   ← добавлено в версии 3
onlinePvpWins   INTEGER   ← добавлено в версии 3
onlinePvpLosses INTEGER   ← добавлено в версии 3
updatedAt       INTEGER (ms)
```

**История миграций:**

| Версия | Изменение |
|---|---|
| 1 | Исходная схема |
| 2 | Промежуточные изменения |
| 3 | Добавлены поля `onlinePvpBest`, `onlinePvpWins`, `onlinePvpLosses` (ALTER TABLE) |

Миграция реализована через `Migration(2, 3)` с использованием `ALTER TABLE` — данные существующих пользователей не теряются.

### 10.3 Firebase Firestore

Firestore используется в двух контекстах:

**Облачный лидерборд** (`CloudLeaderboardRepository`):
```
leaderboard_scores/{userId}_{mode}/
  mode:       "ARCADE"
  score:      42
  playerName: "Марсель"
  userId:     "abc123"
  timestamp:  1711234567890
```

**Матчмейкинг** (`matchmaking_pool`, `matches`):
```
matchmaking_pool/{userId}/
  userId:     "abc123"
  name:       "Марсель"
  timestamp:  epoch_ms

matches/{roomId}/
  player1Id, player2Id, status, score1, score2, startTime
```

`LeaderboardRepository` объединяет локальные (Room) и облачные (Firestore) результаты: дедупликация по имени игрока, сортировка по убыванию очков.

### 10.4 Firebase Auth (Google Sign-In)

`AuthManager` — Singleton, управляющий жизненным циклом аутентификации:

1. `signIn(Activity, RC)` → запускает Google-интент выбора аккаунта
2. `handleSignInResult(data, callback)` → обменивает Google-токен на Firebase-сессию
3. `isSignedIn()` → проверяет наличие текущего пользователя в `FirebaseAuth`
4. `signOut(activity, callback)` → разрывает сессию Google и Firebase
5. При входе автоматически синхронизируется лидерборд и активируется матчмейкинг

---

## 11. Пользовательский интерфейс

### 11.1 Принципы дизайна

- **Тёмная тема** (Dark Theme) — фиолетово-чёрная палитра (`#1A102B` / `#8f5cff`)
- **Адаптивность** — гибкий layout для разных разрешений (1080p / FHD+)
- **Доступность** — `contentDescription` на всех иконках и интерактивных элементах
- **Анимации переходов** — scale+alpha на кнопках (`GameView.animateButton`)
- **Material Design 3** — BottomSheetDialog, TabLayout, CardView

### 11.2 Экран матчмейкинга

`MatchmakingActivity` показывает:
- ProgressBar (spinner) со статусом «Поиск соперника…»
- Имя найденного соперника при успехе: «Соперник найден: %s»
- Кнопку «Отмена» для выхода из очереди
- Автоматический переход на игровой экран при успехе или таймауте

### 11.3 Локализация

`LocaleHelper` оборачивает `Context` в нужную locale:

```java
// В каждом Activity:
@Override
protected void attachBaseContext(Context base) {
    super.attachBaseContext(LocaleHelper.wrap(base));
}
```

Ресурсные файлы:
- `res/values/strings.xml` — русский (по умолчанию)
- `res/values-en/strings.xml` — английский

Переключение происходит мгновенно — без перезапуска приложения, только Activity.

### 11.4 Музыкальный плеер

`MusicPlayer` — Singleton, управляющий `MediaPlayer`:
- Автозапуск при открытии игры (если музыка включена в настройках)
- Автоостановка при `onStop()` Activity
- Кнопки prev/next для смены трека
- Хранит текущий трек и позицию воспроизведения в `SharedPreferences`

---

## 12. Тестирование

### 12.1 Unit-тесты (JUnit 4)

**`GameModeTest.java`** — тесты перечисления режимов:
| Тест | Проверяет |
|---|---|
| `fromName_arcade_returnsArcade` | Корректный разбор "ARCADE" |
| `fromName_timed_returnsTimed` | Корректный разбор "TIMED" |
| `fromName_onlineDuel_returnsOnlineDuel` | Корректный разбор "ONLINE_DUEL" |
| `fromName_onlinePvp_returnsOnlinePvp` | Корректный разбор "ONLINE_PVP" |
| `fromName_null_returnsArcadeDefault` | null → ARCADE (дефолт) |
| `fromName_emptyString_returnsArcadeDefault` | "" → ARCADE (дефолт) |
| `fromName_unknown_returnsArcadeDefault` | Неизвестное имя → ARCADE |
| `fromName_lowercase_returnsArcadeDefault` | Регистрозависимость |
| `extraKey_isNotEmpty` | Константа EXTRA_KEY определена |
| `values_containsAllFourModes` | Ровно 4 значения в enum |

**`PlayerStatsTest.java`** — тесты модели данных игрока:
| Тест | Проверяет |
|---|---|
| `defaultValues_areZero` | Нулевые значения при создании |
| `defaultOnlinePvpValues_areZero` | PvP-поля нулевые при создании |
| `id_defaultIsSingleton` | id = 1 по умолчанию |
| `arcadeBest_updatesCorrectly` | Запись рекорда Аркады |
| `duelWinsAndLosses_areIndependent` | Независимость побед/поражений дуэли |
| `onlinePvpWinsAndLosses_areIndependent` | Независимость PvP-побед/поражений |
| `onlinePvpBest_updatesCorrectly` | Запись PvP-рекорда |
| `totalGames_incrementsCorrectly` | Инкремент счётчика игр |
| `bestScore_updatesOnlyIfGreater` | Рекорд не снижается |
| `onlinePvpBest_updatesOnlyIfGreater` | PvP-рекорд не снижается |

### 12.2 Ручное тестирование

| Сценарий | Ожидаемый результат | Статус |
|---|---|---|
| Бросок попадает в кольцо | Счёт +1, кольцо перемещается, анимация сетки | ✅ |
| Бросок мимо кольца | Счёт без изменений, мяч возвращается | ✅ |
| Таймер истекает (TIMED) | Показывается экран результата | ✅ |
| Победа в дуэли | Конфетти, сохранение в статистику | ✅ |
| Матчмейкинг: соперник найден | Переход в игру с roomId | ✅ |
| Матчмейкинг: таймаут 30 сек | Запуск дуэли с ботом | ✅ |
| Онлайн PvP: счёт соперника | Обновление ghost-индикатора | ✅ |
| Смена языка | Мгновенный рестарт Activity, все строки изменились | ✅ |
| Разблокировка достижения | Новый скин доступен в инвентаре | ✅ |
| Сброс прогресса | SharedPreferences и Room очищены | ✅ |
| Firebase недоступен | Приложение работает в офлайн-режиме | ✅ |

### 12.3 Ограничения тестирования

- Отсутствуют UI-тесты (Espresso) — запланированы в следующей версии
- Отсутствуют интеграционные тесты Firestore (требует реальное сетевое окружение)
- Нет Robolectric-тестов для `DemoDuelClient` и `OnlinePvpClient`

---

## 13. Результаты и выводы

### 13.1 Достигнутые результаты

В ходе разработки ВКР выполнено:

1. ✅ Спроектирована и реализована многослойная архитектура (UI → Engine → Network/AI → Data)
2. ✅ Написан игровой движок на Android Canvas API (~2600 строк, без сторонних фреймворков)
3. ✅ Реализована физическая модель броска (квадратичная Безье, хитбокс-детекция, отскок от стойки)
4. ✅ Созданы **четыре** игровых режима: Аркада, На время, Дуэль с ботом, Онлайн PvP
5. ✅ Реализован онлайн-матчмейкинг через Firebase Firestore: очередь, автотаймаут, real-time синхронизация
6. ✅ Разработан ИИ-бот с человекоподобным поведением (переменная точность, серии, банковские броски)
7. ✅ Реализована система достижений (8 уровней) и инвентарь (3×3×3 скина)
8. ✅ Интегрированы Room (локальный лидерборд) и Firebase Firestore (облачный лидерборд, 4 вкладки)
9. ✅ Реализована аутентификация через Google Sign-In + Firebase Auth
10. ✅ Добавлена поддержка двух языков (RU/EN) с мгновенным переключением
11. ✅ Написаны unit-тесты (JUnit 4) для `GameMode` и `PlayerStats`, включая PvP-поля
12. ✅ Добавлены анимации: конфетти, физика сетки, дрожание кольца, свайп-трейл
13. ✅ База данных Room обновлена до версии 3 с миграцией (новые PvP-поля)

### 13.2 Технические показатели

| Показатель | Значение |
|---|---|
| Строк кода (Java) | ~8 000+ |
| Строк `GameView.java` (ядро движка) | ~2 600 |
| Количество Activity | 7 |
| Количество unit-тестов | 20 |
| Минимальный Android API | 21 (Android 5.0) |
| Целевой Android API | 34 (Android 14) |
| Поддерживаемые языки | 2 (RU / EN) |

### 13.3 Практическая ценность

- APK собирается и запускается на устройствах с Android 5.0–14
- Приложение работает **полностью офлайн** — три режима (Аркада, На время, Дуэль с ботом) не требуют интернета
- Онлайн PvP при недоступности Firebase автоматически переключается на ИИ-бота — нет деградации UX
- Архитектура позволяет заменить Firestore на любой другой backend без изменения `GameView`
- Код опубликован на GitHub с открытой лицензией

### 13.4 Выводы

В процессе работы был получен практический опыт:
- Разработки игровых приложений на Android без игровых движков с полным контролем над рендерингом
- Применения паттернов Repository, Strategy, Singleton, Observer в реальном проекте
- Работы с базой данных Room, миграциями схемы и облачными сервисами Firebase
- Реализации онлайн-матчмейкинга и real-time синхронизации данных через Firestore без WebSocket-сервера
- Проектирования многослойной архитектуры Android-приложения с чёткими границами ответственности
- Реализации алгоритма «человекоподобного» ИИ с системой серий и адаптацией к темпу игрока

Проект демонстрирует, что полнофункциональная мобильная игра с онлайн-режимом может быть создана средствами нативного Android SDK и Firebase без зависимости от тяжёлых игровых движков и выделенных серверов — при правильной архитектуре и тщательном проектировании.

---

## 14. Список использованных источников

1. Android Developer Documentation — [developer.android.com](https://developer.android.com)
2. Google, «Room Persistence Library» — [developer.android.com/training/data-storage/room](https://developer.android.com/training/data-storage/room)
3. Google, «Room database migrations» — [developer.android.com/training/data-storage/room/migrating-db-versions](https://developer.android.com/training/data-storage/room/migrating-db-versions)
4. Firebase Documentation — [firebase.google.com/docs](https://firebase.google.com/docs)
5. Firebase Firestore Realtime Updates — [firebase.google.com/docs/firestore/query-data/listen](https://firebase.google.com/docs/firestore/query-data/listen)
6. Google Sign-In for Android — [developers.google.com/identity/sign-in/android](https://developers.google.com/identity/sign-in/android)
7. Material Design 3 Guidelines — [m3.material.io](https://m3.material.io)
8. Bezier Curves in Computer Graphics — Paul Bourke, «Bézier Curve», 1997
9. Gamma, E. et al. «Design Patterns: Elements of Reusable Object-Oriented Software», Addison-Wesley, 1994
10. Android Studio Documentation — [developer.android.com/studio](https://developer.android.com/studio)
11. Gradle Build Tool — [gradle.org/guides](https://gradle.org/guides)
12. JUnit 4 Reference — [junit.org/junit4](https://junit.org/junit4)
13. Sensor Tower, «State of Mobile Gaming 2024» — [sensortower.com](https://sensortower.com)
14. Google Play, «Android Vitals» — [developer.android.com/topic/performance/vitals](https://developer.android.com/topic/performance/vitals)

---

*Казанский Федеральный Университет, 2026 г.*  
*Студент: Яруллин Марсель Радикович, группа 09-253*  
*Институт вычислительной математики и информационных технологий*
