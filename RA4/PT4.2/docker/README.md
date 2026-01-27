Aquí tens una guia ràpida en format **Markdown pur** amb les comandes més utilitzades per gestionar el teu entorn. Com que el teu fitxer té un nom personalitzat, he inclòs el flag `-f` en els exemples.

```markdown
# Guia de Docker Compose (`docker-compose-dev.yml`)

### 1. Aixecar els serveis (DEV)
Crea i inicia els contenidors en segon pla (recomanat) per a desenvolupament.
```bash
docker-compose -f dev/docker-compose-dev.yml up -d

```

### 2. Aturar els serveis (DEV)

Atura els contenidors però no els elimina.

```bash
docker-compose -f dev/docker-compose-dev.yml stop

```

### 3. Aturar i eliminar (DEV)

Atura els contenidors i els elimina, però **manté els volums** (les dades de la DB es guarden).

```bash
docker-compose -f dev/docker-compose-dev.yml down

```

### 4. Reiniciar els serveis (DEV)

Útil si has fet algun canvi petit o vols refrescar l'estat.

```bash
docker-compose -f dev/docker-compose-dev.yml restart

```

### 5. Veure l'estat i logs (DEV)

Per comprovar si el contenidor està funcionant o per veure errors de MySQL:

```bash
# Llistat de contenidors i ports
docker-compose -f dev/docker-compose-dev.yml ps

# Veure els logs en temps real
docker-compose -f dev/docker-compose-dev.yml logs -f

```

### 6. Neteja total (DEV) (Atenció!)

Atura, elimina contenidors i **esborra els volums** (es perdran totes les dades de la base de dades).

```bash
docker-compose -f dev/docker-compose-dev.yml down -v

```

### 7. Executar comandes dins del contenidor (DEV)

Si vols entrar a la línia de comandes de MySQL directament:

```bash
docker exec -it mp06-tema3-api-mysql mysql -u chatuser -pchatpass

---

### 8. Entorn de TEST

Aquests comandaments utilitzen la base de dades de test mapejada al port local 3307.

```bash
# Aixecar test
docker-compose -f test/docker-compose-test.yml up -d

# Estat i logs
docker-compose -f test/docker-compose-test.yml ps
docker-compose -f test/docker-compose-test.yml logs -f

# Aturar i eliminar
docker-compose -f test/docker-compose-test.yml down

# Neteja total (elimina volums)
docker-compose -f test/docker-compose-test.yml down -v
```


```

```

Vols que t'ajudi a crear un fitxer `.sh` o `.bat` per executar aquestes comandes amb un sol clic?

```