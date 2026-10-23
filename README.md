# Quint Launcher

Launcher para Android TV (pensado para una Quint de 32" con Android 11; todavía sin probar en el equipo) con estética inspirada en el Apple TV de tvOS: fondo claro, tiles con esquinas redondeadas y animación de foco con el control remoto.

## Qué tiene

- Arriba a la derecha: hora, **red** (Wi‑Fi / Ethernet / sin conexión, se actualiza sola), **modo nocturno** (luna) y **ajustes**. Red y ajustes abren la configuración del sistema.
- **Modo día/noche**: el botón de luna cambia entre *automático* (sigue el atardecer de Federal), *nocturno* y *diurno*. Queda guardado aunque apagues la tele.
- **Clima de Federal, Entre Ríos**: temperatura, máxima y mínima, con ícono según el cielo (datos de Open-Meteo).
- Al bajar, toda la pantalla se desplaza (barra, favoritas y apps), así al volver arriba queda el fondo limpio.
- **Favoritas**: una barra tipo dock con las apps que elijas.
- **Todas las apps**: bajando con el control remoto aparece el resto, en orden alfabético.
- Sin banners de películas: solo apps.
- Se registra como launcher de inicio (`HOME` + `LEANBACK_LAUNCHER`).

## Cómo elegir las favoritas

1. Bajá hasta "Todas las apps" y tocá **Elegir favoritas**.
2. Tocá cada app: tilde azul = va arriba, más = no va.
3. Tocá **Listo**.

En teoría también alterna favorita si mantenés apretado OK sobre una app (depende del control).
La primera vez se cargan solas las de streaming conocidas que tengas instaladas.

## Cómo obtener el APK (sin computadora)

1. En este repo, pestaña **Actions** → workflow **Build APK** (corre solo en cada push a `main`).
2. El APK queda en **Releases** como `QuintLauncher.apk`, y también como artifact de la corrida.

## Instalarlo en la tele

- Bajá el APK desde Releases en la tele con una app como *Downloader* y abrilo (hay que permitir "orígenes desconocidos" para esa app), o pasalo por pendrive / `adb install QuintLauncher.apk`.
- Al apretar el botón **Home** del control, Android TV te va a preguntar qué launcher usar: elegí **Quint Launcher**.
- Si tu tele no pregunta, puede venir con el launcher del fabricante bloqueado; en ese caso se puede usar un adb para cambiar el predeterminado.

## Compilar local

Android Studio (Hedgehog o más nuevo) → abrir la carpeta → Run. Kotlin 1.9.10, Compose BOM 2023.10.01, minSdk 28, targetSdk 30.

## Notas

- Android no trae la tipografía SF de Apple; se usa la del sistema.
- Los tiles usan el banner que cada app publica para Android TV; si una app no tiene, muestra su ícono sobre fondo oscuro.
- El texto de las secciones está en `app/src/main/res/values/strings.xml` (ahí se puede cambiar "Favoritas" por "Recientes", por ejemplo).

## Créditos

- Ideas de clima, modo día/noche por atardecer y estética tomadas de [Free My TV](https://github.com/simbasjungles/free-my-tv) (MIT). Ver `licenses/MIT-free-my-tv.txt`.
- Íconos del clima: Material Symbols (Apache 2.0). Ver `licenses/Apache-2.0-material-symbols.txt`.
- Fuente: Inter (SIL OFL). Ver `licenses/OFL-Inter.txt`.
- Clima: [Open-Meteo](https://open-meteo.com/) (uso gratuito sin clave).
