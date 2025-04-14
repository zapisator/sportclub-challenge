## Explicación de la Implementación

(Esta sección provee una explicación breve de las decisiones clave de arquitectura e implementación tomadas durante la realización de la tarea, complementando las instrucciones de uso del README principal.)

**Enfoque General:**
Se comenzó la tarea inmediatamente después de recibirla, lo que proporcionó un margen de tiempo suficiente. Esto permitió no solo cumplir con los requisitos mínimos, sino también profundizar en el estudio e implementación de enfoques y tecnologías más avanzadas, pero prometedoras, buscando crear una solución moderna y de calidad.

**Arquitectura:**
Como base arquitectónica, se eligió la Arquitectura Hexagonal (Puertos y Adaptadores). A pesar de requerir estudio adicional en algunos aspectos, este enfoque se seleccionó deliberadamente para asegurar una alta testeabilidad, flexibilidad y mantenibilidad a largo plazo de la solución, separando claramente la lógica de negocio del núcleo de los detalles de infraestructura (trabajo con BD, framework web, procesos Batch, etc.). Se definieron las capas principales: `domain`, `application` (con casos de uso y puertos), `adapter` (in/out) e `infrastructure` (configuración, Batch, entidades JPA, etc.).

**Migración de Datos (Spring Batch):**
Para la tarea de migración de datos entre las dos bases de datos, se utilizó Spring Batch. Una implementación inicial podría haber sido más simple, pero el análisis de posibles problemas de rendimiento, robustez, gestión de errores y capacidad de reinicio al trabajar con los volúmenes de datos indicados (100+100 registros y potencialmente más) llevó a la elección de este framework. Proporciona procesamiento por lotes (chunks), capacidad de reinicio, logging detallado y manejo de errores a nivel de registro. La lógica de comparación y decisión (crear/actualizar/omitir) está encapsulada en los `ItemProcessors`, que interactúan con los repositorios de la base de datos destino. La limpieza de la caché tras una migración exitosa se implementa mediante un `JobExecutionListener`.

**API y DTOs:**
Los endpoints de la API (`/usuarios`, `/acceso`, `/auth/login`, `/migrate`) se diseñaron utilizando DTOs (Data Transfer Objects) para las solicitudes y respuestas, asegurando una clara separación entre la representación interna de datos (modelos de dominio, entidades JPA) y el contrato externo de la API. La paginación para `/usuarios` se implementó usando las herramientas estándar de Spring Data (`Pageable`) y un `PageResultDto` personalizado para coincidir con el formato de respuesta de la tarea. La validación de datos de entrada se realiza mediante anotaciones de Jakarta Bean Validation (`@Valid`). El manejo de errores está centralizado usando `@RestControllerAdvice` (`GlobalExceptionHandler`).
Al implementar la validación del DNI, se consideró la profundidad de la verificación. A diferencia de CUIL/CUIT, no existe un algoritmo público y simple para verificar la suma de control o la validez del número de DNI argentino. Una verificación completa requeriría la integración con servicios gubernamentales externos, lo cual excede el alcance y la complejidad de este challenge técnico. Por lo tanto, se tomó la decisión pragmática de limitarse a la validación del formato (7 a 9 caracteres numéricos) utilizando expresiones regulares y anotaciones de validación estándar, asegurando que la entrada cumpla con la estructura esperada sin intentar verificar su validez real en sistemas externos.

**Seguridad (Spring Security & JWT):**
La seguridad de la API se gestiona con Spring Security. Se implementó autenticación basada en JWT (JSON Web Token). El endpoint `/auth/login` acepta un DNI y, en caso de éxito, devuelve un JWT. Los endpoints protegidos (ej., `/usuarios`) requieren la presencia de un JWT válido en la cabecera `Authorization: Bearer <token>`. Se utilizaron componentes personalizados como `JwtAuthenticationFilter`, `JwtAuthenticationEntryPoint` y `UserDetailsServiceImpl`, que trabaja con el DNI como identificador de usuario.

**Configuración y Bases de Datos:**
Se configuraron dos fuentes de datos H2 independientes (source y target) con sus propias configuraciones JPA (`SourceJpaConfig`, `TargetJpaConfig`), fábricas de EntityManager y gestores de transacciones. Esto asegura el aislamiento al trabajar con bases de datos distintas. La configuración JWT (secreto, expiración) se externalizó a `application.properties` y se gestiona mediante `@ConfigurationProperties`, permitiendo la sobreescritura mediante variables de entorno (importante para Docker y la seguridad del secreto).

**Inicialización de la BD de Origen:**
El requisito de poblar la base de datos de origen con datos de prueba (100 sedes, 100 usuarios) se implementó usando un `CommandLineRunner` de Spring Boot (`SourceDatabaseInitializer`), activado mediante un perfil específico (`init-source-db`). Esto permitió encapsular la lógica de inicialización y hacer su ejecución controlable (por ejemplo, mediante `docker-compose.init.yml`), sin afectar el código principal de la aplicación.

**Manejo de Requisitos Incompletos:**
La tarea no especificaba un modelo de roles (ej., administrador) ni un proceso de registro de usuarios. Por lo tanto, se implementó la autenticación vía JWT basada en el DNI (como se indica en la parte 3 de la tarea). El endpoint de migración (`/migrate`) se dejó sin autorización específica por roles, siguiendo estrictamente los requisitos proporcionados, aunque en una aplicación real requeriría protección adecuada (ej., rol ADMIN), lo cual se refleja en el código comentado y las anotaciones de Swagger.

**Verificación de Funcionalidad:**
Para verificar la correcta operación, se proporcionó documentación interactiva de la API (Swagger UI) para posibilitar pruebas manuales, y también se implementó un conjunto extenso de pruebas automatizadas: Pruebas Unitarias (para servicios, dominio), Pruebas de Integración (para controladores, adaptadores de persistencia, Job de Batch, inicializador de BD) y Pruebas de Arquitectura (usando ArchUnit para verificar dependencias entre capas).

**Proceso de Desarrollo y Commits:**
El proceso de desarrollo fue iterativo, con refactorización frecuente y revisión de decisiones a medida que se estudiaban tecnologías y se añadía funcionalidad. En el contexto de un challenge individual enfocado en el resultado final, se priorizó la velocidad y la calidad del código resultante sobre el mantenimiento detallado del historial de commits, práctica habitual en el trabajo en equipo en proyectos a largo plazo. Por ello, el código final se presenta en un commit principal que refleja la solución completada.

**Conclusión:**
En resumen, la implementación siguió principios de complejidad gradual, seleccionando herramientas robustas (Spring Batch, Spring Security JWT, Docker, MapStruct, ArchUnit) y buscando un código moderno, testeable y de calidad dentro del tiempo y los requisitos dados, aplicando la Arquitectura Hexagonal para una mejor organización y mantenibilidad.
