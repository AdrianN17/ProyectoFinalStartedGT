www.galaxy.edu.pe
RÚBRICA DE PROYECTO FINAL DE CURSO
Sección : CURS-000505
Curso : Starter Development
Instructor : Aristedes Novoa Arbildo
Horario :
 Lunes y Miércoles de 19:00 h a 21:00 h
Alumno :

I. CONSIDERACIONES GENERALES
a. El desarrollo del proyecto es personal; sin embargo, está permitido colaborar con sus
compañeros de clase o recibir asesoría de cualquier otro profesional con experiencia en la
creación de librerías, frameworks o starters personalizados orientados a la estandarización
y reutilización de componentes de software.
b. Está permitido utilizar libros, tutoriales, documentación oficial, scripts, repositorios de
referencia y material del curso para revisar conceptos y complementar el desarrollo de la
solución.
c. Se permite reutilizar proyectos, librerías, starters o componentes desarrollados durante las
prácticas del curso o provenientes de fuentes externas, siempre que no se vulneren los
derechos de autor y que dichos componentes sean adecuadamente integrados y
adaptados al proyecto presentado.
II. ACTIVIDADES A REALIZAR
a. Crear uno o más proyectos como pruebas de concepto (PoCs) y un proyecto integrado que
permita utilizar en conjunto los Galaxy Starters desarrollados durante el curso, demostrando
su integración y funcionamiento en una aplicación basada en Spring Boot.
b. Crear un repositorio en GitHub, GitLab o cualquier otra plataforma de gestión de versiones
para versionar el código fuente de la solución. El repositorio deberá incluir los starters
desarrollados, el proyecto de integración, la documentación técnica y los archivos de
configuración necesarios para su compilación, publicación y consumo. Este repositorio será
entregado al instructor del curso como sustento del proyecto y registrado en el sistema
académico de Galaxy Training.
c. La solución deberá incluir los principales componentes y funcionalidades desarrollados
durante el curso, tales como logging corporativo, auditoría, cifrado, observabilidad, autoconfiguración, pruebas, versionamiento y publicación de artefactos. La evaluación se realizará
de acuerdo con los criterios establecidos en la presente rúbrica.
www.galaxy.edu.pe
# Pattern Consideraciones
Cumple Comentarios de
revisión
Si No
01 Inventario de
Starters
Crear un inventario de los Galaxy Starters desarrollados, describiendo sus
funcionalidades, componentes, escenarios de uso y beneficios. Preparar una
tabla resumen.
02 Estructura Modular
Crear starters con una estructura modular clara (core, api, autoconfigure,
test, ejemplos u otras variantes según corresponda), facilitando la
reutilización, mantenimiento y evolución de la solución. Incluir
AutoConfiguration.imports cuando aplique.
03 Maven & Gradle
Configurar correctamente pom.xml o build.gradle aplicando BOM,
versionamiento semántico (SemVer), gestión de dependencias y publicación
local o remota de artefactos.
04 Starter de Logging
Implementar un starter corporativo para logging estructurado, incluyendo
configuración centralizada, correlación de trazas y personalización mediante
propiedades. Crear PoCs para demostrar su funcionamiento.
05 Starter de Auditoría
Implementar un starter corporativo de auditoría basado en AOP o
mecanismos equivalentes para registrar operaciones y eventos relevantes.
Crear PoCs para demostrar su funcionamiento.
06 Starter de Cifrado Implementar un starter corporativo para el cifrado y protección de datos
sensibles, incluyendo pruebas de integración y validación de funcionamiento.
www.galaxy.edu.pe
# Pattern Consideraciones
Cumple Comentarios de
revisión
Si No
07 Starter de
Observabilidad
Implementar un starter corporativo para observabilidad, métricas,
health checks y monitoreo de aplicaciones. Crear PoCs para
demostrar su funcionamiento.
08
Testing y Calidad Implementar pruebas unitarias e integración para los starters
desarrollados, incluyendo métricas de cobertura y validaciones de
calidad del código.
09
Publicación y
Versionamiento
Aplicar SemVer y publicar los starters y BOM corporativos en
repositorios locales o remotos (Nexus, JitPack o equivalentes).
Automatizar el proceso mediante scripts o herramientas de
construcción.
10
Presentación y
Documentación
Preparar README.md, diagramas, documentación técnica y material
de presentación que permita sustentar el diseño, implementación,
publicación y consumo de los starters desarrollados.
III. CONSIDERACIONES DE APROBACIÓN
Para aprobar el curso, el participante deberá obtener una calificación mínima de catorce (14) puntos sobre veinte (20),
sustentar su proyecto en la fecha programada y evidenciar la correcta implementación e integración de los Galaxy Starters
desarrollados, incluyendo logging, auditoría, cifrado, observabilidad, versionamiento, documentación y publicación de
artefactos.
www.galaxy.edu.pe
IV. PRESENTACION DEL PROYECTO
El participante deberá realizar la presentación y sustentación de su proyecto final en la fecha y horario establecidos por la
Coordinación Académica de Galaxy Training. La sustentación tendrá una duración máxima de diez (10) minutos, durante los
cuales deberá exponer los Galaxy Starters desarrollados, demostrar su funcionamiento e integración, así como evidenciar
los mecanismos de logging, auditoría, cifrado, observabilidad, pruebas y publicación implementados.
La presentación y sustentación constituyen requisitos obligatorios para la evaluación del proyecto final y permitirán validar
la autoría del trabajo presentado, así como el nivel de dominio técnico alcanzado sobre los conceptos y tecnologías
desarrollados durante el curso.