# Лабораторная работа №10

## Асинхронность и планировщик: корутины, @Scheduled, Spring Mail

---

## Цель работы

Добавить в проект доставки еды асинхронную обработку задач и плановые фоновые операции: уведомления по email при смене статуса заказа и автоматическая отмена зависших заказов.

---

## Что нужно сдать

Ссылку на PR в ваш репозиторий (шаблон у вас есть).

---

## Теоретический блок

### 1) @Async: старый подход (кратко)

До появления корутин в Kotlin асинхронность в Spring решалась через аннотацию `@Async`. Метод, помеченный `@Async`, выполняется в отдельном потоке из пула — HTTP-ответ уходит клиенту немедленно.

Для включения нужны `@EnableAsync` и, опционально, явно настроенный пул потоков:

```kotlin
@SpringBootApplication
@EnableAsync
class DeliveryApplication

@Service
class NotificationService(private val mailSender: JavaMailSender) {

    @Async
    fun sendEmail(to: String, subject: String, text: String) {
        mailSender.send(SimpleMailMessage().apply {
            setTo(to)
            this.subject = subject
            this.text = text
        })
    }
}
```

Главное ограничение: вызов `@Async`-метода из того же класса не работает — Spring не успевает создать прокси, и метод выполнится синхронно. Это источник трудноуловимых багов.

В современных Kotlin-проектах `@Async` вытеснён корутинами — они гибче, выразительнее и лишены proxy-ловушки. Далее в лабе используем корутины.

---

### 2) Kotlin корутины в Spring Boot

Корутины — механизм конкурентности в Kotlin, который позволяет писать асинхронный код в линейном стиле без callback-ада и лишних потоков. Корутина — это лёгкая «сопрограмма»: она может приостановиться (`suspend`) без блокировки потока и возобновиться позже.

#### Зависимости

```xml
<dependency>
    <groupId>org.jetbrains.kotlinx</groupId>
    <artifactId>kotlinx-coroutines-core</artifactId>
</dependency>
<dependency>
    <groupId>org.jetbrains.kotlinx</groupId>
    <artifactId>kotlinx-coroutines-reactor</artifactId>
</dependency>
```

Версии управляются Spring Boot BOM — указывать не нужно.

`coroutines-core` — основная библиотека корутин. `coroutines-reactor` нужен для интеграции с внутренними механизмами Spring (в частности, чтобы `suspend`-методы в контроллерах работали корректно в Spring MVC).

#### CoroutineScope: правильный способ запуска

Каждая корутина запускается внутри `CoroutineScope` — он отвечает за её жизненный цикл и отмену. Правильный подход — создать scope как Spring-бин и инжектить его в сервисы:

```kotlin
@Configuration
class CoroutineConfig {

    @Bean
    fun applicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
```

`SupervisorJob` — важная деталь: если одна дочерняя корутина падает с исключением, остальные продолжают работу. Без него падение одной отменяет весь scope.

```kotlin
@Service
class NotificationService(
    private val mailSender: JavaMailSender,
    private val scope: CoroutineScope
) {
    private val logger = KotlinLogging.logger {}

    fun sendOrderStatusUpdate(to: String, orderId: Long, status: String) {
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    mailSender.send(SimpleMailMessage().apply {
                        setTo(to)
                        subject = "Заказ #$orderId: статус изменён"
                        text = "Новый статус: $status"
                    })
                }
            }.onFailure { ex ->
                logger.error(ex) { "Не удалось отправить уведомление на $to" }
            }
        }
    }
}
```

`scope.launch` запускает корутину и сразу возвращает управление — `sendOrderStatusUpdate` не блокирует вызывающий код.

#### scope.launch vs withContext

Это два разных инструмента, которые часто путают:

| | `scope.launch { }` | `withContext(Dispatcher) { }` |
|:---|:---|:---|
| Что делает | Запускает **новую** корутину | Переключает диспетчер **внутри** текущей корутины |
| Вызывающий код | Не ждёт — fire-and-forget | Ждёт завершения блока |
| Где вызывать | Из обычной функции или корутины | Только из `suspend`-функции или корутины |
| Возвращает | `Job` (можно отменить) | Результат блока |
| Типичный сценарий | Запустить фоновую задачу и забыть | Сделать блокирующий вызов, не блокируя поток |

