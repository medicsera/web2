# Лабораторная работа №9

## Кэширование: Redis и Spring Cache

---

## Цель работы

Добавить в проект доставки еды из ЛР-8 слой кэширования: подключить Redis, настроить Spring Cache и закэшировать горячие read-эндпоинты. Реализовать инвалидацию кэша при изменении данных.

---

## Что нужно сдать

Ссылку на PR в ваш репозиторий (шаблон у вас есть).

---

## Теоретический блок

### 1) Зачем нужен кэш

Каждый GET-запрос в вашем сервисе сейчас идёт в PostgreSQL. Большинство данных при этом не меняется между запросами: список ресторанов, меню конкретного заведения, информация о блюде. Если к API обратятся 100 пользователей подряд с запросом `GET /api/v1/restaurants`, БД выполнит один и тот же SELECT 100 раз.

Кэш решает это: результат первого запроса сохраняется во временном хранилище. Следующие 99 запросов получают ответ из кэша — без обращения к БД. Выигрыш двойной:
- **Latency** — ответ из Redis приходит за ~1 мс против ~10–50 мс из БД.
- **Нагрузка** — БД разгружается и может обслуживать записи и сложные запросы.

Кэш не серебряная пуля. Он добавляет сложность: нужно следить за актуальностью данных, настраивать TTL, думать об инвалидации. Кэшировать стоит данные, которые:
- Читаются часто (hot path)
- Изменяются редко
- Дорого вычисляются

Список ресторанов и меню — хрестоматийный пример таких данных.

---

### 2) Redis: что это и зачем

**Redis** (Remote Dictionary Server) — хранилище данных в памяти (in-memory). В отличие от PostgreSQL, Redis не пишет каждую операцию на диск синхронно — он держит все данные в RAM, поэтому работает на порядки быстрее.

Redis не только кэш: это многофункциональное хранилище со своими структурами данных:

| Структура | Команды | Пример использования |
|:--|:--|:--|
| String | GET, SET, INCR | Кэш одного объекта, счётчики |
| Hash | HGET, HSET | Кэш объекта с полями |
| List | LPUSH, RPOP | Очереди задач |
| Set | SADD, SMEMBERS | Уникальные значения |
| Sorted Set | ZADD, ZRANGE | Рейтинги, топы |
| Stream | XADD, XREAD | Событийный лог |

В рамках этой лабораторной Redis используется как кэш для Spring Cache — взаимодействие идёт через Spring-абстракцию, а не напрямую через Redis-команды.

#### Подключение Redis через docker-compose

Добавьте Redis в ваш `docker-compose.yml`:

```yaml
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 5

volumes:
  redis_data:
```

Флаг `--appendonly yes` включает AOF-персистентность: Redis записывает каждую команду в файл, чтобы данные пережили рестарт.

---

### 3) Spring Cache: абстракция над хранилищем

Spring Cache — это слой абстракции, который позволяет добавить кэширование через аннотации, не завязываясь на конкретное хранилище. Один и тот же код будет работать с Redis, Caffeine (in-memory), EhCache или любым другим провайдером — нужно только поменять конфигурацию.

#### Зависимости

```xml
<!-- Starter для Spring Cache + Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

#### Включение кэша

Добавьте `@EnableCaching` в конфигурационный класс или главный класс приложения:

```kotlin
@SpringBootApplication
@EnableCaching
class DeliveryApplication

fun main(args: Array<String>) {
    runApplication<DeliveryApplication>(*args)
}
```

#### Настройка в application.yaml

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  cache:
    type: redis
    redis:
      time-to-live: 300000  # 5 минут в миллисекундах
```

---

### 4) Конфигурация Redis как кэша

По умолчанию Spring Data Redis сериализует объекты через Java Serialization — это неудобно (классы должны реализовывать `Serializable`) и нечитаемо в Redis-клиенте. Лучше использовать JSON через Jackson:

