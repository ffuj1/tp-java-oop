# AGENTS.md

## Proyecto
Trabajo Práctico AED II - Operación Soberanía Federal - Escudo Nacional

## Objetivo del agente
Asistir en la implementación incremental de un simulador en Java, priorizando:
- exactitud funcional,
- cumplimiento estricto de la consigna,
- determinismo,
- contabilidad exacta,
- separación clara de responsabilidades,
- compilación limpia con javac.

El agente NO debe optimizar por velocidad sacrificando reglas de negocio ni restricciones de la consigna.

---

## Restricciones no negociables

1. Dinero:
   - Toda operación monetaria debe usar `java.math.BigDecimal`.
   - Está prohibido usar `double` o `float` para cálculos monetarios.

2. Aleatoriedad:
   - Debe existir una sola instancia de `java.util.Random` en toda la ejecución.
   - Esa instancia se inicializa exclusivamente con la `seed` leída de `soberania.properties`.
   - El mismo seed debe producir exactamente el mismo resultado.

3. AgenteFederal:
   - La mochila se implementa con `ActivoIncautado[]`.
   - La lógica LIFO se implementa manualmente con puntero de tope.
   - Está prohibido usar `java.util.Stack`, `Deque` o colecciones dinámicas en esta clase.

4. CentroComando:
   - Debe administrar los bunkers con `HashMap<Integer, BunkerObjetivo>`.
   - Toda auditoría monetaria debe centralizarse aquí.
   - Ninguna otra clase debe mutar acumuladores globales directamente.

5. Excepciones:
   - Debe existir `OperacionException extends Exception`.
   - Todas las excepciones de negocio deben extender esa clase.
   - Cada catch debe tener efectos reales sobre el estado del sistema.

6. IO y logging:
   - Durante la simulación no se puede usar `System.out` ni `System.err`.
   - La única salida por consola permitida es el reporte final.
   - El logging debe usar `java.util.logging.Logger` con `FileHandler` y `SimpleFormatter`.

7. Performance:
   - La simulación debe procesar la cantidad configurada de bunkers de forma secuencial por ID.
   - Debe medirse el tiempo total con `System.nanoTime()`.

---

## Arquitectura objetivo

### Paquetes sugeridos
- `app`
- `modelo`
- `excepciones`
- `config`
- `logging`
- `servicio`
- `util`

### Clases núcleo
- `ActivoIncautado`
- `AgenteFederal`
- `BunkerObjetivo`
- `CentroComando`

### Clases esperables de soporte
- `Configuracion`
- `ConfiguracionException`
- `LoggerFactory` o `OperacionLogger`
- `SimuladorOperacion`
- `GeneradorBunkers`
- `Main` o `SoberaniaFederal`

---

## Modelo del dominio

### ActivoIncautado
Responsabilidad:
- representar un bien o divisa confiscada.

Campos mínimos:
- `int id`
- `String tipo`
- `BigDecimal valorNominal`
- `int integridad`

Invariantes:
- `valorNominal >= 0`
- `integridad` en rango [0, 100]

### AgenteFederal
Responsabilidad:
- gestionar la recolección de activos con lógica LIFO.

Campos mínimos:
- `ActivoIncautado[] mochila`
- `int tope`

Métodos esperables:
- `push(ActivoIncautado)`
- `pop()`
- `peek()`
- `isEmpty()`
- `isFull()`
- `size()`

### BunkerObjetivo
Responsabilidad:
- representar un nodo de la red con activos, resistencia y región.

Campos mínimos:
- `int id`
- `ArrayList<ActivoIncautado> almacen`
- `double nivelResistencia`
- `Region region`

Restricciones:
- los IDs comienzan en 1 y son correlativos.

### CentroComando
Responsabilidad:
- mantener los bunkers y la auditoría global.

Campos mínimos:
- `HashMap<Integer, BunkerObjetivo> bunkers`
- `BigDecimal valorTotalInicial`
- `BigDecimal totalIncautado`
- `BigDecimal totalSiniestrado`
- `BigDecimal totalQuemadoAlerta`
- `BigDecimal totalRemanente`

Regla:
- toda suma/resta monetaria global debe pasar por métodos explícitos de esta clase.

---

## Convenciones de auditoría

Para resolver la ambigüedad de la consigna, adoptar esta convención:

- `totalIncautado`: activos capturados exitosamente.
- `totalSiniestrado`: activos destruidos por fuego cruzado.
- `totalQuemadoAlerta`: activos destruidos por alerta radial.
- `totalRemanente`: activos que quedan sin incautar al cierre, incluyendo:
  - bunkers no neutralizados,
  - activos no recolectados por mochila saturada,
  - cualquier remanente final que permanezca en la red.