Пример, который показывает оба в одном месте:

```kotlin
// Обычная функция — вызывается из OrderService
fun sendOrderStatusUpdate(to: String, orderId: Long, status: String) {
    scope.launch {                           // (1) запускаем новую корутину, не ждём
        withContext(Dispatchers.IO) {         // (2) внутри — переключаемся на IO-поток
            mailSender.send(...)             //     здесь выполняется блокирующий вызов
        }
    }
    // сюда попадаем сразу, не дожидаясь отправки письма
}
```

`scope.launch` отвечает за то, что вызывающий код не блокируется. `withContext` отвечает за то, что блокирующий `mailSender.send` не занимает поток из корутинного пула.

#### suspend-функции

`suspend fun` — функция, которую можно приостанавливать. Её можно вызывать только из другой `suspend`-функции или из корутины. Если нужен результат фоновой операции, используйте `suspend` вместо `launch`:

```kotlin
// Вызвать можно только из корутины или другой suspend-функции
suspend fun fetchRestaurantRating(id: Long): Double {
    return withContext(Dispatchers.IO) {
        externalApiClient.getRating(id)  // блокирующий вызов — безопасно на IO
    }
}
```

#### Что происходит при вызове эндпоинта: пошагово

Возьмём `updateStatus` и проследим выполнение от запроса до ответа для двух версий.

**Версия 1: suspend fun**

```kotlin
@PatchMapping("/orders/{id}/status")
suspend fun updateStatus(@PathVariable id: Long, @RequestBody dto: StatusDto): OrderDto {
    return orderService.updateStatus(id, dto.status)
}

suspend fun updateStatus(id: Long, status: String): OrderDto {
    return withContext(Dispatchers.IO) {  // JPA-репозитории блокирующие
        val order = orderRepository.findById(id)
            ?: throw NotFoundException("Заказ $id не найден")
        order.status = status
        orderRepository.save(order).toDto()
    }
}
```

```
[0 мс]  Клиент → PATCH /orders/1/status
[0 мс]  Spring принимает запрос, выделяет поток
[0 мс]  Контроллер вызывает orderService.updateStatus(1, "DELIVERED")
[0 мс]  Сервис вызывает orderRepository.findById(1)
          → корутина приостанавливается, поток ОСВОБОЖДЁН обратно в пул
[5 мс]  БД ответила → корутина ВОЗОБНОВЛЯЕТСЯ на свободном потоке
[5 мс]  Сервис вызывает orderRepository.save(order)
          → корутина приостанавливается, поток ОСВОБОЖДЁН
[8 мс]  БД ответила → корутина ВОЗОБНОВЛЯЕТСЯ
[8 мс]  Сервис возвращает OrderDto контроллеру
[8 мс]  Spring отправляет HTTP 200 {"id":1, "status":"DELIVERED"}
[8 мс]  Клиент получает корректный ответ ✓

— Если на шаге [5 мс] заказ не найден —
[5 мс]  Сервис бросает NotFoundException
[5 мс]  Исключение всплывает через контроллер в Spring
[5 мс]  @RestControllerAdvice перехватывает → HTTP 404 {"message":"..."}
[5 мс]  Клиент получает корректную ошибку ✓
```

---

**Версия 2: та же логика, но через scope.launch**

```kotlin
@PatchMapping("/orders/{id}/status")
fun updateStatus(@PathVariable id: Long, @RequestBody dto: StatusDto): OrderDto? {
    return orderService.updateStatus(id, dto.status)
}

fun updateStatus(id: Long, status: String): OrderDto? {
    var result: OrderDto? = null
    scope.launch {
        val order = orderRepository.findById(id)
            ?: throw NotFoundException("Заказ $id не найден")
        order.status = status
        result = orderRepository.save(order).toDto()
    }
    return result  // ← выполняется немедленно, до того как корутина что-то сделала
}
```

