# Grandmastery

[![codecov](https://codecov.io/gh/LostHikking/grandmastery/branch/main/graph/badge.svg?token=1NT8RP2OJY)](https://codecov.io/gh/LostHikking/grandmastery)
[![example workflow](https://github.com/LostHikking/grandmastery/actions/workflows/gradle.yml/badge.svg)](https://github.com/LostHikking/grandmastery/actions)

Grandmastery - приложение для игры в шахматы, написанное на Java.

## Linter

Для проверки кода на соответствие правилам используется Gradle [checkstyle](https://docs.gradle.org/current/userguide/checkstyle_plugin.html) плагин и xml [файл](./config/checkstyle/checkstyle.xml) с описанием правил.

Что бы упростить работу с проектом советуется установить [google-java-format](https://plugins.jetbrains.com/plugin/8527-google-java-format) плагин для Idea, который форматирует код по стандартам google. Проверить проходит ли код линтер можно командой:

Для основного кода:
```bash
./gradlew checkstyleMain
```

Для тестов:
```bash
./gradlew checkstyleTest
```

Для всего проекта:
```bash
./gradlew build
```