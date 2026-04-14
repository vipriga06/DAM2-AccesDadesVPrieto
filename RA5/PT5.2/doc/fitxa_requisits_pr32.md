# Fitxa de Requisits - PR3.2 WebSocket Joc 2D

Autor: Victor P.
Data: 2026-04-14

## 1. Objectiu Principal

Desenvolupar un mini-servidor WebSocket en Node.js que rebi moviments 2D d'un jugador en format JSON des d'un client Node.js, guardi cada moviment a MongoDB, i tanqui una partida quan no hi hagi moviment durant 10 segons, calculant i retornant la distancia en linia recta entre punt inicial i final.

## 2. Requisits Funcionals

- RF-01: El servidor WebSocket ha d'escoltar connexions entrants en un port configurable.
- RF-02: El client Node.js s'ha de poder connectar al servidor.
- RF-03: El jugador s'ha de moure amb les tecles de fletxa al client.
- RF-04: Cada moviment ha d'enviar un JSON valid al servidor amb `type`, `playerId`, `x`, `y` i `direction`.
- RF-05: El servidor ha de validar els missatges; si son invalids, ha de respondre amb error.
- RF-06: Mentre hi hagi moviment, tots els moviments han de pertanyer a la mateixa partida (`sessionId`).
- RF-07: Cada moviment valid ha de generar 1 document a MongoDB amb camp per associar-lo a la partida.
- RF-08: Si passen 10 segons sense moviment, la partida s'ha de considerar finalitzada.
- RF-09: En finalitzar la partida, el servidor ha de calcular la distancia euclidiana entre punt inicial i final.
- RF-10: El servidor ha d'informar al client de la finalitzacio i de la distancia calculada.
- RF-11: El servidor ha de registrar cada moviment amb un index de moviment (`moveIndex`) per facilitar auditories.
- RF-12: El servidor ha de marcar el motiu de finalitzacio de partida (`inactivity_timeout`, `client_disconnected`).
- RF-13: El client ha d'oferir un mode demo per poder provar la logica sense interaccio manual.

## 3. Requisits No Funcionals

- RNF-01 (Logging): El servidor ha d'usar winston i escriure logs en pantalla i fitxer.
- RNF-02 (Configurabilitat): Port, timeout i URI de MongoDB han d'estar en variables d'entorn.
- RNF-03 (Mantenibilitat): Codi modular i amb noms clars de funcions.
- RNF-04 (Fiabilitat): Gestio basica d'errors de JSON, WebSocket i MongoDB.
- RNF-05 (Observabilitat): El servidor ha d'enviar logs estructurats a Loki per ser consultats des de Grafana.
- RNF-06 (Seguretat basica): No s'han de deixar credencials hardcoded al codi font.
- RNF-07 (Traçabilitat): Cada log clau ha d'incloure almenys `sessionId` i `playerId` quan sigui aplicable.
- RNF-08 (Testabilitat): Les funcions de transformacio de dades han de tenir tests unitaris automatitzats.

## 4. Millores afegides a partir de suggeriments d'IA (Claude)

Aspectes introduits per fer la fitxa mes professional i mes avaluable:

- Definicio explicita de motius de finalitzacio de sessio.
- Observabilitat centralitzada amb Loki + Grafana.
- Reforc de traçabilitat per facilitar debugging i revisio de partida.
- Configuracio per entorns amb variables externes.
- Criteris de testabilitat i facilitat de manteniment.
- Registre ordenat de moviments amb index intern.

## 5. Format JSON Client -> Servidor

## 4. Format JSON Client -> Servidor

```json
{
  "type": "move",
  "playerId": "player-1",
  "x": 4,
  "y": 2,
  "direction": "right"
}
```

## 6. Format JSON Servidor -> Client

Resposta de moviment guardat:

```json
{
  "type": "move_saved",
  "sessionId": "uuid",
  "x": 4,
  "y": 2,
  "direction": "right"
}
```

Resposta de partida finalitzada:

```json
{
  "type": "session_ended",
  "reason": "inactivity_timeout",
  "sessionId": "uuid",
  "playerId": "player-1",
  "start": { "x": 0, "y": 0 },
  "end": { "x": 4, "y": 2 },
  "movesRegistered": 5,
  "straightDistance": 4.4721,
  "inactivityMs": 10000
}
```

## 7. Criteris de validacio tecnica

- Connexio client-servidor operativa per WebSocket.
- Insercio d'1 document per moviment a MongoDB.
- Calcul correcte de distancia euclidiana: $d = \sqrt{(x_2 - x_1)^2 + (y_2 - y_1)^2}$.
- Finalitzacio automàtica de sessio als 10 segons d'inactivitat.
- Logs visibles en consola, fitxer i Grafana (via Loki).
