<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ include file="layout/header.jsp" %>
<div class="hero">
  <div class="max-w-7xl mx-auto px-4">
    <h1>Nuestros Servicios Dentales</h1>
    <p>Ofrecemos una amplia gama de servicios dentales con la más alta calidad y tecnología avanzada</p>
  </div>
</div>
<section class="max-w-7xl mx-auto px-4 py-10 w-full">
  <div class="flex gap-3 flex-wrap mb-8">
    <span class="pill pill-purple" style="padding:8px 18px">Todos los Servicios</span>
    <span class="pill pill-gray" style="padding:8px 18px">Preventivos</span>
    <span class="pill pill-gray" style="padding:8px 18px">Estéticos</span>
    <span class="pill pill-gray" style="padding:8px 18px">Restaurativos</span>
  </div>
  <div class="grid grid-3">
    <c:forEach var="s" items="${servicios}">
      <div class="card overflow-hidden">
        <div style="height:120px;background:linear-gradient(135deg,#667eea,#764ba2);display:grid;place-items:center"><i class="fas fa-tooth text-4xl text-white"></i></div>
        <div class="p-5">
          <h3 class="font-semibold mb-1">${s.nombre}</h3>
          <p class="text-xs text-gray-500 mb-3">${s.descripcion}</p>
          <div class="flex items-center justify-between">
            <div><span class="text-xs text-gray-400">Precio desde</span><div class="text-lg font-bold text-indigo-600">S/. ${s.precio}</div></div>
            <a href="${pageContext.request.contextPath}/citas" class="btn btn-info btn-sm"><i class="fas fa-calendar-plus mr-1"></i>Reservar</a>
          </div>
          <div class="mt-3 pt-3 border-t border-gray-100 text-xs text-gray-400">
            <i class="fas fa-clock mr-1"></i>Duración: ${s.duracion_minutos} min
            <i class="fas fa-shield-alt ml-3 mr-1"></i>Garantía incluida
          </div>
        </div>
      </div>
    </c:forEach>
  </div>
  <c:if test="${empty servicios}"><div class="card"><div class="empty"><i class="fas fa-stethoscope"></i><p>No hay servicios disponibles</p></div></div></c:if>
</section>
<section class="py-12" style="background:linear-gradient(135deg,#667eea,#764ba2)">
  <div class="max-w-3xl mx-auto px-4 text-center text-white">
    <h2 class="text-2xl font-semibold mb-2">¿Necesitas más información?</h2>
    <p class="text-indigo-100 text-sm mb-5">Nuestro equipo está listo para ayudarte a elegir el mejor tratamiento para ti</p>
    <div class="flex gap-3 justify-center flex-wrap">
      <a href="tel:+51999888777" class="btn btn-primary"><i class="fas fa-phone mr-1"></i>Llamar Ahora</a>
      <a href="${pageContext.request.contextPath}/faq" class="btn btn-ghost"><i class="fas fa-question-circle mr-1"></i>Ver FAQ</a>
    </div>
  </div>
</section>
<%@ include file="layout/footer.jsp" %>
