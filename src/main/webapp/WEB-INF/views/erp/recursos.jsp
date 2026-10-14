<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ include file="../layout/header.jsp" %><div class="hero"><div class="max-w-7xl mx-auto px-4"><h1>Módulo B · Recursos y Servicios</h1><p>Configuración de odontólogos, horarios laborales, servicios, precios y asignaciones que usan las reservas</p></div></div><section class="max-w-7xl mx-auto px-4 py-8 w-full"><c:if test="${not empty error}"><div class="alert alert-error"><i class="fas fa-exclamation-triangle mt-0.5"></i><span>${error}</span></div></c:if><c:if test="${not empty mensaje}"><div class="alert alert-ok"><i class="fas fa-check-circle mt-0.5"></i><span>${mensaje}</span></div></c:if>

  <div class="grid grid-4 mb-6">
    <div class="card" style="text-align:center"><i class="fas fa-user-md text-2xl text-indigo-500 mb-2"></i><b>${odontologos.size()}</b><p class="text-xs text-gray-500">Odontólogos</p></div>
    <div class="card" style="text-align:center"><i class="fas fa-stethoscope text-2xl text-indigo-500 mb-2"></i><b>${servicios.size()}</b><p class="text-xs text-gray-500">Servicios</p></div>
    <div class="card" style="text-align:center"><i class="fas fa-clock text-2xl text-indigo-500 mb-2"></i><b>${horarios.size()}</b><p class="text-xs text-gray-500">Horarios</p></div>
    <div class="card" style="text-align:center"><i class="fas fa-link text-2xl text-indigo-500 mb-2"></i><b>${asignaciones.size()}</b><p class="text-xs text-gray-500">Asignaciones</p></div>
  </div>

  <div class="grid grid-2 mb-6">
    <div class="card"><h2><i class="fas fa-user-md text-indigo-500"></i>Odontólogos</h2>
      <div class="tbl-wrap mb-4"><table class="tbl"><thead><tr><th>ID</th><th>Nombre</th><th>Correo</th><th>DNI</th><th>Activo</th></tr></thead><tbody><c:forEach var="o" items="${odontologos}"><tr><td><span class="mono">${o.id_usuario}</span></td><td class="font-semibold">${o.nombre}</td><td>${o.correo}</td><td>${o.dni}</td><td><span class="pill ${o.activo ? 'pill-green' : 'pill-gray'}">${o.activo ? 'Sí' : 'No'}</span></td></tr></c:forEach></tbody></table></div>
      <form method="post" action="${pageContext.request.contextPath}/recursos" class="max-w-md">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="odontologo">
        <div class="field"><label>ID (vacío = nuevo)</label><input name="id" type="number" min="0" placeholder="ID odontólogo"></div>
        <div class="field"><label>Nombre *</label><input name="nombre" required maxlength="100"></div>
        <div class="field"><label>Correo *</label><input type="email" name="correo" required maxlength="100"></div>
        <div class="field"><label>Contraseña (nuevo o cambio)</label><input type="password" name="contrasena" maxlength="128"></div>
        <div class="field"><label>DNI *</label><input name="dni" required pattern="[0-9]{8}" maxlength="8"></div>
        <div class="field"><label>Teléfono *</label><input name="telefono" required maxlength="15"></div>
        <div class="field"><label>Dirección</label><input name="direccion" maxlength="500"></div>
        <div class="field"><label>Fecha de nacimiento</label><input type="date" name="nacimiento"></div>
        <label class="checks mt-4"><input type="checkbox" name="activo" checked> Activo</label>
        <button class="btn btn-primary w-full"><i class="fas fa-save"></i>Guardar odontólogo</button>
      </form>
    </div>

    <div class="card"><h2><i class="fas fa-stethoscope text-indigo-500"></i>Servicios y precios</h2>
      <div class="tbl-wrap mb-4"><table class="tbl"><thead><tr><th>ID</th><th>Servicio</th><th>Precio</th><th>Min</th><th>Activo</th></tr></thead><tbody><c:forEach var="s" items="${servicios}"><tr><td><span class="mono">${s.id_servicio}</span></td><td class="font-semibold">${s.nombre}</td><td>S/. ${s.precio}</td><td>${s.duracion_minutos}</td><td><span class="pill ${s.activo ? 'pill-green' : 'pill-gray'}">${s.activo ? 'Sí' : 'No'}</span></td></tr></c:forEach></tbody></table></div>
      <form method="post" action="${pageContext.request.contextPath}/recursos" class="max-w-md">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="servicio">
        <div class="field"><label>ID (vacío = nuevo)</label><input name="id" type="number" min="0" placeholder="ID servicio"></div>
        <div class="field"><label>Nombre *</label><input name="nombre" required maxlength="100"></div>
        <div class="field"><label>Descripción</label><input name="descripcion" maxlength="1000"></div>
        <div class="field"><label>Precio (S/.) *</label><input name="precio" required pattern="[0-9]{1,8}(\.[0-9]{1,2})?" placeholder="0.00"></div>
        <div class="field"><label>Duración (min, múltiplo de 15) *</label><input name="duracion" required type="number" min="15" max="240" step="15"></div>
        <div class="flex gap-3"><label class="checks"><input type="checkbox" name="activo" checked> Activo</label><label class="checks"><input type="checkbox" name="previa"> Requiere atención previa</label></div>
        <button class="btn btn-primary w-full mt-2"><i class="fas fa-save"></i>Guardar servicio</button>
      </form>
    </div>

    <div class="card"><h2><i class="fas fa-clock text-indigo-500"></i>Horarios laborales</h2>
      <div class="tbl-wrap mb-4"><table class="tbl"><thead><tr><th>Odontólogo</th><th>Día</th><th>Horario</th><th>Acción</th></tr></thead><tbody><c:forEach var="h" items="${horarios}"><tr><td class="font-semibold">${h.nombre}</td><td>${h.dia_semana}</td><td>${h.hora_inicio} - ${h.hora_fin}</td><td><form method="post" action="${pageContext.request.contextPath}/recursos"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="quitarHorario"><input type="hidden" name="id" value="${h.id_horario}"><button class="btn btn-danger btn-sm" onclick="return confirm('¿Retirar horario? Se bloquea si hay citas pendientes.')"><i class="fas fa-trash"></i>Quitar</button></form></td></tr></c:forEach></tbody></table></div>
      <form method="post" action="${pageContext.request.contextPath}/recursos" class="max-w-md">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="horario">
        <div class="field"><label>Odontólogo *</label><select name="odontologo" required><c:forEach var="o" items="${odontologos}"><option value="${o.id_usuario}">${o.nombre}</option></c:forEach></select></div>
        <div class="field"><label>Día *</label><select name="dia" required><c:forEach var="d" items="${dias}"><option value="${d}">${d}</option></c:forEach></select></div>
        <div class="field"><label>Inicio *</label><input type="time" name="inicio" required step="900"></div>
        <div class="field"><label>Fin *</label><input type="time" name="fin" required step="900"></div>
        <button class="btn btn-primary w-full"><i class="fas fa-plus"></i>Agregar horario</button>
      </form>
    </div>

    <div class="card"><h2><i class="fas fa-link text-indigo-500"></i>Asignación odontólogo - servicio</h2>
      <div class="tbl-wrap mb-4"><table class="tbl"><thead><tr><th>Odontólogo</th><th>Servicio</th><th>Acción</th></tr></thead><tbody><c:forEach var="a" items="${asignaciones}"><tr><td class="font-semibold">${a.odontologo}</td><td>${a.servicio}</td><td><form method="post" action="${pageContext.request.contextPath}/recursos"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="quitarAsignacion"><input type="hidden" name="odontologo" value="${a.id_odontologo}"><input type="hidden" name="servicio" value="${a.id_servicio}"><button class="btn btn-danger btn-sm"><i class="fas fa-trash"></i>Quitar</button></form></td></tr></c:forEach></tbody></table></div>
      <form method="post" action="${pageContext.request.contextPath}/recursos" class="max-w-md">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="asignar">
        <div class="field"><label>Odontólogo *</label><select name="odontologo" required><c:forEach var="o" items="${odontologos}"><option value="${o.id_usuario}">${o.nombre}</option></c:forEach></select></div>
        <div class="field"><label>Servicio *</label><select name="servicio" required><c:forEach var="s" items="${servicios}"><option value="${s.id_servicio}">${s.nombre}</option></c:forEach></select></div>
        <button class="btn btn-primary w-full"><i class="fas fa-link"></i>Asignar</button>
      </form>
    </div>
  </div>
</section><%@ include file="../layout/footer.jsp" %>