```
[0 мс]  Клиент → PATCH /orders/1/status
[0 мс]  Spring принимает запрос, выделяет поток
[0 мс]  Контроллер вызывает orderService.updateStatus(1, "DELIVERED")
[0 мс]  Сервис запускает scope.launch { ... }
          → корутина уходит в фон, scope.launch СРАЗУ возвращает Job
[0 мс]  return result → result = null, функция вернула null ✗
[0 мс]  Spring отправляет HTTP 200 null — ответ уже ушёл ✗
[0 мс]  Клиент получил пустой ответ, данные ещё не сохранены

  [параллельно, в фоне]
  [5 мс]  Фоновая корутина: БД вернула заказ
  [8 мс]  Фоновая корутина: заказ сохранён — но клиент об этом не узнает ✗

— Если на шаге [5 мс] заказ не найден —
[5 мс]  Фоновая корутина бросает NotFoundException
[5 мс]  @RestControllerAdvice НЕ перехватит — HTTP-ответ уже ушёл
[5 мс]  Исключение уходит в CoroutineExceptionHandler → лог
[0 мс]  Клиент уже получил HTTP 200, хотя ничего не произошло ✗
```

`scope.launch` физически не может вернуть результат вызывающему коду — функция возвращает `Job`, а не данные. Используйте `suspend fun` везде, где клиент ждёт результат. `scope.launch` — только для работы, которую запустили и забыли.

---

#### Диспетчеры

| Диспетчер            | Для чего                                          |
|:---------------------|:--------------------------------------------------|
| `Dispatchers.IO`     | Блокирующий I/O: БД, файлы, сеть, отправка email |
| `Dispatchers.Default`| CPU-интенсивные задачи: парсинг, вычисления       |
| `Dispatchers.Main`   | UI-поток (не используется в бэкенде)             |

#### Подводные камни

**GlobalScope — не использовать.** `GlobalScope.launch` запускает корутину без привязки к жизненному циклу — при остановке приложения она не будет отменена, возможна утечка ресурсов. Всегда используйте явный scope.

```kotlin
// Плохо
GlobalScope.launch { sendEmail(...) }

// Хорошо
scope.launch { sendEmail(...) }
```

**Блокирующий код без withContext.** Вызов блокирующей операции (JDBC, `mailSender.send`) прямо в корутине на `Dispatchers.Default` заблокирует поток из пула и снизит пропускную способность. Оборачивайте в `withContext(Dispatchers.IO)`.

```kotlin
// Плохо — блокирует поток из Default-пула
scope.launch {
    mailSender.send(message)  // блокирующий вызов
}

// Хорошо
scope.launch {
    withContext(Dispatchers.IO) {
        mailSender.send(message)
    }
}
```

**@Transactional не работает с корутинами.** Spring-транзакции хранятся в `ThreadLocal` и не передаются между потоками. Если нужна транзакция внутри корутины — запустите блокирующую операцию через обычный `@Transactional`-метод в другом бине, не пытайтесь использовать `@Transactional` на `suspend`-функции напрямую.

**CancellationException нельзя поглощать.** Корутины используют `CancellationException` для сигнала об отмене. Если поймаете его в `catch` — не забудьте перебросить:

```kotlin
scope.launch {
    try {
        doWork()
    } catch (e: CancellationException) {
        throw e  // обязательно перебросить
    } catch (e: Exception) {
        logger.error(e) { "Ошибка в фоновой задаче" }
    }
}
```

Альтернатива — `runCatching`, который не перехватывает `CancellationException`.

**Обработка исключений.** Исключение в `launch`-корутине не всплывает в вызывающий код — оно уходит в `CoroutineExceptionHandler` или просто теряется. Этот пункт заслуживает отдельного раздела — см. ниже.

#### Обработка исключений и связь с @RestControllerAdvice

Прежде чем думать об обработке исключений, нужно ответить на вопрос: **важен ли результат операции клиенту прямо сейчас?**

От этого зависит весь подход.

---

**Если результат важен клиенту — используйте `suspend fun`.**

В этом случае операция выполняется в рамках HTTP-запроса. Исключения из `suspend`-функций всплывают по цепочке вызовов до Spring, и `@RestControllerAdvice` перехватывает их как обычно — никаких специальных усилий не нужно:

```kotlin
@RestController
class OrderController(private val orderService: OrderService) {

    @PatchMapping("/orders/{id}/status")
    suspend fun updateStatus(@PathVariable id: Long, @RequestBody dto: StatusDto): OrderDto {
        return orderService.updateStatus(id, dto.status)
    }
}

@Service
class OrderService(private val orderRepository: OrderRepository) {

    suspend fun updateStatus(id: Long, status: String): OrderDto {
        val order = orderRepository.findById(id)
            ?: throw NotFoundException("Заказ $id не найден")  // пробросится наверх
        order.status = status
        return orderRepository.save(order).toDto()
    }
}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(404).body(ErrorResponse(ex.message))  // поймает
}
```

