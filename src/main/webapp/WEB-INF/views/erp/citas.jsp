<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ include file="../layout/header.jsp" %><div class="hero"><div class="max-w-7xl mx-auto px-4"><h1>Módulo C · Citas y Atención</h1><p>Reserva dentro de la disponibilidad real, cancela, y registra la atención del odontólogo</p></div></div><section class="max-w-7xl mx-auto px-4 py-8 w-full"><c:if test="${not empty error}"><div class="alert alert-error"><i class="fas fa-exclamation-triangle mt-0.5"></i><span>${error}</span></div></c:if><c:if test="${not empty mensaje}"><div class="alert alert-ok"><i class="fas fa-check-circle mt-0.5"></i><span>${mensaje}</span></div></c:if>

  <c:if test="${sessionScope.rolUsuario != 'odontologo'}">
  <div class="grid grid-2 mb-6">
    <div class="card"><h2><i class="fas fa-search text-indigo-500"></i>1. Consultar disponibilidad real</h2>
      <form method="get" action="${pageContext.request.contextPath}/citas">
        <input type="hidden" name="accion" value="disponibilidad">
        <c:if test="${sessionScope.rolUsuario != 'paciente'}"><div class="field"><label>Paciente *</label><select name="paciente" required><option value="">Seleccione paciente...</option><c:forEach var="p" items="${pacientes}"><option value="${p.id_usuario}" ${p.id_usuario == pacienteElegido ? 'selected' : ''}>${p.nombre}</option></c:forEach></select></div></c:if>
        <div class="field"><label>Servicio y odontólogo asignados *</label><select name="opcion" required><option value="">Seleccione...</option><c:forEach var="o" items="${opciones}"><option value="${o.doctor}:${o.id_servicio}">S/. ${o.precio} · ${o.servicio} · ${o.odontologo} (${o.duracion_minutos} min)</option></c:forEach></select></div>
        <div class="field"><label>Fecha *</label><input type="date" name="fecha" required value="${fechaElegida}"></div>
        <button class="btn btn-info"><i class="fas fa-search"></i>Ver horarios disponibles</button>
      </form>
      <c:if test="${not empty horas}">
        <p class="text-xs text-gray-500 mt-3">Horarios libres (dentro del horario laboral, sin cruces):</p>
        <div class="flex flex-wrap gap-1 mt-2"><c:forEach var="h" items="${horas}"><span class="pill pill-green">${h}</span></c:forEach></div>
      </c:if>
    </div>
    <div class="card"><h2><i class="fas fa-calendar-plus text-indigo-500"></i>2. Reservar (genera pago pendiente)</h2>
      <form method="post" action="${pageContext.request.contextPath}/citas">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="reservar">
        <c:if test="${sessionScope.rolUsuario != 'paciente'}"><div class="field"><label>Paciente *</label><select name="paciente" required><option value="">Paciente...</option><c:forEach var="p" items="${pacientes}"><option value="${p.id_usuario}" ${p.id_usuario == pacienteElegido ? 'selected' : ''}>${p.nombre}</option></c:forEach></select></div></c:if>
        <div class="field-row"><div class="field"><label>ID odontólogo *</label><input name="odontologo" required type="number" min="1" value="${doctorElegido}"></div><div class="field"><label>ID servicio *</label><input name="servicio" required type="number" min="1" value="${servicioElegido}"></div></div>
        <div class="field-row"><div class="field"><label>Fecha *</label><input type="date" name="fecha" required value="${fechaElegida}"></div><div class="field"><label>Hora *</label><select name="hora" required><option value="">Hora...</option><c:forEach var="h" items="${horas}"><option value="${h}">${h}</option></c:forEach></select></div></div>
        <div class="field"><label>Motivo (opcional)</label><input name="motivo" maxlength="1000"></div>
        <button class="btn btn-success"><i class="fas fa-check"></i>Reservar cita</button>
      </form>
      <p class="hint">Use los ID de la opción elegida. El servidor valida asignación, horario laboral y cruces; la reserva crea el pago pendiente.</p>
    </div>
  </div>
  </c:if>

  <div class="card"><h2><i class="fas fa-calendar-alt text-indigo-500"></i>Agenda</h2>
    <div class="tbl-wrap"><table class="tbl"><thead><tr><th>Fecha</th><th>Paciente</th><th>Servicio / Odontólogo</th><th>Estado</th><th>Pago</th><th>Acciones</th></tr></thead><tbody><c:forEach var="c" items="${citas}"><tr><td>${c.fecha} ${c.hora}</td><td class="font-semibold">${c.paciente}</td><td>${c.servicio}<br><span class="text-xs text-gray-500">${c.odontologo}</span></td><td><span class="pill ${c.estado=='finalizada'?'pill-green':c.estado=='confirmada'?'pill-blue':c.estado=='en_atencion'?'pill-purple':c.estado=='cancelada'?'pill-red':'pill-amber'}\">${c.estado}</span></td><td><c:if test="${not empty c.id_pago}">S/. ${c.monto} <span class="pill ${c.estado_pago=='pagado'?'pill-green':'pill-amber'}\">${c.estado_pago}</span></c:if></td><td class="actions"><c:if test="${c.estado == 'sin_pagar' || c.estado == 'derivada_sin_pagar' || c.estado == 'reservada'}"><form method="post" action="${pageContext.request.contextPath}/citas"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="cancelar"><input type="hidden" name="id" value="${c.id_cita}"><button class="btn btn-danger btn-sm" onclick="return confirm('¿Cancelar la reserva?')"><i class="fas fa-times"></i>Cancelar</button></form></c:if><c:if test="${sessionScope.rolUsuario == 'odontologo' && c.estado == 'confirmada'}"><form method="post" action="${pageContext.request.contextPath}/citas"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="iniciar"><input type="hidden" name="id" value="${c.id_cita}"><button class="btn btn-info btn-sm"><i class="fas fa-play"></i>Iniciar atención</button></form></c:if><c:if test="${sessionScope.rolUsuario == 'odontologo' && c.estado == 'en_atencion'}"><form method="post" action="${pageContext.request.contextPath}/citas"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="finalizar"><input type="hidden" name="id" value="${c.id_cita}"><div class="field"><input name="observaciones" required maxlength="4000" placeholder="Observaciones"></div><div class="field"><input name="receta" maxlength="4000" placeholder="Receta"></div><div class="field"><input name="derivacion" maxlength="2000" placeholder="Derivación (opcional)"></div><button class="btn btn-success btn-sm"><i class="fas fa-flag-checkered"></i>Finalizar atención</button></form></c:if></td></tr></c:forEach></tbody></table></div><c:if test="${empty citas}"><div class="empty"><i class="fas fa-calendar-times"></i><p>Sin citas visibles.</p></div></c:if>
  </div>
</section><%@ include file="../layout/footer.jsp" %>