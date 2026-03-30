# Minecraft Report System

Serverübergreifendes Report-System für Paper/Bungee-Netzwerke mit MongoDB + Redis und Unterstützung für Java- & Bedrock-Spieler.

## Architektur

### Module
- `api`: Öffentliche Interfaces, DTOs, Enums
- `common`: Morphia-Modelle, Redis Publisher, mc-api.io Client, Service-Implementierung
- `bukkit`: Command-Handling, Spielerinteraktion, Scheduler-Adapter für Paper
- `bungee`: Admin Login-Reminder und Scheduler-Adapter für Proxy

### Asynchronität
Alle I/O-lastigen Vorgänge laufen über eine `PlatformAsyncExecutor`-Abstraktion.
- Bukkit: `BukkitScheduler#runTaskAsynchronously`
- Bungee: `ProxyScheduler#runAsync`

Damit werden keine eigenen Thread-Pools/ExecutorServices verwendet.

### Messaging
- `reports:new`: Neue Reports zur Live-Benachrichtigung auf allen Instanzen
- `reports:status_update`: Statusänderungen für Reporter-Benachrichtigungen über Proxy-Grenzen

## Setup

### Voraussetzungen
- Java 21
- Maven 3.9+
- MongoDB 7+
- Redis 7+

### Lokale Infrastruktur starten
```bash
docker compose up -d
```

### Build
```bash
mvn clean package
```

## Deployment
- `bukkit/target/*.jar` auf alle Paper Server
- `bungee/target/*.jar` auf alle Bungee/Waterfall Proxies
- Beide verwenden dieselbe MongoDB- und Redis-Instanz

## Konfiguration (Beispiel)
Aktuell sind Default-Werte in `ReportSystemConfig#defaults()` hinterlegt:
- Mongo: `mongodb://localhost:27017`
- DB: `report_system`
- Redis: `localhost:6379`
- Bedrock Prefix: `.`
- UUID Cache TTL: `20 Minuten`

Für Produktion sollte ein Config-Loader pro Plattform ergänzt werden.

## API-Dokumentation (Kurzüberblick)
- `ReportService#createReport(...)`: Persistiert Report + publiziert `reports:new`
- `ReportService#changeStatus(...)`: Aktualisiert Report + publiziert `reports:status_update`
- `UuidLookupService#lookup(...)`: Asynchrone UUID-Auflösung via mc-api.io

## Performance-Überlegungen
- UUID-Lookups werden mit Guava-Cache zwischengespeichert
- MongoDB-Modelle sind für gezielte Indizierung vorbereitet (`status`, `reported`, `reporter`, `createdAt`)
- Redis Pub/Sub minimiert Polling und liefert Echtzeit-Events

## Offene Erweiterungen
- SmartInventory-UI für `/report` und `/reports`
- Floodgate Forms für Bedrock UX
- Robustere JSON-Verarbeitung (Jackson/Gson)
- Persistente Offline-Notification Queue für Reporter
- Duplicate-Detection (Spam-Schutz)
