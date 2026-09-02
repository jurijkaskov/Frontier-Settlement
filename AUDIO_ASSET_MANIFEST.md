# Реестр звуковых ассетов Frontier Settlement (Пункт 37)

## 1. Звуковые эффекты интерфейса (UI)

| Semantic ID | Ресурс (`res/raw`) | Категория | Default Vol | Loop | Описание |
|---|---|---|---|---|---|
| `UI_CLICK` | `sfx_ui_click.wav` | UI | 0.70 | Нет | Короткий чистый клик по кнопке |
| `UI_CONFIRM` | `sfx_ui_confirm.wav` | UI | 0.85 | Нет | Двухтональное подтверждение выбора |
| `UI_CANCEL` | `sfx_ui_cancel.wav` | UI | 0.75 | Нет | Нисходящий тон отмены/закрытия |
| `UI_TAB` | `sfx_ui_tab.wav` | UI | 0.60 | Нет | Легкий переключатель вкладок |
| `UI_TOGGLE` | `sfx_ui_toggle.wav` | UI | 0.65 | Нет | Механический щелчок тумблера/чекбокса |
| `UI_ERROR` | `sfx_ui_error.wav` | UI | 0.80 | Нет | Низкий предупреждающий сигнал ошибки |
| `UI_WARNING` | `sfx_ui_warning.wav` | UI | 0.85 | Нет | Предупреждающий сигнал нехватки ресурсов |

---

## 2. Поселение, производство и прогрессия

| Semantic ID | Ресурс (`res/raw`) | Категория | Default Vol | Loop | Описание |
|---|---|---|---|---|---|
| `RESOURCE_GAIN` | `sfx_resource_gain.wav` | SFX | 0.75 | Нет | Восходящий звон сбора ресурсов |
| `DAILY_REPORT` | `sfx_daily_report.wav` | UI | 0.80 | Нет | Чистый гонг сводки дня |
| `NEW_DAY` | `sfx_new_day.wav` | SFX | 0.90 | Нет | Гармоничный аккорд рассвета нового дня |
| `BUILDING_CONSTRUCT` | `sfx_building_construct.wav` | SFX | 0.90 | Нет | Удар строительного молота/каркаса |
| `BUILDING_UPGRADE` | `sfx_building_upgrade.wav` | SFX | 0.90 | Нет | Восходящий аккорд завершения улучшения |
| `WORKSHOP_CRAFT` | `sfx_craft.wav` | SFX | 0.85 | Нет | Механический стук станка в мастерской |
| `WORKSHOP_REPAIR` | `sfx_repair.wav` | SFX | 0.85 | Нет | Звон гаечного ключа и деталей |
| `RESEARCH_COMPLETE` | `sfx_research_complete.wav` | SFX | 0.95 | Нет | Электронный триумфальный импульс науки |

---

## 3. Торговля, задания и дипломатия

| Semantic ID | Ресурс (`res/raw`) | Категория | Default Vol | Loop | Описание |
|---|---|---|---|---|---|
| `TRADE_BUY` | `sfx_trade_buy.wav` | SFX | 0.80 | Нет | Звон кредитных жетонов при покупке |
| `TRADE_SELL` | `sfx_trade_sell.wav` | SFX | 0.80 | Нет | Звон сдачи при продаже товаров |
| `REPUTATION_INCREASE` | `sfx_reputation_up.wav` | SFX | 0.85 | Нет | Теплый аккорд роста доверия фракции |
| `QUEST_ACCEPTED` | `sfx_quest_accept.wav` | SFX | 0.85 | Нет | Взятие контракта на исполнение |
| `QUEST_OBJECTIVE_COMPLETE` | `sfx_quest_objective.wav` | SFX | 0.85 | Нет | Выполнение промежуточной цели |
| `QUEST_COMPLETED` | `sfx_quest_complete.wav` | SFX | 1.00 | Нет | Победный аккорд завершения квеста |
| `QUEST_FAILED` | `sfx_quest_failed.wav` | SFX | 0.85 | Нет | Глухой звук провала задания |

---

## 4. Карта, переход и события

| Semantic ID | Ресурс (`res/raw`) | Категория | Default Vol | Loop | Описание |
|---|---|---|---|---|---|
| `LOCATION_DISCOVERED` | `sfx_location_discovered.wav` | SFX | 0.90 | Нет | Таинственный импульс открытия точки |
| `TRAVEL_STEP` | `sfx_travel_step.wav` | SFX | 0.60 | Нет | Шаг по гравию/пустоши |
| `VEHICLE_ENGINE` | `sfx_vehicle_engine.wav` | SFX | 0.70 | Нет | Низкий гул дизельного двигателя |
| `EVENT_REVEAL` | `sfx_event_reveal.wav` | SFX | 0.85 | Нет | Аккорд неожиданного события |
| `EVENT_POSITIVE_RESULT` | `sfx_event_positive.wav` | SFX | 0.90 | Нет | Положительный исход проверки навыка |
| `EVENT_NEGATIVE_RESULT` | `sfx_event_negative.wav` | SFX | 0.90 | Нет | Потеря при неудаче выбора |

---

## 5. Добыча и контейнеры (Loot)

| Semantic ID | Ресурс (`res/raw`) | Категория | Default Vol | Loop | Описание |
|---|---|---|---|---|---|
| `LOOT_REVEAL` | `sfx_loot_reveal.wav` | SFX | 0.85 | Нет | Открытие ящика/контейнера |
| `LOOT_PICK` | `sfx_loot_pick.wav` | UI | 0.65 | Нет | Забор отдельного предмета |
| `LOOT_RARE` | `sfx_loot_rare.wav` | SFX | 0.95 | Нет | Звонкий мажорный аккорд редкого снаряжения |
| `LOOT_TAKE_ALL` | `sfx_loot_take_all.wav` | UI | 0.85 | Нет | Быстрый сбор всех трофеев |

