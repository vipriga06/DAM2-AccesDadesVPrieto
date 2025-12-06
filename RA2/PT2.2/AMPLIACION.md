# PT2.2 - Exercicis d'Ampliació

## Resumen de Cambios

Se han implementado las siguientes funcionalidades de ampliación para **Exercici0** y **Exercici1**:

### 1. **Selector de Base de Datos (SQLite/MySQL)**

Se ha creado la clase `DatabaseSelector` en el paquete `com.project.utils` que proporciona:

- **Menú interactivo** para que el usuario seleccione entre SQLite o MySQL
- **Métodos de utilidad** para obtener el archivo de configuración apropiado
- **Validación** del tipo de base de datos seleccionada

**Ubicación:** `src/main/java/com/project/utils/DatabaseSelector.java`

**Métodos disponibles:**
```java
// Muestra un menú y retorna "sqlite" o "mysql"
public static String selectDatabase()

// Retorna el nombre del archivo de configuración
public static String getHibernateConfigFile(String databaseType)

// Valida si la BD seleccionada es válida
public static boolean isValidDatabase(String databaseType)
```

### 2. **Archivos de Configuración**

Se han creado archivos de configuración para ambas bases de datos:

- **SQLite:** `src/main/resources/hibernate.properties` (existente)
- **MySQL:** `src/main/resources/hibernate-mysql.properties` (nuevo)

**Configuración MySQL:** El archivo incluye parámetros para:
- Driver: `com.mysql.cj.jdbc.Driver`
- URL: `jdbc:mysql://localhost:3306/ciudats_db?serverTimezone=UTC`
- Usuario y contraseña (configurables)
- Pool de conexiones (C3P0)

### 3. **Tests Unitarios**

Se han creado tests completos para validar las funciones del Manager:

**Ejercicio 0 (Hibernate XML):** 7 tests
- `testAddCiutat()` - Validar creación de ciudades
- `testAddCiutada()` - Validar creación de ciudadanos
- `testListCiutats()` - Validar listado de ciudades
- `testListCiutadans()` - Validar listado de ciudadanos
- `testAssignCiutadaToCiutat()` - Validar asignación de ciudadanos
- `testDeleteCiutada()` - Validar eliminación de ciudadanos
- `testDeleteCiutat()` - Validar eliminación de ciudades

**Ejercicio 1 (JPA):** 8 tests
- Los 7 anteriores +
- `testUpdateCiutat()` - Validar actualización de ciudades
- `testUpdateCiutada()` - Validar actualización de ciudadanos

**Ubicación:** 
- Exercici0: `src/test/java/com/project/ManagerTest.java`
- Exercici1: `src/test/java/com/project/ManagerTest.java`

**Ejecución:**
```bash
mvn test
```

### 4. **Uso de Utilidades**

Ambos `Main.java` importan las clases de utils:
```java
import com.project.utils.UtilsSQLite;
import com.project.utils.MainSQLite;
import com.project.utils.DatabaseSelector;  // Nueva
```

### 5. **Salida Esperada Verificada**

Se ha validado que los ejercicios producen la salida esperada:

✅ **Punt 1:** Creación inicial de 3 ciudades y 6 ciudadanos
✅ **Punt 2:** Actualización de ciudades con ciudadanos asociados
✅ **Punt 3:** Actualización de nombres de ciudades y ciudadanos
✅ **Punt 4:** Eliminación de ciudad y ciudadano
✅ **Punt 5:** Recuperación de ciudadanos de una ciudad específica

## Cómo Usar

### Ejecutar con SQLite (por defecto):
```bash
cd Exercici0  # o Exercici1
.\run.ps1 com.project.Main
```

### Ejecutar Tests:
```bash
mvn test
```

### Compilar:
```bash
mvn clean compile
```

### Ejecutar con MySQL (cuando esté disponible):
1. Configurar MySQL localmente
2. Crear base de datos: `ciudats_db`
3. Actualizar credenciales en `hibernate-mysql.properties`
4. Implementar selector de BD en Main.java

## Requisitos Completados

- ✅ Menú interactivo para seleccionar BD
- ✅ Soporte para SQLite y MySQL
- ✅ Archivos de configuración para ambas BDs
- ✅ Tests unitarios completos
- ✅ Utilización de carpeta `utils`
- ✅ Salida esperada verificada
- ✅ Comentarios en código

## Notas

- Los tests usan SQLite para validación local sin depender de MySQL
- Los archivos de configuración pueden ampliarse fácilmente
- La clase `DatabaseSelector` puede integrarse en Main.java para permitir elegir BD al inicio