```kotlin
@Configuration
class CacheConfig {

    @Bean
    fun redisCacheManagerBuilderCustomizer(
        objectMapper: ObjectMapper
    ): RedisCacheManagerBuilderCustomizer {
        val serializer = GenericJackson2JsonRedisSerializer(objectMapper)
        val serializationPair = RedisSerializationContext.SerializationPair
            .fromSerializer(serializer)

        return RedisCacheManagerBuilderCustomizer { builder ->
            builder.cacheDefaults(
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(5))
                    .serializeValuesWith(serializationPair)
                    .disableCachingNullValues()
            )
        }
    }
}
```

Что настраивается:
- **Сериализация**: JSON вместо Java Serialization — данные читаемы в `redis-cli`.
- **TTL**: время жизни записи. После истечения Redis удаляет её автоматически.
- **Null values**: `disableCachingNullValues()` — не кэшируем `null`, иначе кэшируется "объект не найден" и при следующем создании клиент получит `null` из кэша вместо реального объекта.

> **Важно для Kotlin**: Jackson по умолчанию не знает о Kotlin-специфике (data class, nullable типы). Убедитесь, что в проекте подключён `jackson-module-kotlin`:
> ```xml
> <dependency>
>     <groupId>com.fasterxml.jackson.module</groupId>
>     <artifactId>jackson-module-kotlin</artifactId>
> </dependency>
> ```
> Spring Boot подключает его автоматически, если он есть в classpath.

---

### 5) @Cacheable: кэширование результата метода

`@Cacheable` — основная аннотация. При первом вызове метода результат сохраняется в кэше. При последующих вызовах с теми же параметрами метод не выполняется, а результат берётся из кэша.

```kotlin
@Service
class RestaurantService(
    private val restaurantRepository: RestaurantRepositoryPort
) {
    private val logger = KotlinLogging.logger {}

    @Cacheable(cacheNames = ["restaurants"])
    fun getAll(): List<Restaurant> {
        logger.info { "Загрузка всех ресторанов из БД" }
        return restaurantRepository.findAll()
    }

    @Cacheable(cacheNames = ["restaurants"], key = "#id")
    fun getById(id: Long): Restaurant {
        logger.info { "Загрузка ресторана id=$id из БД" }
        return restaurantRepository.findById(id)
            ?: throw NotFoundException("Ресторан с id=$id не найден")
    }
}
```

#### Как формируется ключ кэша

Redis хранит данные в виде пар ключ-значение. Spring Cache автоматически формирует ключ из имени кэша и параметров метода:

- `getAll()` → ключ: `restaurants::SimpleKey []`
- `getById(1L)` → ключ: `restaurants::1`
- `getById(2L)` → ключ: `restaurants::2`

Параметр `key` принимает SpEL-выражение. `#id` — значение параметра с именем `id`:

```kotlin
// #id — значение параметра id
@Cacheable(cacheNames = ["dishes"], key = "#restaurantId")
fun getDishesByRestaurant(restaurantId: Long): List<Dish>

// Составной ключ
@Cacheable(cacheNames = ["orders"], key = "#userId + '_' + #status")
fun getOrdersByUserAndStatus(userId: Long, status: OrderStatus): List<Order>
```

> **Логирование помогает проверить работу кэша.** Добавьте `logger.info` перед обращением к репозиторию. Если при повторном запросе лог не появляется — метод не вызывается, данные идут из кэша. Это нагляднее, чем смотреть на время ответа.

---

### 6) @CacheEvict: инвалидация кэша

Кэшированные данные устаревают при изменении. Если создать новый ресторан — список ресторанов в кэше содержит старые данные. `@CacheEvict` удаляет записи из кэша при вызове метода.