`suspend fun` в сервисе ведёт себя ровно так же, как обычная функция с точки зрения обработки исключений — просто выполняется на IO-потоке.

---

**Если результат клиенту не нужен — используйте `scope.launch` и обрабатывайте ошибки внутри.**

`scope.launch` означает, что вы намеренно отвязываете задачу от HTTP-запроса: клиент получит ответ немедленно, а работа продолжится в фоне. Отправка email при смене статуса — классический пример: клиент получил `200 OK`, заказ обновлён. Произошла ли отправка письма — его уже не касается.

Именно поэтому пробросить исключение из `scope.launch` обратно клиенту **невозможно** — HTTP-ответ уже ушёл. Попытки это сделать — признак того, что задача на самом деле не fire-and-forget, и нужно использовать `suspend`.

Ошибки внутри `scope.launch` обрабатываются локально. Удобный инструмент — `runCatching`, который не перехватывает `CancellationException`:

```kotlin
@Service
class NotificationService(
    private val mailSender: JavaMailSender,
    private val scope: CoroutineScope
) {
    private val logger = KotlinLogging.logger {}

    fun sendOrderStatusUpdate(to: String, orderId: Long, status: String) {
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    mailSender.send(SimpleMailMessage().apply {
                        setTo(to)
                        subject = "Заказ #$orderId: статус изменён"
                        text = "Новый статус: $status"
                    })
                }
            }.onSuccess {
                logger.info { "Уведомление по заказу #$orderId отправлено на $to" }
            }.onFailure { ex ->
                logger.error(ex) { "Не удалось отправить уведомление на $to" }
                // клиенту об этом не сообщаем — он уже получил свой ответ
            }
        }
    }
}
```

---

**`CoroutineExceptionHandler` — страховка для всего scope.**

Это последний рубеж: если в каком-то `launch` забыли обработать исключение, handler на уровне scope поймает его и не даст потеряться молча. Не замена локальной обработке, а дополнение к ней:

```kotlin
@Bean
fun applicationScope(): CoroutineScope {
    val handler = CoroutineExceptionHandler { _, ex ->
        LoggerFactory.getLogger("CoroutineScope")
            .error("Необработанное исключение в фоновой корутине", ex)
    }
    return CoroutineScope(SupervisorJob() + Dispatchers.IO + handler)
}
```

---

**Итог: как выбрать подход.**

| Ситуация | Инструмент | Исключения |
|:----|:----|:----|
| Клиенту нужен результат (найти заказ, создать ресурс) | `suspend fun` | Всплывают в `@RestControllerAdvice` |
| Клиенту не нужен результат (отправить письмо, записать в лог) | `scope.launch` | Обрабатываются внутри корутины |

---

### 3) Планировщик: @Scheduled

`@Scheduled` запускает метод по расписанию — в фоне, независимо от HTTP-запросов.

#### Включение

```kotlin
@SpringBootApplication
@EnableScheduling
class DeliveryApplication
```

> `@EnableAsync` нужен только если вы используете аннотацию `@Async`. При работе с корутинами он не нужен.

#### Варианты расписания

```kotlin
@Scheduled(fixedDelay = 60_000)       // через 60 сек после окончания предыдущего
@Scheduled(fixedRate = 60_000)        // каждые 60 сек, независимо от времени выполнения
@Scheduled(cron = "0 0 3 * * *")      // каждый день в 03:00
```

#### Интервал из конфигурации

Хардкодить интервалы в аннотациях — плохая практика: чтобы поменять, нужно перекомпилировать. Выносите в `application.yaml`:

```yaml
app:
  scheduler:
    stuck-order-interval-ms: 900000   # 15 минут
    stuck-order-threshold-hours: 1
```

```kotlin
@Scheduled(fixedDelayString = "\${app.scheduler.stuck-order-interval-ms}")
fun cancelStuckOrders() { ... }
```

#### Cron-формат

Spring использует 6-польный cron: `секунды минуты часы день_месяца месяц день_недели`.

