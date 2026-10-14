<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ include file="layout/header.jsp" %>
<div class="hero">
  <div class="max-w-7xl mx-auto px-4">
    <h1>Tu Sonrisa es Nuestra <span style="color:#fbbf24">Pasión</span></h1>
    <p>En ArquiDent ofrecemos servicios dentales de calidad con tecnología de vanguardia y un equipo profesional comprometido con tu bienestar y salud bucal.</p>
  </div>
</div>
<section class="max-w-7xl mx-auto px-4 py-10 w-full">
  <h2 class="section-title"><i class="fas fa-star text-indigo-500"></i>¿Por qué elegir ArquiDent?</h2>
  <p class="section-sub">Combinamos experiencia, tecnología y calidez humana para brindarte la mejor atención dental</p>
  <div class="grid grid-3">
    <div class="card" style="text-align:center"><div class="w-14 h-14 rounded-full bg-indigo-50 grid place-items-center mx-auto mb-3"><i class="fas fa-user-md text-indigo-500 text-xl"></i></div><b>Profesionales Expertos</b><p class="text-xs text-gray-500 mt-1">Nuestro equipo está formado por odontólogos especializados con años de experiencia y formación continua en las mejores técnicas.</p></div>
    <div class="card" style="text-align:center"><div class="w-14 h-14 rounded-full bg-indigo-50 grid place-items-center mx-auto mb-3"><i class="fas fa-cogs text-indigo-500 text-xl"></i></div><b>Tecnología Avanzada</b><p class="text-xs text-gray-500 mt-1">Utilizamos equipos de última generación para brindarte tratamientos precisos, cómodos y con los mejores resultados.</p></div>
    <div class="card" style="text-align:center"><div class="w-14 h-14 rounded-full bg-indigo-50 grid place-items-center mx-auto mb-3"><i class="fas fa-heart text-indigo-500 text-xl"></i></div><b>Atención Personalizada</b><p class="text-xs text-gray-500 mt-1">Cada paciente recibe un trato único, con seguimiento continuo y un plan de tratamiento adaptado a sus necesidades.</p></div>
  </div>
  <h2 class="section-title mt-10"><i class="fas fa-tooth text-indigo-500"></i>Nuestros Servicios Destacados</h2>
  <p class="section-sub">Ofrecemos una amplia gama de servicios dentales para cuidar tu salud bucal</p>
  <div class="grid grid-3">
    <c:forEach var="s" items="${serviciosDestacados}">
      <div class="card">
        <h3 class="font-semibold mb-1">${s.nombre}</h3>
        <p class="text-xs text-gray-500 mb-3">${s.descripcion}</p>
        <div class="flex items-center justify-between">
          <div><span class="text-xs text-gray-400">Precio desde</span><div class="text-lg font-bold text-indigo-600">S/. ${s.precio}</div></div>
          <a href="${pageContext.request.contextPath}/citas" class="btn btn-info btn-sm"><i class="fas fa-calendar-plus mr-1"></i>Reservar</a>
        </div>
      </div>
    </c:forEach>
  </div>
  <div class="text-center mt-10">
    <a href="${pageContext.request.contextPath}/servicios" class="btn btn-primary"><i class="fas fa-list mr-1"></i>Ver Todos los Servicios</a>
  </div>
</section>
<section class="py-12" style="background:linear-gradient(135deg,#667eea,#764ba2)">
  <div class="max-w-3xl mx-auto px-4 text-center text-white">
    <h2 class="text-2xl font-semibold mb-2">¿Listo para Mejorar tu Sonrisa?</h2>
    <p class="text-indigo-100 text-sm mb-5">Agenda tu cita hoy mismo y descubre por qué somos la clínica dental de confianza en Lima.</p>
    <div class="flex gap-3 justify-center flex-wrap">
      <a href="${pageContext.request.contextPath}/registro" class="btn btn-primary"><i class="fas fa-calendar-check mr-1"></i>Reservar Cita Ahora</a>
      <a href="${pageContext.request.contextPath}/servicios" class="btn btn-ghost"><i class="fas fa-phone mr-1"></i>Llamar Ahora</a>
    </div>
  </div>
</section>
<%@ include file="layout/footer.jsp" %>