```kotlin
@CacheEvict(cacheNames = ["restaurants"], allEntries = true)
fun create(command: CreateRestaurantCommand): Restaurant {
    val restaurant = restaurantRepository.save(command.toEntity())
    logger.info { "Создан ресторан id=${restaurant.id}, кэш инвалидирован" }
    return restaurant
}

@CacheEvict(cacheNames = ["restaurants"], allEntries = true)
fun update(id: Long, command: UpdateRestaurantCommand): Restaurant {
    val restaurant = restaurantRepository.findById(id)
        ?: throw NotFoundException("Ресторан с id=$id не найден")
    return restaurantRepository.save(restaurant.apply(command))
}

@CacheEvict(cacheNames = ["restaurants"], allEntries = true)
fun delete(id: Long) {
    if (!restaurantRepository.existsById(id)) {
        throw NotFoundException("Ресторан с id=$id не найден")
    }
    restaurantRepository.deleteById(id)
}
```

`allEntries = true` — удалить все записи в кэше `restaurants`. Это грубо, но надёжно: после создания ресторана неизвестно, какие конкретно ключи устарели (например, постраничные запросы). Для простой модели это правильный подход.

Если нужно удалить только конкретную запись:

```kotlin
// Удалить только ресторан с этим id
@CacheEvict(cacheNames = ["restaurants"], key = "#id")
fun delete(id: Long)
```

#### Инвалидация связанных кэшей

Когда меняется блюдо — устаревает не только кэш этого блюда, но и кэш меню ресторана. Для инвалидации нескольких кэшей используйте `@Caching`:

```kotlin
@Caching(evict = [
    CacheEvict(cacheNames = ["dishes"], key = "#restaurantId"),
    CacheEvict(cacheNames = ["restaurants"], allEntries = true)
])
fun addDish(restaurantId: Long, command: CreateDishCommand): Dish {
    val restaurant = restaurantRepository.findById(restaurantId)
        ?: throw NotFoundException("Ресторан с id=$restaurantId не найден")
    return dishRepository.save(command.toEntity(restaurant))
}
```

---

### 7) @CachePut: обновление кэша

`@CachePut` выполняет метод **всегда** и сохраняет результат в кэш. В отличие от `@Cacheable`, не пропускает вызов метода. Удобно при обновлении: вместо инвалидации сразу кладём новое значение.

```kotlin
// Вместо @CacheEvict + повторного обращения к БД:
@CachePut(cacheNames = ["restaurants"], key = "#id")
fun update(id: Long, command: UpdateRestaurantCommand): Restaurant {
    val restaurant = restaurantRepository.findById(id)
        ?: throw NotFoundException("Ресторан с id=$id не найден")
    return restaurantRepository.save(restaurant.apply(command))
}
```

Сравнение трёх аннотаций:

| Аннотация | Метод выполняется? | Кэш обновляется? | Когда использовать |
|:--|:--:|:--:|:--|
| `@Cacheable` | Только при кэш-промахе | Да (при промахе) | Чтение |
| `@CacheEvict` | Всегда | Нет (удаляет) | Создание, удаление |
| `@CachePut` | Всегда | Да (обновляет) | Обновление |

---

### 8) Разные TTL для разных кэшей

Разные данные устаревают с разной скоростью. Список ресторанов меняется редко — его можно кэшировать на час. Данные заказа меняются часто — на минуту или вообще не кэшировать.

```kotlin
@Configuration
class CacheConfig {

    @Bean
    fun redisCacheManagerBuilderCustomizer(
        objectMapper: ObjectMapper
    ): RedisCacheManagerBuilderCustomizer {
        val serializer = GenericJackson2JsonRedisSerializer(objectMapper)
        val serializationPair = RedisSerializationContext.SerializationPair
            .fromSerializer(serializer)

        fun config(ttl: Duration) = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ttl)
            .serializeValuesWith(serializationPair)
            .disableCachingNullValues()

        return RedisCacheManagerBuilderCustomizer { builder ->
            builder
                .withCacheConfiguration("restaurants", config(Duration.ofHours(1)))
                .withCacheConfiguration("dishes", config(Duration.ofHours(1)))
                .cacheDefaults(config(Duration.ofMinutes(5)))
        }
    }
}
```

---

### 9) Проверка кэша через redis-cli

Убедиться, что данные реально попадают в Redis, можно через встроенный CLI:

