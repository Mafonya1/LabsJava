# Лабораторная работа №1 — «Разработка модели данных»

## Что реализовано

- Иерархия из трёх типов:
  - `Batch` — базовый абстрактный тип;
  - `ImportedBatch` — редактируемая партия с дополнительными полями (`country`, `customsCode`);
  - `ArchivedBatch` — read-only партия.
- Интерфейс `Editable` с методом `List<String> validate()`.
- Загрузка данных из CSV, битые строки пропускаются и показываются в диалоге с кодами ошибок (`BAD_NUMBER`, `WRONG_FIELD_COUNT`, `EMPTY_LINE`, `UNKNOWN_TYPE`).
- Сохранение данных в CSV.
- Добавление и редактирование только редактируемых типов.
- Кнопка «Изменить» блокируется для read-only-объектов (`instanceof Editable`).
- Диалоги ошибок для загрузки, сохранения и валидации.

## Технологии

- Java 21
- JavaFX 21 (FXML)
- Maven
- JUnit 5

## Структура проекта

```
src/main/java/com/warehouse/
├── model/       Batch, ImportedBatch, ArchivedBatch, Editable
├── io/          CsvLoader, CsvSaver, LoadResult, LoadError, CsvLoadException
├── service/     BatchService
└── ui/          MainApp, MainController, BatchDialogController
```

Слои разделены: GUI не содержит бизнес-логики, сервис не знает про JavaFX, модель не знает ни про то, ни про другое.

## Формат CSV

Разделитель — `;`, без строки-заголовка.

```
Archive;article;name;quantity;cell;date           ← архивная партия (6 полей)
article;name;quantity;cell;date;country;code      ← импортная партия (7 полей)
```

Пример `batches.csv`:

```
Archive;101;ПартияАрхив1;15;5;20.09.2026
102;ПартияИмпорт2;15;12;24.09.2026;Китай;CN-123
103;ПартияИмпорт3;20;7;01.10.2026;Германия;DE-456
битая;строка;с;буквами;вместо;чисел
104;ПартияИмпорт4;abc;9;02.10.2026;Турция;TR-789
```

Строки 4 и 5 намеренно битые — на них показывается диалог ошибок.

## Запуск

Требуется JDK 21 и Maven.

```bash
mvn clean package
mvn javafx:run
```

Или открыть проект в IntelliJ IDEA как Maven-проект и запустить `com.warehouse.ui.MainApp`.

## Тесты

```bash
mvn test
```

Покрыты: разбор валидной импортной строки, разбор валидной архивной строки, пропуск битой строки с корректным кодом ошибки.
