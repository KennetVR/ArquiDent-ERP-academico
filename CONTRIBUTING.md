# Trabajo con Git

1. Crear una rama: `git switch -c feature/descripcion` o `fix/descripcion`.
2. Mantener las capas MVC–DAO y las validaciones en servicios.
3. Ejecutar `mvn verify`; para integración usar la base `arquident_cpe_test`.
4. Si cambia Tailwind, regenerar `src/main/webapp/css/tailwind.css`.
5. Actualizar la documentación afectada y abrir un pull request hacia `main`.

Commits: `tipo(ambito): descripcion`, por ejemplo:

```text
feat(citas): agregar filtro de agenda
fix(caja): corregir validacion de importe
docs(deploy): explicar instalacion en Tomcat
ci(build): verificar pruebas con MySQL
```

La configuración privada, backups, logs, `.runtime/` y `target/` se excluyen de Git.
La versión preparada se identifica con una etiqueta `vX.Y.Z` y una Release con WAR.
