# Padrón Electoral

Aplicación de escritorio en Java para consultar registros del padrón electoral, navegar por páginas de resultados, filtrar por nombre o cédula, seleccionar personas y exportar la selección a PDF.

## Características

- Consulta individual por cédula
- Exploración paginada del padrón
- Búsqueda por nombre o cédula
- Selección de registros para exportación
- Exportación a PDF
- Soporte de respuesta en JSON y XML para detalle de exportación
- Servidor HTTP y TCP integrados
- GUI de escritorio en Swing

---

# Requisitos previos

Antes de usar el programa, asegúrate de tener instalado lo siguiente:

- **Java JDK** compatible con el proyecto
- **NetBeans** (recomendado, porque el proyecto fue trabajado ahí)
- Las librerías `.jar` necesarias ya agregadas al proyecto:
  - `itextpdf-5.2.0.jar`
  - `javax.mail.jar`
  - `activation.jar`

> Nota: en esta versión **solo se usa iText para PDF**.  
> Las librerías de mail quedaron agregadas, pero el envío por correo **todavía no está implementado**.

---

# Importante antes de ejecutar

## Debes colocar el archivo del padrón manualmente

El archivo `PADRON.txt` **no viene incluido en el repositorio**, así que debes agregarlo antes de correr el sistema.

Esto es obligatorio porque el programa lo necesita para cargar los datos del padrón.

## Carpeta correcta

Debes colocar los archivos dentro de la carpeta:

```text
PadronElectoral/data/
Archivos esperados

Dentro de data/ deben existir estos archivos:

PadronElectoral/data/PADRON.txt
PadronElectoral/data/distelec.txt
Nombres exactos

Los nombres deben quedar exactamente así:

PADRON.txt
distelec.txt

Si cambias el nombre o los colocas en otra carpeta, el programa no va a encontrarlos.

Estructura esperada

La estructura mínima del proyecto debe verse así:

PadronElectoral/
├── data/
│   ├── PADRON.txt
│   └── distelec.txt
├── lib/
│   ├── itextpdf-5.2.0.jar
│   ├── javax.mail.jar
│   └── activation.jar
├── nbproject/
├── src/
├── build.xml
└── manifest.mf
Cómo configurar el proyecto
1. Clonar o descargar el repositorio

Abre el proyecto en tu computadora.

2. Verificar la carpeta data

Si no existe, créala dentro de PadronElectoral/.

3. Copiar los archivos de datos

Coloca:

PADRON.txt
distelec.txt

dentro de:

PadronElectoral/data/
4. Verificar las librerías

Dentro de PadronElectoral/lib/ deben estar los .jar.

Si no están vinculados en NetBeans:

clic derecho al proyecto
Properties
Libraries
Add JAR/Folder
seleccionar los .jar dentro de lib/
Cómo ejecutar el programa
Opción recomendada: desde NetBeans
Paso 1

Abre el proyecto PadronElectoral en NetBeans.

Paso 2

Haz Clean and Build para asegurarte de que todo compile correctamente.

Paso 3

Ejecuta primero la clase principal del sistema:

padron.app.Main

Esto levanta:

el servidor TCP
el servidor HTTP

Si todo está bien, en consola deberías ver algo parecido a esto:

Distelec cargado: ...
TCP escuchando en puerto 5000
HTTP escuchando en http://localhost:8080/padron
PadronElectoral iniciado
Paso 4

Ahora ejecuta la GUI:

padron.presentacion.gui.GuiMain
Cómo usar el programa
Al abrir la GUI

La ventana está dividida en dos partes:

Panel izquierdo

Aquí puedes:

ver el padrón paginado
buscar por nombre o cédula
navegar entre páginas
seleccionar registros
Panel derecho

Aquí puedes:

ver la lista seleccionada
quitar registros
limpiar la lista
escoger formato de detalle para PDF
exportar a PDF
Navegación del padrón

Cuando la barra de búsqueda está vacía:

se carga el padrón completo paginado
la primera página muestra los primeros 100 registros
puedes avanzar con Siguiente
puedes retroceder con Anterior

Ejemplo:

Página 1: registros 1–100
Página 2: registros 101–200
etc.
Búsqueda

En el campo “Nombre o cédula” puedes escribir:

un nombre
un apellido
una cédula
parte de una cédula

Luego puedes:

presionar Enter
o hacer clic en Buscar

El sistema mostrará los resultados paginados.

Para limpiar la búsqueda

Haz clic en Limpiar.

Eso vuelve a mostrar el padrón general paginado.

Seleccionar personas

Para agregar una persona a la lista de exportación:

selecciona una fila en la tabla del lado izquierdo
haz clic en Agregar seleccionado

También puedes usar doble clic sobre una fila.

La persona aparecerá en la tabla del lado derecho.

Reglas importantes
no se agregan duplicados
si una persona ya está en la lista, el sistema lo avisa
Exportar a PDF

Cuando ya tengas personas en la lista de la derecha:

elige el formato de detalle:
JSON
XML
haz clic en Exportar PDF
selecciona dónde guardar el archivo
el sistema generará el PDF
Qué incluye el PDF
tabla resumen con las personas seleccionadas
fecha de exportación
criterio de búsqueda
total de personas exportadas
detalle crudo en JSON o XML, según la preferencia elegida
Solución de problemas
Error: no encuentra el padrón

Revisa que exista:

PadronElectoral/data/PADRON.txt

y que el nombre esté exactamente así.

Error: no encuentra distelec

Revisa que exista:

PadronElectoral/data/distelec.txt
La GUI dice que no responde el backend

Asegúrate de haber ejecutado primero:

padron.app.Main

y después:

padron.presentacion.gui.GuiMain
La búsqueda tarda demasiado

El padrón puede ser muy grande, así que algunas búsquedas pueden tardar más que el cambio de páginas normal.

Recomendaciones:

usar criterios más específicos
buscar con Enter o con el botón Buscar
esperar unos segundos si el filtro es muy amplio
El PDF no se genera

Verifica que:

itextpdf-5.2.0.jar esté agregado al proyecto
tengas permisos para escribir en la carpeta destino
haya al menos una persona seleccionada
Notas importantes
PADRON.txt no se sube al repositorio por su tamaño y por manejo del proyecto
el usuario debe agregar ese archivo manualmente antes de ejecutar
en esta versión el envío por correo no está implementado
el enfoque actual está en consulta, exploración y exportación a PDF
Flujo recomendado de uso
colocar PADRON.txt y distelec.txt en PadronElectoral/data/
abrir el proyecto en NetBeans
hacer Clean and Build
ejecutar padron.app.Main
ejecutar padron.presentacion.gui.GuiMain
explorar el padrón o buscar por nombre/cédula
agregar personas a la lista
exportar la selección a PDF
Autoría

Proyecto académico desarrollado en Java con arquitectura por capas, servidor HTTP/TCP y GUI de escritorio para consulta del padrón electoral.
