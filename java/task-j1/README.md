# Task J1: Сервер генерации ключей

Сервер по TCP принимает имя клиента, генерирует для него пару ключей RSA 8192 бит и сертификат X509, подписанный ключом сервера, и отдаёт их клиенту. Повторный запрос с тем же именем получает ту же пару, даже если генерация ещё не закончилась.

## Запуск

1. Создать ключ подписи RSA:
   ```bash
   openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:4096 -out ca.key
   ```

2. Запустить сервер:
   ```bash
   ./gradlew :java:task-j1:runServer --args="<порт> <число-нитей> <ключ.pem> <issuer>"
   ./gradlew :java:task-j1:runServer --args="5555 4 $PWD/ca.key 'CN=NSU Key Server'"
   ```
   Issuer указывается как имя X500, например `CN=NSU Key Server`.

3. Запустить клиент:
   ```bash
   ./gradlew :java:task-j1:runClient --args="<имя> <хост> <порт> [--delay <сек>] [--abort]"
   ./gradlew :java:task-j1:runClient --args="alice localhost 5555"
   ```
   - `--delay <сек>`: пауза между отправкой запроса и чтением ответа (медленный клиент).
   - `--abort`: завершиться вместо чтения ответа (аварийное завершение клиента).

   Файлы `alice.key` и `alice.crt` появятся в `java/task-j1/`.

## Проверка результата

```bash
cd java/task-j1
openssl x509 -in alice.crt -noout -subject -issuer   # subject=CN=alice, issuer=CN=NSU Key Server
openssl rsa -in alice.key -noout -text | head -1     # Private-Key: (8192 bit, 2 primes)
```