| Выражение           | Значение             |
|:--------------------|:---------------------|
| `0 * * * * *`       | каждую минуту        |
| `0 0 * * * *`       | каждый час           |
| `0 0 3 * * *`       | каждый день в 03:00  |
| `0 */30 * * * *`    | каждые 30 минут      |
| `0 0 9 * * MON-FRI` | в 09:00 по будням    |

#### Обычная функция, suspend или scope.launch?

**`@Scheduled`-метод всегда должен быть обычной `fun`.** Spring-планировщик не умеет вызывать `suspend`-функции — если пометить `suspend fun` аннотацией `@Scheduled`, Spring вызовет её как обычный Java-метод, получит объект `Continuation` и ничего не выполнит.

Внутри обычного `@Scheduled`-метода у вас три варианта:

**Прямой вызов** — для лёгких операций. Планировщик выполняет работу синхронно. Просто и предсказуемо: `fixedDelay` отсчитывается от момента завершения работы, поэтому запуски никогда не накладываются.

```kotlin
@Scheduled(fixedDelayString = "\${app.scheduler.stuck-order-interval-ms}")
fun cancelStuckOrders() {
    val stuck = orderRepository.findByStatus("PREPARING")  // синхронный вызов
    stuck.forEach { ... }
}
```

**`scope.launch`** — для тяжёлых или долгих операций. Планировщик запускает корутину и сразу возвращается. Работа выполняется на пуле IO-потоков. Важный нюанс: поскольку метод возвращается мгновенно, `fixedDelay` начнёт отсчитывать паузу от возврата метода, а не от завершения корутины. `fixedRate` в этом случае создаёт риск наложения запусков — если предыдущая корутина ещё не завершилась, а интервал истёк, запустится новая. Используйте с осторожностью.

```kotlin
@Scheduled(fixedDelayString = "\${app.scheduler.stuck-order-interval-ms}")
fun cancelStuckOrders() {
    scope.launch {
        val stuck = orderRepository.findByStatus("PREPARING")
        stuck.forEach { ... }
    }
    // возвращается немедленно, корутина работает в фоне
}
```

**`suspend fun` на `@Scheduled`** — не использовать. Не работает.

Итог:

| Сценарий | Что писать |
|:---|:---|
| Быстрая операция (1–2 запроса в БД) | Обычная `fun`, прямой вызов |
| Долгая IO-операция, много записей | `scope.launch` внутри обычной `fun` |
| `suspend fun` прямо на методе | Не работает |

Для примера с отменой зависших заказов достаточно прямого вызова — операция простая. `scope.launch` понадобился бы, если бы заказов было тысячи и обработка каждого занимала заметное время.

Для проверки в разработке временно уменьшите интервал в `application.yaml`.

---

### 4) Spring Mail

#### Зависимость

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

#### JavaMailSender

`SimpleMailMessage` — для простого текста, `MimeMessage` — для HTML и вложений:

```kotlin
val message = SimpleMailMessage().apply {
    setTo(to)
    subject = "Заказ #$orderId: статус изменён"
    text = "Ваш заказ перешёл в статус: $status"
}
mailSender.send(message)
```

---

### 5) Вариант A: Gmail SMTP

Gmail позволяет отправлять письма через SMTP, но требует **пароль приложения** — отдельный 16-символьный пароль, который Google выдаёт для конкретного приложения.

**Создание пароля приложения:**
1. Включите двухфакторную аутентификацию: Google Account → Security → 2-Step Verification.
2. Перейдите: Google Account → Security → **App passwords**.
3. Введите название (например, `spring-delivery`), нажмите Create.
4. Скопируйте 16-символьный пароль — он показывается один раз.

