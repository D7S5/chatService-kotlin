# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Dependencies (Kafka, Redis, Zookeeper, Kafdrop)
sudo docker compose up -d

# Run (requires application-demo.yml with DB config)
./gradlew bootRun --args='--spring.profiles.active=demo'

# Build
./gradlew clean bootJar -x test

# Run tests
./gradlew test
```

## Architecture

Spring Boot 3.5 + Kotlin/Java hybrid chat service with WebSocket (STOMP), Kafka message queuing, Redis session/cache, JPA/MySQL, and OAuth2 (Google/Naver/Kakao).

### Messaging Pipeline (Transactional Outbox Pattern)
- **Group chat**: Controller -> `GroupMessageProducer` (saves to `group_outbox` table) -> `GroupMessageOutboxProcessor` (@Scheduled, claims batch with row locking) -> Kafka `group-message-topic` -> `GroupMessageBroadcastConsumer` (WebSocket push) + `GroupMessageStoreConsumer` (DB persist)
- **DM**: Controller -> `DMProducerService` (saves to `dm_outbox`) -> `DMOutboxProcessor` -> Kafka `dm-messages` -> `DMBroadcastConsumer` + `DmStoreConsumer`

### WebSocket Auth Flow
1. Client calls `POST /api/ws/token` (uses refresh token cookie) -> gets one-time ws-token
2. Client connects to `/ws?ws-token=xxx` -> `WsHandshakeInterceptor` consumes token via `WsTokenService` (Redis)
3. `CustomHandshakeHandler` sets userId as Principal
4. `StompAuthChannelInterceptor` checks room participant/ban status on SUBSCRIBE

### Key Packages
- `config/` - Security, WebSocket, Redis, Kafka (separate producer/consumer configs for Group and DM)
- `security/` - JWT (access+refresh with Redis rotation), `UserPrincipal` (implements both UserDetails and OAuth2User), `CookieUtil`
- `service/` - `RoomParticipantService` (join/leave/kick/ban with Spring Events), `ChatRoomService`, `DMService`
- `redis/` - `OnlineStatusService` (heartbeat+TTL based), `WsTokenService`, `UserSessionRegistry`
- `kafka/` - Outbox processors, producers, broadcast/store consumers
- `oauth/` - Google/Naver/Kakao via `OAuthAttributes.of()` pattern

### Patterns
- Entities use mutable `var` properties with JPA (not data classes) - follow this pattern for entities
- DTOs use `data class` for simple value objects, `record` for Java DTOs with validation
- Constructor injection everywhere (Kotlin primary constructors, Java used `@RequiredArgsConstructor`)
- `@Transactional` event listeners (`TransactionPhase.AFTER_COMMIT`) for WebSocket notifications after DB commits
- Redis key conventions: `RT:current:{userId}`, `RT:blacklist:{hash}`, `online:ttl:{userId}`, `ws_token:{token}`, `room:invite:{code}`