Ecuación de cierre:
`valorTotalInicial = totalIncautado + totalSiniestrado + totalQuemadoAlerta + totalRemanente`

La diferencia contable debe ser exactamente cero.

---

## Flujo de simulación esperado

1. Cargar configuración.
2. Crear logger.
3. Crear única instancia de `Random`.
4. Generar red de bunkers proceduralmente.
5. Calcular `valorTotalInicial`.
6. Procesar bunkers secuencialmente por ID.
7. Registrar cada intervención con try/catch/finally.
8. Cerrar auditoría.
9. Emitir reporte final por consola.

---

## Reglas de inicialización procedural

Cada bunker debe generarse usando la misma `Random` compartida.

Por defecto:
- cantidad de activos por bunker: entre 1 y 10
- valor nominal por activo: entre 1000 y 10000000
- nivel de resistencia: entre 0.1 y 0.9
- región: asignación cíclica o aleatoria válida

Si faltan parámetros opcionales, usar fallbacks codificados.

---

## Excepciones de negocio obligatorias

- `OperacionException`
- `ResistenciaSuperiorException`
- `MochilaSaturadaException`
- `VeeduriaExternaException`
- `AlertaRadialException`
- `DisturbioCivilException`

### Comportamientos obligatorios

#### ResistenciaSuperiorException
- Si la intervención falla, intentar apoyo con bunker `ID + 1`.
- Si no existe, registrar repliegue en log.

#### MochilaSaturadaException
- Cortar recolección del bunker actual.
- Los activos restantes pasan a `totalRemanente`.

#### VeeduriaExternaException
- Forzar intervención sin bajas civiles.
- Registrar demora en WARNING.

#### AlertaRadialException
- Afectar vecinos `ID - 1` e `ID + 1`.
- Destruir parte de sus activos.
- Acumular el valor destruido en `totalQuemadoAlerta`.

#### DisturbioCivilException
- Se puede disparar como máximo una vez por ejecución.
- Aumenta 40% la resistencia de todos los bunkers restantes.
- Ninguna resistencia puede superar 1.0.

---

## IAS (Índice de Aceptación Social)

- Valor inicial: 50.0
- Baja civil: -2.0 por cada baja
- Operativo limpio: +0.1
- Nunca puede quedar por debajo de 0 ni por encima de 100
- Si baja de 15, puede disparar `DisturbioCivilException`

---

## Configuración obligatoria

Archivo: `soberania.properties` en la raíz del proyecto.

Claves mínimas:
- `seed`
- `probabilidadSiniestro`
- `probabilidadVeeduria`
- `capacidadMochila`
- `cantidadBunkers`

La carga debe usar:
- `FileInputStream`
- `Properties.load()`

Si falta archivo o clave obligatoria:
- abortar con error descriptivo.

---

## Logging

Archivo esperado:
- `registro_federal.log`

Configurar con:
- `FileHandler`
- `SimpleFormatter`

Niveles:
- `INFO` para eventos normales
- `WARNING` para siniestros y veedurías
- `SEVERE` para errores no recuperables

Debe existir al menos un registro por bunker procesado.

---

## Entrega de cambios por parte del agente

En cada respuesta, el agente debe entregar:

1. Objetivo del cambio
2. Archivos creados o modificados
3. Resumen técnico
4. Restricciones respetadas
5. Supuestos tomados
6. Riesgos o puntos ambiguos
7. Cómo compilar
8. Cómo probar

No debe hacer cambios silenciosos en archivos no pedidos.

---

## Política de cambios

- Implementar de forma incremental y compilable.
- No mezclar muchas responsabilidades en una sola entrega.
- No introducir frameworks.
- No usar librerías externas.
- No refactorizar masivamente salvo pedido explícito.
- No eliminar código existente sin justificarlo.
- No dejar placeholders en archivos finales.

---

## Checklist de Definition of Done

Un cambio se considera terminado solo si:
- compila con `javac`,
- respeta las restricciones no negociables,
- no introduce uso incorrecto de `double` para dinero,
- no crea nuevas instancias de `Random`,
- no usa colecciones dinámicas en `AgenteFederal`,
- no deja catches vacíos,
- mantiene el proyecto más cerca del reporte final exigido,
- describe cómo probar el cambio.

---

## Objetivo final de entrega

El proyecto debe quedar listo para generar:
- código fuente compilable,
- `soberania.properties` real,
- `registro_federal.log`,
- reporte final correcto,
- insumos para `informe_estrategia.pdf`.

El estudiante debe poder explicar todo el código entregado.