**Конфигурация `application.yaml`:**

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${GMAIL_USERNAME}
    password: ${GMAIL_APP_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

> **Никогда не коммитьте учётные данные в репозиторий.**
>
> `GMAIL_USERNAME` и `GMAIL_APP_PASSWORD` — переменные окружения. Определите их в `.env`-файле (он уже в `.gitignore` с ЛР-8). В `.env.example` оставьте ключи с пустыми значениями — как документацию для других разработчиков.
>
> Если вы случайно закоммитили пароль — **немедленно отзовите его** в настройках Google и создайте новый. История git публична, удалить коммит недостаточно.

---

### 6) Вариант B: MailHog

MailHog — фейковый SMTP-сервер с веб-интерфейсом. Перехватывает все письма локально: ничего не уходит в интернет, никаких реальных аккаунтов.

**Добавить в docker-compose.yaml:**

```yaml
  mailhog:
    image: mailhog/mailhog
    ports:
      - "1025:1025"   # SMTP
      - "8025:8025"   # Web UI
```

**Конфигурация `application.yaml`:**

```yaml
spring:
  mail:
    host: localhost
    port: 1025
```

Откройте `http://localhost:8025` — все отправленные письма появятся там в реальном времени.

---

## Практическое задание

### 1) Добавьте зависимости и включите корутины

Добавьте `kotlinx-coroutines-core` и `kotlinx-coroutines-reactor` в `pom.xml`. Создайте `CoroutineConfig` с `applicationScope` как Spring-бином (`SupervisorJob + Dispatchers.IO`).

### 2) Реализуйте NotificationService

Создайте сервис с методом отправки email при смене статуса заказа. Используйте `scope.launch` и оборачивайте блокирующий `mailSender.send` в `withContext(Dispatchers.IO)`. Добавьте обработку ошибок — ошибка отправки письма не должна ронять основной поток выполнения.

Вызывайте `NotificationService` из `OrderService` при каждом обновлении статуса заказа.

### 3) Настройте Spring Mail

Выберите один вариант: **Gmail** или **MailHog**.

- Для Gmail: создайте App Password, вынесите `GMAIL_USERNAME` и `GMAIL_APP_PASSWORD` в `.env`. Убедитесь, что `.env` в `.gitignore`.
- Для MailHog: добавьте сервис в docker-compose.

Проверьте, что письмо реально доходит: папка «Отправленные» в Gmail или веб-интерфейс MailHog.

### 4) Реализуйте планировщик

Создайте `OrderScheduler` с `@Scheduled`-методом, который:
- находит заказы в статусе `PREPARING`, созданные раньше порогового времени
- переводит их в `CANCELLED`
- логирует количество найденных и каждый отменённый заказ на уровне INFO
- шлёт уведомление пользователю через `NotificationService`

Интервал проверки и пороговое время вынесите в `application.yaml`.

---

## Критерии оценки (максимум 15 баллов)

| Категория          | Критерий                                                                         | Баллы  |
|:-------------------|:---------------------------------------------------------------------------------|:------:|
| Корутины           | `CoroutineScope`-бин, `scope.launch`, `withContext(Dispatchers.IO)`, обработка ошибок | 4 |
| NotificationService| Письмо отправляется при смене статуса, вызов из `OrderService`                   |   3    |
| Spring Mail        | Gmail или MailHog настроен, письмо реально доходит, учётные данные в `.env`      |   4    |
| @Scheduled         | Планировщик отменяет зависшие заказы, INFO-логирование, интервал из `application.yaml` | 4 |
| **Итого**          |                                                                                  | **15** |

---

## Мини-чеклист перед сдачей

1. При смене статуса заказа уходит письмо (видно в MailHog или Gmail «Отправленные»).
2. `NotificationService` использует `scope.launch`, а не `GlobalScope`.
3. Блокирующий `mailSender.send` обёрнут в `withContext(Dispatchers.IO)`.
4. Учётные данные SMTP — в `.env`, не захардкожены в yaml, `.env` в `.gitignore`.
5. Планировщик логирует количество зависших заказов и каждую отмену.
6. Интервал и порог планировщика настраиваются в `application.yaml`.
7. `./mvnw test` проходит без ошибок.

---

## Что почитать

1. [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
2. [Coroutines + Spring Boot](https://spring.io/blog/2019/04/12/going-reactive-with-spring-coroutines-and-kotlin-flow)
3. [Baeldung — Kotlin Coroutines](https://www.baeldung.com/kotlin/coroutines)
4. [Spring @Scheduled](https://docs.spring.io/spring-framework/reference/integration/scheduling.html#scheduling-annotation-support-scheduled)
5. [Spring Mail](https://docs.spring.io/spring-framework/reference/integration/email.html)
6. [Baeldung — @Async](https://www.baeldung.com/spring-async)
7. [Gmail App Passwords](https://support.google.com/accounts/answer/185833)
8. [MailHog](https://github.com/mailhog/MailHog)
