# Backpacks Mod для Minecraft NeoForge 1.21.1

Мод добавляет сумки с дополнительным хранилищем предметов.

## Возможности

- **9 слотов** для хранения предметов в сумке
- **Экипировка через клавишу** (по умолчанию `B`) - не занимает слоты брони
- **Замедление от заполненности**: начиная с 5-го слота игрок получает замедление, которое растет экспоненциально до 30% на 9-м слоте
- **Замедление под водой**: дополнительно -15% скорости под водой из-за худшей обтекаемости
- **Открытие без удержания в руке**: нажмите клавишу экипировки чтобы открыть GUI сумки
- **Кастомная модель**: сумка отображается на спине игрока (используется bbmodel-подобная структура)
- **Нет зависимостей**: мод работает самостоятельно

## Крафт

```
LLL
LSL
LLL
```

- L = Кожа (Leather)
- S = Верёвка (String)

## Управление

- **B** (по умолчанию) - Экипировать/снять сумку и открыть интерфейс

## Установка

1. Установите Minecraft 1.21.1 с NeoForge 21.1.65+
2. Поместите файл мода `.jar` в папку `mods`
3. Запустите игру

## Настройка

Конфигурация находится в `config/backpacks-common.toml`:
- `slowdownStartSlot` - слот, с которого начинается замедление (1-9)
- `maxSlowdownPercent` - максимальное замедление (0.0-1.0)
- `underwaterSlowdownPercent` - замедление под водой (0.0-1.0)

## Для разработчиков

### Сборка

```bash
./gradlew build
```

### Структура проекта

```
backpacks-mod/
├── src/main/java/com/example/backpacks/
│   ├── BackpacksMod.java         # Основной класс мода
│   ├── ModEvents.java            # События сервера (замедление)
│   ├── item/
│   │   └── BackpackItem.java     # Предмет сумки
│   ├── capability/
│   │   └── BackpackCapabilityProvider.java  # Хранилище предметов
│   ├── client/
│   │   ├── BackpackScreen.java   # GUI сумки
│   │   ├── BackpackMenu.java     # Контейнер сумки
│   │   ├── BackpackModel.java    # 3D модель сумки
│   │   ├── BackpackKeyHandler.java # Обработка клавиш
│   │   └── ClientModEvents.java  # Клиентские события
│   ├── config/
│   │   └── BackpackConfig.java   # Конфигурация
│   └── network/
│       └── BackpackNetwork.java  # Сетевая синхронизация
└── src/main/resources/
    ├── META-INF/neoforge.mods.toml
    ├── assets/backpacks/         # Ресурсы (текстуры, модели, языки)
    └── data/backpacks/           # Рецепты и лут-таблицы
```

### Добавление bbmodel

Для использования собственной модели из Blockbench:
1. Экспортируйте модель в формат Java для Minecraft
2. Замените содержимое `BackpackModel.java` на код из экспорта
3. Обновите текстуру в `assets/backpacks/textures/entity/backpack.png`

## Лицензия

MIT License