```bash
# Зайти в контейнер с Redis
docker compose exec redis redis-cli

# Список всех ключей
KEYS *

# Посмотреть тип значения
TYPE "restaurants::SimpleKey []"

# Посмотреть значение (JSON, если настроена Jackson-сериализация)
GET "restaurants::SimpleKey []"

# Посмотреть TTL (в секундах, -1 = без TTL, -2 = ключ не существует)
TTL "restaurants::SimpleKey []"
```

---

### 10) Тестирование кэширования

Кэш нужно тестировать явно — иначе вы не узнаете, что аннотации расставлены правильно и инвалидация работает как надо. Главное, что нужно проверить:

- **Cache miss → cache hit**: первый вызов идёт в БД, второй — из кэша.
- **Eviction**: после create/update/delete следующий вызов снова идёт в БД.
- **Изоляция тестов**: кэш между тестами очищается, чтобы они не влияли друг на друга.

Для этого нужен **реальный Redis** в тестах — используем Testcontainers (Postgres в тестах вы уже поднимали так же):

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestaurantCacheTest {

    companion object {
        @Container
        @JvmStatic
        val redis = GenericContainer("redis:7-alpine")
            .withExposedPorts(6379)

        @DynamicPropertySource
        @JvmStatic
        fun redisProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redis.host }
            registry.add("spring.data.redis.port") { redis.firstMappedPort }
        }
    }

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var cacheManager: CacheManager

    @BeforeEach
    fun clearCache() {
        // сбрасываем кэш перед каждым тестом — изоляция
        cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `повторный запрос списка ресторанов не идёт в БД`() {
        mockMvc.perform(get("/api/v1/restaurants")).andExpect(status().isOk)
        mockMvc.perform(get("/api/v1/restaurants")).andExpect(status().isOk)
        // проверяем, что в кэше есть данные (промах не произошёл второй раз)
        val cache = cacheManager.getCache("restaurants")
        assertNotNull(cache?.get(SimpleKey.EMPTY))
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `создание ресторана инвалидирует кэш`() {
        // заполняем кэш
        mockMvc.perform(get("/api/v1/restaurants")).andExpect(status().isOk)
        assertNotNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))

        // создаём ресторан — кэш должен быть сброшен
        mockMvc.perform(
            post("/api/v1/restaurants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name": "Новый", "address": "ул. Теста, 1"}""")
        ).andExpect(status().isCreated)

        // кэш пуст — следующий GET пойдёт в БД
        assertNull(cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY))
    }
}
```

> `cacheManager.getCache("restaurants")?.get(SimpleKey.EMPTY)` — проверяет запись для метода без параметров (`getAll()`). Для методов с параметром ключ другой: `cacheManager.getCache("restaurants")?.get(1L)` для `getById(1L)`.

Паттерн тестирования простой: **arrange** (заполнить кэш первым запросом) → **act** (мутировать данные) → **assert** (кэш пуст или обновлён через `cacheManager`).

---

## Практическое задание

### 1) Подключите Redis

1. Добавьте Redis в `docker-compose.yml` с healthcheck.
2. Добавьте зависимости `spring-boot-starter-data-redis` и `spring-boot-starter-cache` в `pom.xml`.
3. Добавьте `@EnableCaching` в главный класс приложения.
4. Вынесите хост и порт Redis в переменные окружения в `application.yaml`.

### 2) Настройте CacheConfig

1. Создайте конфигурацию с JSON-сериализацией через `GenericJackson2JsonRedisSerializer`.
2. Задайте разный TTL для кэшей `restaurants` и `dishes` (минимум 5 минут).
3. `disableCachingNullValues()` обязателен.

### 3) Определите и закэшируйте часто читаемые данные

Проанализируйте эндпоинты проекта и выберите те, которые стоит кэшировать. Ориентируйтесь на критерии из теоретического блока. Закэшируйте не менее трёх методов в сервисном слое.

### 4) Реализуйте инвалидацию

1. При создании ресторана — сбросить весь кэш `restaurants`.
2. При обновлении ресторана — сбросить кэш `restaurants` (или обновить через `@CachePut`).
3. При удалении ресторана — сбросить кэш `restaurants` и `dishes`.
4. При добавлении/обновлении/удалении блюда — сбросить кэш `dishes` для этого ресторана.

### 5) Убедитесь в работе кэша

1. Добавьте `logger.info` перед обращением в репозиторий в закэшированных методах.
2. При двух одинаковых GET-запросах логи не должны повторяться.

### 6) Напишите тесты на кэширование

Используйте Testcontainers с Redis (по аналогии с Postgres в ЛР-6).

1. Добавьте зависимость `org.testcontainers:testcontainers` (если ещё нет).
2. Поднимите Redis-контейнер через `GenericContainer` и пробросьте порт через `@DynamicPropertySource`.
3. В `@BeforeEach` сбрасывайте кэш через `cacheManager` — это гарантирует изоляцию между тестами.
4. Напишите минимум три теста:
    - Повторный GET не идёт в БД (кэш-хит виден через `cacheManager.getCache(...)`).
    - После создания/удаления сущности запись в кэше отсутствует.
    - После update кэш содержит обновлённые данные (если используете `@CachePut`).
5. Убедитесь, что все тесты из ЛР-6/7 по-прежнему проходят — подключение Redis не должно ломать существующую логику.

---

## Критерии оценки (максимум 25 баллов)

| Категория       | Критерий                                                                           | Баллы  |
|:----------------|:-----------------------------------------------------------------------------------|:------:|
| Штраф           | Не проходят тесты из предыдущих ЛР                                                 |   -5   |
| Redis в compose | Redis поднимается с healthcheck, хост/порт в env                                   |   3    |
| CacheConfig     | JSON-сериализация, TTL, disableCachingNullValues                                   |   3    |
| @Cacheable      | Кэш для `getAll`, `getById` ресторана и блюд по ресторану                          |   5    |
| @CacheEvict     | Инвалидация при create/update/delete ресторанов и блюд                             |   5    |
| Связанные кэши  | Удаление блюда инвалидирует кэш и `dishes`, и `restaurants`                        |   3    |
| Проверка кэша   | Логирование + демонстрация через redis-cli или тест                                |   3    |
| Тесты           | Testcontainers Redis, изоляция через `cacheManager.clear()`, ≥3 теста на кэш/evict |   2    |
| Качество        | Чистота кода, конфигурация через env                                               |   1    |
| **Итого**       |                                                                                    | **25** |

---

## Мини-чеклист перед сдачей

1. `docker compose up` поднимает Redis без ошибок, healthcheck зелёный.
2. `GET /api/v1/restaurants` дважды подряд — лог "загрузка из БД" появляется только один раз.
3. `POST /api/v1/restaurants` — после создания следующий `GET /api/v1/restaurants` снова идёт в БД (кэш сброшен).
4. `docker compose exec redis redis-cli KEYS *` — видны ключи вида `restaurants::*`, `dishes::*`.
5. Значения в Redis — читаемый JSON, не бинарные данные.
6. Тесты на кэш: cache-hit проверяется через `cacheManager`, eviction — через `assertNull` после мутации.
7. Все тесты из ЛР-6/7 проходят без изменений логики.
7. `REDIS_HOST` и `REDIS_PORT` берутся из переменных окружения, не захардкожены.

---

## Что почитать

1. [Spring Cache Abstraction](https://docs.spring.io/spring-framework/reference/integration/cache.html)
2. [Spring Data Redis](https://docs.spring.io/spring-data/redis/reference/)
3. [Redis Documentation](https://redis.io/docs/)
4. [Caching with Spring Boot — Baeldung](https://www.baeldung.com/spring-cache-tutorial)
5. [Redis in Spring Boot — Baeldung](https://www.baeldung.com/spring-data-redis-tutorial)
6. [Testcontainers with Redis](https://java.testcontainers.org/modules/redis/)
