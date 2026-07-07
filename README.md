# VibeRegionGuard

## Кратко

- Зачем создан: приватные регионы для игроков VibeCraft без тяжелой внешней системы.
- Что делает: регионы, участники, флаги, GUI, `/vp`, `/privat`, `/privatadmin`, `/home`, `/sethome`, SQLite/MySQL.
- Совместимость: внутренние Java package/API сохранены как `com.vibeprivate` и `VibePrivateAPI`, плюс добавлен внешний тонкий фасад `VibeRegionGuardApi`.
- Риски: ошибки защиты могут дать гриферство или заблокировать игроков; миграции БД и CMI-команды надо тестировать отдельно.
- Проблемы/баги: нужен полный серверный smoke на Paper/Purpur, автоматические тесты не проверяют реальные Bukkit GUI.
- Статус: API/lifecycle/transfer foundation собран и покрыт тестами; до release candidate еще нужны серверный smoke, GUI pagination и CLAN identity checkpoint.

## Сборка

```bash
./gradlew build
```

## С чего начинать разработку

1. Открыть `docs/ARCHITECTURE_MAP.md`.
2. Проверить `git status --short --branch`.
3. Работать одним слоем за проход: API, lifecycle, storage, GUI, commands или protection.
4. Для внешних интеграций использовать `getVibeRegionGuardApi()` или `getApi()`, не repositories/SQL/reflection.

## Мини-проверка

1. Поставить jar на Paper 1.21.x.
2. Проверить `/vp`, создание региона, добавление участника.
3. Проверить флаги защиты: блоки, контейнеры, урон, телепорт.
4. Проверить перезапуск сервера и сохранение регионов.
