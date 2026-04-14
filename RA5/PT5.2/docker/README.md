# Docker PR3.2: MongoDB + Mongo Express + Loki + Grafana

## Serveis

- `mongo`: base de dades MongoDB
- `mongo-express`: interfície web per consultar MongoDB
- `loki`: base de dades de logs
- `grafana`: panells i cerca de logs (datasource Loki configurat)

## Aixecar i aturar

- `docker compose up -d`: crea i inicia tots els serveis.
- `docker compose down`: atura i elimina contenidors i xarxes.
- `docker compose ps`: mostra estat dels serveis.

## URLs útils

- Mongo Express: `http://localhost:8081`
- Loki API: `http://localhost:3100/ready`
- Grafana: `http://localhost:3000` (usuari `admin`, password `admin`)

## Logs del servidor WebSocket a Loki

1. Assegura aquestes variables al `.env` de `exemple-mongodb`:
	- `LOKI_ENABLED=true`
	- `LOKI_URL=http://localhost:3100`
2. Inicia el servidor amb `npm run pr32:server`.
3. Obre Grafana i ves a Explore, datasource Loki.
4. Consulta amb una expressió com:
	- `{app="pr32-websocket-server"}`

## Notes

- Es manté també el log local a fitxer (`data/logs/pr32_websocket_server.log`) amb winston.
- El servidor envia els logs alhora a consola, fitxer i Loki.