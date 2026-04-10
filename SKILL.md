---
name: soberania-federal-java
description: Asiste en la implementación incremental del TP Operación Soberanía Federal en Java. Úsala cuando la tarea implique arquitectura, clases del dominio, simulación, excepciones, auditoría contable con BigDecimal, pila estática manual, configuración con Properties, logging con Logger, pruebas o performance.
---

# Soberanía Federal Java

## Objetivo
Implementar de forma incremental y compilable el TP de AED II "Operación Soberanía Federal - Escudo Nacional" en Java, cumpliendo estrictamente las restricciones académicas y técnicas de la consigna.

## Cuándo usar esta skill
Usar esta skill cuando la tarea incluya alguna de estas necesidades:
- diseñar la arquitectura del proyecto;
- crear o modificar clases Java del TP;
- implementar `ActivoIncautado`, `AgenteFederal`, `BunkerObjetivo`, `CentroComando`;
- modelar excepciones propias de negocio;
- implementar auditoría contable con `BigDecimal`;
- implementar una pila estática con array fijo;
- leer `soberania.properties` con `Properties`;
- configurar `Logger` con `FileHandler` y `SimpleFormatter`;
- generar la red de bunkers de forma determinista con una única instancia de `Random`;
- mejorar rendimiento, pruebas o reporte final.

## Cuándo no usar esta skill
No usar esta skill para:
- tareas ajenas al TP;
- reescrituras genéricas no relacionadas con la consigna;
- agregar frameworks, librerías externas o patrones innecesarios;
- cambiar el lenguaje de implementación.

## Restricciones no negociables
1. Todo valor monetario debe manejarse con `java.math.BigDecimal`.
2. No usar `double` ni `float` para dinero.
3. Debe existir una sola instancia de `java.util.Random` en toda la ejecución.
4. Esa instancia debe inicializarse con la `seed` leída desde `soberania.properties`.
5. `AgenteFederal` debe usar `ActivoIncautado[]` y lógica LIFO manual con tope.
6. No usar `Stack`, `Deque` ni colecciones dinámicas en `AgenteFederal`.
7. `CentroComando` debe usar `HashMap<Integer, BunkerObjetivo>`.
8. Toda auditoría monetaria global debe centralizarse en `CentroComando`.
9. Todas las excepciones de negocio deben extender `OperacionException`.
10. Cada `catch` debe producir un efecto real sobre el estado del sistema.
11. Durante la simulación no usar `System.out` ni `System.err`.
12. La única salida a consola permitida es el reporte final.
13. El proyecto debe compilar con `javac` sin errores ni warnings evitables.
14. La simulación debe medir tiempo con `System.nanoTime()`.

## Arquitectura objetivo
Paquetes sugeridos:
- `app`
- `modelo`
- `excepciones`
- `config`
- `logging`
- `servicio`
- `util`

Clases núcleo:
- `ActivoIncautado`
- `AgenteFederal`
- `BunkerObjetivo`
- `CentroComando`

Clases de soporte sugeridas:
- `Configuracion`
- `ConfiguracionException`
- `SimuladorOperacion`
- `GeneradorBunkers`
- `LoggerFactory`
- `Main` o `SoberaniaFederal`

## Convención de auditoría
Adoptar esta convención:
- `totalIncautado`: activos capturados exitosamente.
- `totalSiniestrado`: activos destruidos por fuego cruzado.
- `totalQuemadoAlerta`: activos destruidos por alerta radial.
- `totalRemanente`: activos no incautados al cierre, incluyendo bunkers no neutralizados y excedentes por mochila saturada.

Ecuación de cierre:
`valorTotalInicial = totalIncautado + totalSiniestrado + totalQuemadoAlerta + totalRemanente`

La diferencia contable final debe ser exactamente cero.

## Forma de trabajo obligatoria
Trabajar siempre de forma incremental, con cambios pequeños y compilables.
Antes de escribir código complejo:
1. definir arquitectura;
2. listar invariantes;
3. explicitar supuestos;
4. resolver ambigüedades de la consigna.

No hacer refactors masivos salvo pedido explícito.

## Formato de respuesta esperado
En cada respuesta, devolver:
1. Objetivo del cambio
2. Archivos creados o modificados
3. Resumen técnico
4. Restricciones respetadas
5. Supuestos tomados
6. Riesgos o ambigüedades
7. Cómo compilar
8. Cómo probar

## Prioridades
Priorizar en este orden:
1. cumplimiento de consigna;
2. correctitud;
3. determinismo;
4. auditoría exacta;
5. claridad del diseño;
6. rendimiento.

## Checklist de Definition of Done
Un cambio se considera terminado solo si:
- compila;
- respeta `BigDecimal` para dinero;
- no crea nuevas instancias de `Random`;
- no usa colecciones dinámicas en `AgenteFederal`;
- no deja `catch` vacíos;
- mantiene la auditoría consistente;
- indica cómo probarse.

## Ejemplos de pedidos donde debe activarse
- "Creá el esqueleto del proyecto Java para este TP."
- "Implementá AgenteFederal con mochila estática."
- "Modelá la jerarquía de excepciones."
- "Implementá la carga de soberania.properties."
- "Revisá si la auditoría monetaria puede romperse."
- "Optimizá el procesamiento de 50000 bunkers."