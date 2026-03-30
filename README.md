# Minecraft Report System

Produktionsnahes, serverübergreifendes Report-System für Paper + Bungee mit MongoDB, Redis und Java/Bedrock UUID-Resolution.

## Was wurde neu umgesetzt?

- Vollständigeres Service-API mit Report-Lifecycle: erstellen, moderieren (resolve/reject), Details, Listen/Filter, Stats.
- Persistente Offline-Benachrichtigung (`pending_notifications`) für Reporter im Multi-Proxy-Setup.
- Redis-Statusupdates inkl. Zustellung an online Spieler oder Queue für spätere Login-Auslieferung.
- `/reports` Moderationskommandos mit Resolve/Reject/Details/Stats als Command-Alternativen.
- Datenmodell um Template + Indizes erweitert.

## Module

- `api`:
  - DTOs (`ReportCreateRequest`, `ReportFilter`, `ReportView`, `ReportStats`, `ModerationActionRequest`)
  - Modelle (`ReportStatus`, `ReportTemplate`, `PlayerEdition`)
  - Interfaces (`ReportService`, `UuidLookupService`, `PlatformAsyncExecutor`)
- `common`:
  - Morphia Entitäten (`reports`, `report_templates`, `pending_notifications`)
  - Service-Implementierung (`ReportServiceImpl`, `PendingNotificationService`)
  - Redis Publisher/Subscriber und Channel-Konstanten
  - `McApiClient` mit asynchronen HTTP Calls + Cache
- `bukkit`:
  - `ReportBukkitPlugin`
  - Commands `/report`, `/reports`
  - Login-Auslieferung wartender Reporter-Notifications
  - Status-Update Verarbeitung über Redis
- `bungee`:
  - `ReportBungeePlugin`
  - Admin Login Reminder bei offenen Reports

## Async-Strategie

Keine eigenen ExecutorServices in Business-Logik:
- Bukkit: `BukkitScheduler#runTaskAsynchronously`
- Bungee: `ProxyScheduler#runAsync`

Async wird über `PlatformAsyncExecutor` abstrahiert.

## Redis Channels

- `reports:new`
- `reports:status_update`

## Setup

### Voraussetzungen
- Java 21
- Maven 3.9+
- MongoDB 7+
- Redis 7+

### Infrastruktur lokal starten

```bash
docker compose up -d
```

### Build

```bash
mvn clean package
```

## Deployment

- Bukkit JAR auf alle Paper-Instanzen
- Bungee JAR auf alle Proxies
- Alle Instanzen gegen dieselben Mongo-/Redis-Backends

## Nächste sinnvolle Schritte

- SmartInventory GUI für `/report` & `/reports` Listenansicht
- Floodgate Forms für Bedrock-native UX
- Konfigurationsdateien statt Hardcoded Defaults
- Integrationstests mit Testcontainers (Mongo + Redis)
