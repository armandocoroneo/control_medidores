# Control Medidores

App Android nativa (Kotlin + Jetpack Compose + Room) para controlar medidores
de luz con precio de kW variable.

## Funcionamiento

- Cada medidor tiene un **nombre** (medidor o persona).
- Al registrar una lectura nueva, la app **muestra automáticamente la última
  lectura guardada** para ese medidor.
- Pides la **lectura nueva** y el **precio por kW** (varía cada vez).
- La app calcula el consumo (`nueva - anterior`) y el costo
  (`consumo × precio`), y guarda todo en el historial junto con la fecha.
- **Recordatorio dentro de la app**: cada medidor calcula automáticamente su
  próxima fecha de lectura (un mes después de la última). Al abrir la app,
  si algún medidor ya venció, aparece un aviso arriba con la lista.
- **Salida anticipada (facturación por días)**: si el inquilino se va antes
  de la lectura mensual, puedes generar un cobro prorrateado. La app calcula
  el promedio diario de consumo a partir del historial (última lectura
  completa ÷ días de ese periodo) y lo multiplica por los días que ocupó,
  con el precio por kW vigente. Si aún no hay historial, te pide el consumo
  estimado del mes manualmente. Este cobro queda en el historial marcado
  como "PRORRATEO" y **no modifica** la lectura real del medidor.

## Estructura del proyecto

```
app/src/main/java/com/controlmedidores/app/
  data/         Entidades Room (Medidor, Lectura, Prorrateo), DAOs, base de datos, repositorio
  viewmodel/    MedidorViewModel (estado y lógica de negocio)
  ui/screens/   Pantallas Compose (lista con recordatorio, detalle/lectura/prorrateo)
  ui/theme/     Tema Material 3
  MainActivity.kt
codemagic.yaml  Configuración de build para Codemagic
```

## Todo desde el navegador — sin instalar nada

No necesitas terminal, git ni Android Studio. Todo se hace con la web de
GitHub y la web de Codemagic.

### 1. Subir el proyecto a GitHub (desde la web)

1. Descomprime el archivo `medidores-app.zip` en tu computadora (solo para
   tener la carpeta lista; no necesitas abrir nada con programas).
2. Entra a [github.com](https://github.com) e inicia sesión (o crea una
   cuenta gratis).
3. Click en **New repository** (botón verde arriba a la derecha). Ponle un
   nombre, por ejemplo `control-medidores`, y créalo **vacío** (sin README).
4. Dentro del repo recién creado, click en **"uploading an existing file"**
   (o **Add file → Upload files**).
5. Abre la carpeta `medidores-app` descomprimida en tu explorador de
   archivos, selecciona **todo su contenido** (todas las carpetas y
   archivos de adentro, no la carpeta en sí) y arrástralo a la página de
   GitHub. Chrome y Edge conservan la estructura de carpetas al arrastrar.
6. Baja y click en **Commit changes**. Listo, el proyecto ya está en GitHub.

> Nota: la carpeta `.git` que trae el zip es solo un historial local, no
> hace falta subirla — si por accidente aparece en la selección no pasa
> nada, GitHub la ignora al subir por la web.

### 2. Compilar el APK con Codemagic (desde la web)

1. Entra a [codemagic.io](https://codemagic.io) e inicia sesión con tu
   cuenta de GitHub (botón "Sign in").
2. Click en **Add application**, elige GitHub y selecciona el repositorio
   `control-medidores` que acabas de crear.
3. Codemagic detecta el archivo `codemagic.yaml` que ya está en el proyecto
   (workflow `android-debug`) y lo configura automáticamente — no necesitas
   tocar nada.
4. (Opcional) En `codemagic.yaml`, dentro del repositorio de GitHub, puedes
   editar directamente desde la web el correo de notificación
   (reemplaza `tu-correo@ejemplo.com` por el tuyo) usando el lápiz de editar
   que aparece al abrir el archivo en GitHub.
5. Click en **Start new build**. Cuando termine (unos minutos), el APK
   queda disponible para descargar en la pestaña **Artifacts** del build.
6. Descarga el `.apk` a tu celular Android e instálalo (puede pedirte
   habilitar "Instalar apps de orígenes desconocidos" la primera vez).

Cada vez que quieras actualizar la app, subes los archivos modificados a
GitHub por la web (mismo botón "Upload files" o editando directo en el
sitio) y le das "Start new build" de nuevo en Codemagic.

### Firmar para Play Store (opcional, más adelante)

El workflow incluido genera un **APK debug** (instalable directamente para
pruebas, no requiere firma). Cuando quieras publicar en Play Store, se
agrega una configuración de firma (`android_signing`) en Codemagic con tu
keystore y se cambia el script a `./gradlew bundleRelease`. Puedo ayudarte
con eso cuando llegues a ese punto.

## Notas de diseño

- Base de datos local con Room (no requiere internet para funcionar).
- La app impide registrar una lectura nueva menor a la anterior, para evitar
  errores de digitación.
- El prorrateo por salida anticipada es una **estimación**, no una lectura
  real del medidor — queda claramente marcado en el historial para
  diferenciarlo de las lecturas normales.
