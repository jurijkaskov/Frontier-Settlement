# Архитектура проекта «Фронтир: Поселение» (Frontier Settlement)

Данный документ описывает целевую архитектуру, слои, модули, контракты данных и стандарты проектирования приложения «Фронтир: Поселение».

---

## 1. Общий обзор архитектуры (Layered Architecture)

Проект строго следует принципам **Clean Architecture** и **MVI / Unidirectional Data Flow (UDF)**:

```
┌────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                   │
│  Jetpack Compose Screens • ViewModels • UI Components  │
└───────────────────────────▲────────────────────────────┘
                            │ (StateFlow / Actions)
┌───────────────────────────┴────────────────────────────┐
│                      DOMAIN LAYER                      │
│   UseCases • Domain Services • Invariant Validators    │
│            Pure Domain Models & Entities               │
└───────────────────────────▲────────────────────────────┘
                            │ (Repository Interface)
┌───────────────────────────┴────────────────────────────┐
│                       DATA LAYER                       │
│    GameStateRepository • GameSaveDataSource • Configs  │
│             Static Catalogs & Generators               │
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────┴────────────────────────────┐
│                       CORE LAYER                       │
│    GameResult<T> • GameError • GameLogger • Formatters │
│                 Typed Identifiers (IDs)                │
└────────────────────────────────────────────────────────┘
```

---

## 2. Слои и границы ответственности

### 2.1 Core Layer (`com.example.core`)
Базовые универсальные утилиты и примитивы без зависимости от Android UI:
- **`GameResult<T>` & `GameError`** (`core.result`): Функциональный Result-паттерн для безопасной обработки игровых операций без выброса исключений.
- **`GameLogger`** (`core.log`): Централизованное логирование игровых событий, ошибок и отладочных данных с защитой от утечек в релизных сборках.
- **`GameFormatters`** (`core.format`): Форматирование веса, расстояний, времени (`День X, ЧЧ:ММ`), ресурсов и репутации.
- **`GameIdentifiers`** (`core.model`): Строго типизированные псевдонимы идентификаторов (`CharacterId`, `ItemId`, `LocationId`, `QuestId`, `BuildingId` и т.д.).

### 2.2 Domain Layer (`com.example.domain`)
Чистая бизнес-логика игры, не имеющая зависимостей от Android Framework или Jetpack Compose:
- **Models (`domain.model.*`)**: Чистые data-классы и enum'ы (`GameState`, `Character`, `Building`, `ResourceInventory`, `Expedition`, `CombatState`, `Quest`, `Faction`, `Vehicle`).
- **Use Cases (`domain.usecase.*`)**:
  - `settlement/BuildBuildingUseCase`: Проверка ресурсов, лимитов населения и атомарное строительство.
  - `quest/AcceptQuestUseCase`, `TurnInQuestUseCase`: Управление жизненным циклом квестов и наградами.
  - `expedition/StartExpeditionUseCase`, `CompleteExpeditionReturnUseCase`: Контроль логистики, экспедиций и выгрузки добычи.
  - `economy/CraftItemUseCase`: Крафт предметов с проверкой ингредиентов.
- **Domain Services (`domain.service.*`)**:
  - `time/GameClock`, `DailyTickProcessor`: Моделирование хода времени, потребления провизии и суточных отчётов.
  - `quest/QuestProgressProcessor`, `QuestManager`: Реактивная обработка цепочек заданий по событиям `GameEvent`.
  - `reputation/ReputationManager`: Динамические уровни отношений с фракциями и перки доверия.
  - `combat/CombatEngine`: Тактическая пошаговая боевая система с расходом ОД, статусами и навыками.
- **Validators (`domain.validator.GameStateValidator`)**:
  - Валидация целостности данных, уникальности ID, корректности ссылок отряда на поселенцев и отсутствия отрицательных ресурсов.

### 2.3 Data Layer (`com.example.data`)
Хранение, источники данных и конфигурации:
- **`GameStateRepository`** (`data.repository`): Единственный авторитетный источник правды (Single Source of Truth) с реактивным `StateFlow<GameState>`, атомарными транзакциями (`Mutex` / sync swap) и управлением сохранениями.
- **`GameSaveDataSource`** (`data.source`): Абстракция хранилища сохранений (в памяти + сериализация) с автоматическим отказоустойчивым fallback на `InitialGameData`.
- **Конфигурации и генераторы**: `InitialGameData`, `ResearchConfig`, `TradeConfig`, `SurvivorGenerator`, `ReputationBalanceConfig`.

### 2.4 Presentation Layer (`com.example.ui`, `com.example.viewmodel`)
Пользовательский интерфейс и состояние экранов:
- **`GameViewModel`**: Оркестрация пользовательских команд, предоставление `StateFlow<GameState>` для UI и координация пошаговых сценариев.
- **Compose Screens**: Модульные экраны с поддержкой Material 3, адаптивной сетки, тёмной/светлой темы и доступности:
  - `SettlementScreen` (Поселение и постройки)
  - `WarehouseScreen` (Склад и инвентарь)
  - `WorldMapScreen` (Карта Пустоши и маршруты)
  - `ExpeditionScreen` & `CombatView` (Экспедиции и тактические бои)
  - `QuestBoardScreen` (Доска заданий и фракции)
  - `WorkshopScreen` (Мастерская и крафт)
  - `ResearchScreen` (Древо технологий)

---

## 3. Ключевые паттерны и гарантии

1. **Unidirectional Data Flow (UDF)**:
   - UI отправляет намерения (Intent/Action) в ViewModel.
   - Use Case / Domain Service рассчитывает новое неизменяемое состояние (`GameState.copy(...)`).
   - `GameStateRepository` атомарно фиксирует изменения и эмитит новое состояние через `StateFlow`.
   - UI автоматически ререндерится.

2. **Изоляция Domain Layer**:
   - Никаких импортов `androidx.compose.*` или `android.graphics.*` в файлах моделей предметной области (`domain.model`).
   - Иконки и визуальные цвета определяются в презентационном слое (`com.example.domain.content.visual` или `ui.theme`).

3. **Event-Driven прогресс квестов (`GameEvent`)**:
   - Любое значимое игровое действие (крафт, завершение экспедиции, победа в бою, улучшение здания) порождает `GameEvent`.
   - `QuestProgressProcessor.process(event, gameState)` обновляет задачи квестов централизованно без дублирования кода.

---

## 4. Конвенции тестирования

Все критические бизнес-правила покрыты модульными тестами в `app/src/test/java/com/example/`:
- **`ArchitectureAndInvariantsTest`**: Тесты реактивности репозитория, параллельной безопасности (`Mutex`), валидатора инвариантов и Use Cases.
- **`GameEngineTest`**, **`QuestSystemTest`**, **`CombatSystemTest`**, **`ReputationSystemTest`**: Доменные функциональные тесты.
