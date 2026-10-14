# Dependencias y recursos visuales

## Ejecución

- JDK 21 recomendado; compilación compatible con Java 11.
- Tomcat 9 / Servlet API 4 (`javax.servlet`).
- MySQL 8.4 (mínimo 8.0.16).
- Maven; versiones de bibliotecas Java declaradas en `pom.xml`.
- Apache PDFBox 3.0.8 para el PDF con logo.

## Recursos incluidos en el WAR

| Recurso | Versión | Origen / licencia |
|---|---|---|
| Tailwind CSS | 4.3.3 | [tailwindlabs/tailwindcss](https://github.com/tailwindlabs/tailwindcss), MIT |
| Font Awesome Free | 7.1.0 | [FortAwesome/Font-Awesome](https://github.com/FortAwesome/Font-Awesome); licencia incluida en `assets/fontawesome/LICENSE.txt` |
| Poppins (latin, 300–700) | paquete Fontsource 5.3.0 | [Fontsource Poppins](https://fontsource.org/fonts/poppins); SIL OFL, incluida en `assets/poppins/LICENSE` |
| Logo ArquiDent | imagen proporcionada para el proyecto | PNG completo e isotipo en `images/` |

Se mantienen las licencias de los componentes de terceros. No se asigna una
licencia global al código o al logo sin decisión del titular del proyecto.

## Regenerar estilos (solo desarrollo)

El CSS generado se versiona. **No hace falta Node para compilar el WAR ni ejecutar
la aplicación.** Si se añaden utilidades Tailwind nuevas a las JSP:

```powershell
npm ci --prefix tools
npm run build:css --prefix tools
```

Para volver a descargar las versiones fijadas de fuentes e iconos:

```powershell
powershell -ExecutionPolicy Bypass -File tools/descargar-recursos.ps1
```

Después recompilar el WAR. Los recursos se sirven desde `/css`, `/assets` e
`/images`; el filtro publica solo los tipos necesarios.
