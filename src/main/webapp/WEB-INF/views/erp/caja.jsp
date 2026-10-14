<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="../layout/header.jsp" %>
<div class="hero"><div class="max-w-7xl mx-auto px-4 caja-title-row"><div><p class="eyebrow">Administración</p><h1>Caja y comprobantes</h1></div><span class="environment-badge"><i class="fas fa-flask" aria-hidden="true"></i>Pruebas</span></div></div>
<section class="caja-page max-w-7xl mx-auto px-4 py-8 w-full">
  <c:if test="${not empty error}"><div class="alert alert-error" role="alert"><c:out value="${error}"/></div></c:if>
  <c:if test="${not empty mensaje}"><div class="alert alert-ok" role="status"><c:out value="${mensaje}"/></div><c:remove var="mensaje" scope="session"/></c:if>

  <div class="grid grid-4 stats-grid">
    <div class="card kpi-card"><span class="kpi-icon pill-green"><i class="fas fa-wallet" aria-hidden="true"></i></span><div><span class="kpi-label">Ingresos</span><b class="kpi-value">S/ ${ingresos}</b></div></div>
    <div class="card kpi-card"><span class="kpi-icon pill-amber"><i class="fas fa-clock" aria-hidden="true"></i></span><div><span class="kpi-label">Pagos pendientes</span><b class="kpi-value">${pendientes.size()}</b></div></div>
    <div class="card kpi-card"><span class="kpi-icon pill-purple"><i class="fas fa-receipt" aria-hidden="true"></i></span><div><span class="kpi-label">Comprobantes</span><b class="kpi-value">${emisiones.size()}</b></div></div>
    <div class="card kpi-card"><span class="kpi-icon pill-blue"><i class="fas fa-check-double" aria-hidden="true"></i></span><div><span class="kpi-label">Cobros registrados</span><b class="kpi-value">${cobros.size()}</b></div></div>
  </div>

  <div class="card caja-toolbar">
    <h2>Movimientos</h2>
    <form method="get" action="${pageContext.request.contextPath}/caja" class="caja-filters">
      <div class="field"><label for="caja-desde">Desde</label><input id="caja-desde" type="date" name="desde" value="${desde}" required></div>
      <div class="field"><label for="caja-hasta">Hasta</label><input id="caja-hasta" type="date" name="hasta" value="${hasta}" required></div>
      <button class="btn btn-info"><i class="fas fa-sliders" aria-hidden="true"></i>Aplicar</button>
    </form>
  </div>

  <div class="caja-columns">
    <div class="card"><div class="panel-title"><h2>Pagos pendientes</h2><span class="pill pill-amber">${pendientes.size()}</span></div>
      <c:if test="${empty pendientes}"><div class="empty"><i class="fas fa-check-circle"></i><p>Todo al día</p></div></c:if>
      <c:forEach var="p" items="${pendientes}">
        <article class="payment-card">
          <div class="payment-head"><div><h3><c:out value="${p.paciente}"/></h3><p class="payment-service"><c:out value="${p.servicio}"/></p></div><strong class="payment-amount">S/ ${p.monto}</strong></div>
          <div class="payment-meta"><span><i class="far fa-calendar mr-1" aria-hidden="true"></i>${p.fecha} · ${fn:substring(p.hora,0,5)}</span><span>Cita #${p.id_cita} · Pago #${p.id_pago}</span></div>
              <form method="post" action="${pageContext.request.contextPath}/caja" class="charge-form" data-cobrar>
                <input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="cobrar"><input type="hidden" name="cita" value="${p.id_cita}">
                <div class="field"><label for="metodo-${p.id_pago}">Método de pago</label><select class="payment-method" id="metodo-${p.id_pago}" name="metodo" required><c:forEach var="m" items="${metodos}"><option value="${m}">${m}</option></c:forEach></select></div>
                <button class="btn btn-solid" type="submit"><i class="fas fa-check" aria-hidden="true"></i>${emisionConfigurada ? 'Cobrar y emitir' : 'Registrar cobro'}</button>
              </form>
        </article>
      </c:forEach>
    </div>

    <div class="card caja-ledger"><div class="panel-title"><h2>Cobros registrados</h2><span class="pill pill-blue">${cobros.size()}</span></div>
      <c:choose><c:when test="${empty cobros}"><div class="empty"><i class="fas fa-receipt"></i><p>Sin cobros en este período</p></div></c:when><c:otherwise>
      <div class="tbl-wrap"><table class="tbl"><thead><tr><th>Paciente</th><th>Fecha</th><th>Monto</th><th>Método</th></tr></thead><tbody><c:forEach var="p" items="${cobros}"><tr><td><b><c:out value="${p.paciente}"/></b><span class="table-secondary">Pago #${p.id_pago} · Cita #${p.id_cita}</span></td><td>${fn:substring(fn:replace(p.fecha_pago,'T',' '),0,16)}</td><td class="font-semibold">S/ ${p.monto}</td><td><span class="pill pill-blue payment-method"><c:out value="${p.metodo_pago}"/></span></td></tr></c:forEach></tbody></table></div>
      </c:otherwise></c:choose>
    </div>
  </div>

  <div class="card mb-6" id="boletas-demo">
    <div class="panel-title"><h2>Comprobantes emitidos</h2><span class="pill pill-purple">${emisiones.size()}</span></div>
    <c:choose><c:when test="${empty emisiones}"><div class="empty"><i class="fas fa-receipt"></i><p>Sin comprobantes en este período</p></div></c:when><c:otherwise>
      <div class="tbl-wrap"><table class="tbl"><thead><tr><th>Comprobante</th><th>Paciente / Servicio</th><th>Importe</th><th>Estado SUNAT</th><th>Documentos</th></tr></thead><tbody>
        <c:forEach var="e" items="${emisiones}"><tr data-emision="${e.id_emision}">
          <td><b><c:out value="${e.serie}"/>-${e.numero}</b><span class="table-secondary">Pago #${e.id_pago} · Cita #${e.id_cita}</span><span class="table-secondary">${fn:substring(fn:replace(e.creado_en,'T',' '),0,16)}</span></td>
          <td><c:out value="${e.cliente_nombre}"/><br><span class="text-xs text-gray-500"><c:out value="${e.servicio}"/></span></td>
          <td><b>S/ ${e.monto}</b><span class="table-secondary">Base ${e.gravada} · IGV ${e.igv}</span></td>
          <td><span class="pill ${e.estado == 'ACEPTADO_DEMO' ? 'pill-green' : e.estado == 'RECHAZADO' ? 'pill-red' : 'pill-amber'}"><c:choose><c:when test="${e.estado == 'ACEPTADO_DEMO'}">Aceptado</c:when><c:when test="${e.estado == 'RECHAZADO'}">Rechazado</c:when><c:when test="${e.estado == 'ENVIANDO'}">Procesando</c:when><c:otherwise>Por revisar</c:otherwise></c:choose></span>
            <details class="result-detail"><summary>Detalle SUNAT</summary><p>HTTP <c:out value="${e.http_status}" default="—"/> · Código <c:out value="${e.codigo_sunat}" default="—"/></p><p><c:out value="${e.mensaje}"/></p></details></td>
          <td><div class="actions">
            <c:if test="${not empty e.ruta_pdf}"><a class="btn btn-purple btn-sm" href="${pageContext.request.contextPath}/caja?accion=pdf&amp;emision=${e.id_emision}" target="_blank" rel="noopener noreferrer" aria-label="PDF de la boleta ${e.serie}-${e.numero}"><i class="fas fa-file-pdf" aria-hidden="true"></i>PDF</a></c:if>
            <c:if test="${not empty e.ruta_xml}"><a class="btn btn-info btn-sm" href="<c:out value='${e.ruta_xml}'/>" target="_blank" rel="noopener noreferrer">XML</a></c:if>
            <c:if test="${not empty e.ruta_cdr}"><a class="btn btn-info btn-sm" href="<c:out value='${e.ruta_cdr}'/>" target="_blank" rel="noopener noreferrer">CDR</a></c:if>
          </div><c:if test="${empty e.ruta_pdf && empty e.ruta_xml && empty e.ruta_cdr}"><span class="text-xs text-gray-500">Sin archivos confirmados</span></c:if></td>
        </tr></c:forEach>
      </tbody></table></div>
    </c:otherwise></c:choose>
  </div>

  <details class="card external-documents" ${not empty consultas ? 'open' : ''}>
    <summary>Comprobantes externos</summary>
    <form method="post" action="${pageContext.request.contextPath}/caja" class="grid grid-4 gap-3 mb-6 items-end">
      <input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="comprobante">
      <div class="field" style="margin:0"><label for="pago-externo">Pago cobrado</label><select id="pago-externo" name="pago" required><option value="">Seleccione un cobro...</option><c:forEach var="p" items="${cobros}"><option value="${p.id_pago}" data-monto="${p.monto}">#${p.id_pago} · <c:out value="${p.paciente}"/> · S/. ${p.monto}</option></c:forEach></select></div>
      <div class="field" style="margin:0"><label>RUC emisor</label><input name="ruc" required pattern="[0-9]{11}" maxlength="11"></div>
      <div class="field" style="margin:0"><label>Tipo</label><select name="tipo" required><option value="01">Factura 01</option><option value="03">Boleta 03</option></select></div>
      <div class="field" style="margin:0"><label>Serie</label><input name="serie" required pattern="[A-Z0-9]{4}" maxlength="4"></div>
      <div class="field" style="margin:0"><label>Número</label><input name="numero" required type="number" min="1" max="99999999"></div>
      <div class="field" style="margin:0"><label>Fecha emisión</label><input type="date" name="fecha" required></div>
      <div class="field" style="margin:0"><label for="monto-externo">Monto del pago</label><input id="monto-externo" name="monto" required readonly pattern="[0-9]{1,8}(\.[0-9]{1,2})?"></div>
      <div><button class="btn btn-solid"><i class="fas fa-plus" aria-hidden="true"></i>Registrar comprobante</button></div>
    </form>
    <c:if test="${empty comprobantes}"><div class="empty"><i class="fas fa-file"></i><p>Sin comprobantes en el rango.</p></div></c:if>
    <div class="tbl-wrap mb-6"><table class="tbl"><thead><tr><th>Documento</th><th>Pago</th><th>Validez SUNAT</th><th>Acciones</th></tr></thead><tbody><c:forEach var="cp" items="${comprobantes}"><tr><td class="font-semibold">${cp.cod_comp}-${cp.serie}-${cp.numero}<br><span class="text-xs text-gray-500">RUC ${cp.num_ruc} · ${cp.fecha_emision} · S/. ${cp.monto}</span></td><td><span class="pill ${cp.estado_pago=='pagado'?'pill-green':'pill-amber'}\">${cp.estado_pago}</span></td><td><c:choose><c:when test="${not empty cp.resultado}"><span class="pill ${cp.resultado=='ACEPTADO'?'pill-green':cp.resultado=='ANULADO'?'pill-red':'pill-amber'}\">${cp.resultado}</span><br><span class="text-xs text-gray-500">${cp.mensaje}</span></c:when><c:otherwise><span class="pill pill-gray">Sin consulta</span></c:otherwise></c:choose></td><td class="actions"><a href="${pageContext.request.contextPath}/caja?comprobante=${cp.id_comprobante}" class="btn btn-info btn-sm"><i class="fas fa-history"></i>Historial</a><form method="post" action="${pageContext.request.contextPath}/caja"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><input type="hidden" name="accion" value="sunat"><input type="hidden" name="id" value="${cp.id_comprobante}"><button class="btn btn-purple btn-sm"><i class="fas fa-cloud-arrow-down"></i>Consultar en SUNAT</button></form></td></tr></c:forEach></tbody></table></div>
    <c:if test="${not empty consultas}">
      <h3 class="font-semibold text-sm mb-2"><i class="fas fa-history mr-1 text-indigo-500"></i>Historial de consultas</h3>
      <div class="tbl-wrap"><table class="tbl"><thead><tr><th>Fecha</th><th>Resultado</th><th>Detalle</th></tr></thead><tbody><c:forEach var="q" items="${consultas}"><tr><td>${q.consultado_en}</td><td><span class="pill pill-purple">${q.resultado}</span></td><td class="text-xs text-gray-600">HTTP ${q.http_status} · CP ${q.estado_cp} · RUC ${q.estado_ruc} · Dom. ${q.condicion_domicilio} · ${q.codigo_error} · ${q.mensaje} · ${q.observaciones}</td></tr></c:forEach></tbody></table></div>
    </c:if>
  </details>
</section>
<script>
  document.querySelectorAll('[data-cobrar]').forEach(function (form) {
    form.addEventListener('submit', function () {
      var button = form.querySelector('button[type="submit"]');
      button.disabled = true;
      button.textContent = 'Procesando...';
    });
  });
  document.getElementById('pago-externo').addEventListener('change', function () {
    document.getElementById('monto-externo').value = this.options[this.selectedIndex].dataset.monto || '';
  });
  window.addEventListener('pageshow', function (event) { if (event.persisted) window.location.reload(); });
</script>
<%@ include file="../layout/footer.jsp" %>