---

## 6. Тактический пошаговый бой (Combat)

| Semantic ID | Ресурс / Варианты | Категория | Default Vol | Loop | Описание |
|---|---|---|---|---|---|
| `COMBAT_ATTACK_MELEE` | `sfx_combat_melee_01..03` | SFX | 0.90 | Нет | Свист клинка и глухой удар (3 варианта) |
| `COMBAT_SHOT_PISTOL` | `sfx_combat_pistol_01..02` | SFX | 0.90 | Нет | Четкий выстрел пистолета (2 варианта) |
| `COMBAT_SHOT_RIFLE` | `sfx_combat_rifle_01..02` | SFX | 0.95 | Нет | Выстрел автоматической винтовки (2 вар.) |
| `COMBAT_SHOT_SHOTGUN` | `sfx_combat_shotgun.wav` | SFX | 1.00 | Нет | Мощный раскатистый залп картечи |
| `COMBAT_SHOT_HEAVY` | `sfx_combat_heavy.wav` | SFX | 1.00 | Нет | Тяжелый взрывной выстрел |
| `COMBAT_HIT` | `sfx_combat_hit_01..03` | SFX | 0.90 | Нет | Физическое попадание по цели (3 вар.) |
| `COMBAT_MISS` | `sfx_combat_miss.wav` | SFX | 0.70 | Нет | Свист пули мимо цели |
| `COMBAT_ARMOR_BLOCK` | `sfx_combat_block.wav` | SFX | 0.85 | Нет | Металлический рикошет брони |
| `COMBAT_HEAL` | `sfx_combat_heal.wav` | SFX | 0.85 | Нет | Использование стимпака/бинтов |
| `COMBAT_BUFF` | `sfx_combat_buff.wav` | SFX | 0.80 | Нет | Положительный тактический статус |
| `COMBAT_DEBUFF` | `sfx_combat_debuff.wav` | SFX | 0.85 | Нет | Негативный статус/кровотечение |
| `COMBAT_STATUS_EXPIRE` | `sfx_combat_status_expire.wav` | SFX | 0.65 | Нет | Окончание действия эффекта |
| `COMBAT_TURN_PLAYER` | `sfx_combat_turn_player.wav` | UI | 0.60 | Нет | Сигнал начала хода игрока |
| `COMBAT_TURN_ENEMY` | `sfx_combat_turn_enemy.wav` | UI | 0.60 | Нет | Сигнал начала хода противников |
| `COMBAT_VICTORY_STING` | `sfx_combat_victory.wav` | SFX | 1.00 | Нет | Победный аккорд завершения боя |
| `COMBAT_DEFEAT_STING` | `sfx_combat_defeat.wav` | SFX | 0.95 | Нет | Драматичный аккорд поражения |

---

## 7. Фоновая музыка (Music Tracks)

| Semantic ID | Ресурс (`res/raw`) | Default Vol | Loop | Тональность & Описание |
|---|---|---|---|---|
| `SETTLEMENT` | `music_settlement_01.wav` | 0.75 | Да | A-минор/мажор, спокойная гитарно-синтезаторная тема выживания |
| `WORLD_MAP` | `music_world_map_01.wav` | 0.70 | Да | D-минор, протяжная тема просторов и пустоши |
| `EXPLORATION` | `music_exploration_01.wav` | 0.75 | Да | C-минор, приглушенный ритм и напряженность руин |
| `COMBAT` | `music_combat_01.wav` | 0.85 | Да | E-минор, тактический боевой пульс, перкуссия |
| `VICTORY` | `music_victory_01.wav` | 0.90 | Нет | C-мажор, триумфальная концовка вылазки |
| `DEFEAT` | `music_defeat_01.wav` | 0.85 | Нет | F-минор, меланхоличный реквием погибшему отряду |
| `MAIN_MENU` | `music_main_menu_01.wav` | 0.70 | Да | D-минор, заглавная тема горизонта |
| `SILENT` | *None* | 0.00 | Нет | Тишина |

---

## 8. Непрерывный эмбиент (Ambient Loops)

| Semantic ID | Ресурс (`res/raw`) | Default Vol | Loop | Акустические слои |
|---|---|---|---|---|
| `SETTLEMENT_DAY` | `amb_settlement_day.wav` | 0.80 | Да | Степной ветер, дизельный генератор, далекие удары металла |
| `SETTLEMENT_NIGHT` | `amb_settlement_night.wav` | 0.70 | Да | Приглушенный ветер, ночные сверчки, редкий вой вдали |
| `RUINS` | `amb_ruins.wav` | 0.85 | Да | Сквозняк в разрушенных зданиях, скрип арматуры, сыпь |
| `INDUSTRIAL` | `amb_industrial.wav` | 0.85 | Да | Низкий резонансный гул ангаров, капли, скрип кранов |
| `FOREST` | `amb_forest.wav` | 0.75 | Да | Шелест мертвой листвы, свист ветра в кронах |
| `ROAD` | `amb_road.wav` | 0.80 | Да | Открытый песчаный ветер, сухой шелест гравия |
| `STORM` | `amb_storm.wav` | 0.90 | Да | Шквальный вой пылевой бури, удары песчинок |
| `SILENT` | *None* | 0.00 | Нет | Тишина |
