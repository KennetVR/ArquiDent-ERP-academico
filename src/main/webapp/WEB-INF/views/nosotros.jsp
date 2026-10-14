<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ include file="layout/header.jsp" %>
<div class="hero">
  <div class="max-w-7xl mx-auto px-4">
    <h1>Conoce ArquiDent</h1>
    <p>Más de 10 años cuidando sonrisas con excelencia y tecnología de vanguardia</p>
  </div>
</div>
<section class="max-w-7xl mx-auto px-4 py-10 w-full">
  <div class="grid grid-2 mb-8">
    <div class="card"><h2><i class="fas fa-bullseye text-indigo-500"></i>Nuestra Misión</h2><p class="text-sm text-gray-600">En ArquiDent nos dedicamos a brindar servicios odontológicos de alta calidad, con tecnología de vanguardia y un enfoque personalizado para cada paciente. Nuestro compromiso es mejorar la salud bucal y la calidad de vida de nuestros pacientes.</p></div>
    <div class="card"><h2><i class="fas fa-eye text-indigo-500"></i>Nuestra Visión</h2><p class="text-sm text-gray-600">Ser la clínica dental líder en la región, reconocida por la calidad de nuestros servicios, la calidez humana de nuestro equipo y la innovación constante en tratamientos dentales.</p></div>
  </div>
  <h2 class="section-title"><i class="fas fa-heart text-purple-500"></i>Nuestros Valores</h2>
  <p class="section-sub">Los principios que guían cada uno de nuestros tratamientos y servicios</p>
  <div class="grid grid-3">
    <div class="card" style="text-align:center"><div class="w-12 h-12 rounded-full bg-indigo-50 grid place-items-center mx-auto mb-3"><i class="fas fa-award text-indigo-500"></i></div><b>Excelencia</b><p class="text-xs text-gray-500 mt-1">Nos esforzamos por ofrecer el más alto estándar en cada tratamiento y servicio.</p></div>
    <div class="card" style="text-align:center"><div class="w-12 h-12 rounded-full bg-indigo-50 grid place-items-center mx-auto mb-3"><i class="fas fa-hand-holding-heart text-indigo-500"></i></div><b>Compromiso</b><p class="text-xs text-gray-500 mt-1">Estamos comprometidos con la salud y bienestar de nuestros pacientes.</p></div>
    <div class="card" style="text-align:center"><div class="w-12 h-12 rounded-full bg-indigo-50 grid place-items-center mx-auto mb-3"><i class="fas fa-handshake text-indigo-500"></i></div><b>Confianza</b><p class="text-xs text-gray-500 mt-1">Construimos relaciones duraderas basadas en la honestidad y transparencia.</p></div>
    <div class="card" style="text-align:center"><div class="w-12 h-12 rounded-full bg-indigo-50 grid place-items-center mx-auto mb-3"><i class="fas fa-lightbulb text-indigo-500"></i></div><b>Innovación</b><p class="text-xs text-gray-500 mt-1">Incorporamos continuamente nuevas tecnologías y técnicas.</p></div>
    <div class="card" style="text-align:center"><div class="w-12 h-12 rounded-full bg-indigo-50 grid place-items-center mx-auto mb-3"><i class="fas fa-users text-indigo-500"></i></div><b>Trabajo en Equipo</b><p class="text-xs text-gray-500 mt-1">Nuestro equipo multidisciplinario trabaja de manera coordinada para lograr los mejores resultados.</p></div>
    <div class="card" style="text-align:center"><div class="w-12 h-12 rounded-full bg-indigo-50 grid place-items-center mx-auto mb-3"><i class="fas fa-hand-holding-medical text-indigo-500"></i></div><b>Calidad Humana</b><p class="text-xs text-gray-500 mt-1">Tratamos a cada paciente con respeto, empatía y atención personalizada.</p></div>
  </div>
</section>
<section class="py-12" style="background:linear-gradient(135deg,#667eea,#764ba2)">
  <div class="max-w-3xl mx-auto px-4 text-center text-white">
    <h2 class="text-2xl font-semibold mb-2">¿Listo para cuidar tu sonrisa?</h2>
    <p class="text-indigo-100 text-sm mb-5">Agenda tu cita hoy mismo y descubre por qué somos la clínica dental de confianza en Lima.</p>
    <div class="flex gap-3 justify-center flex-wrap">
      <a href="${pageContext.request.contextPath}/registro" class="btn btn-primary"><i class="fas fa-calendar-check"></i>Reservar Cita</a>
      <a href="${pageContext.request.contextPath}/servicios" class="btn btn-ghost"><i class="fas fa-phone"></i>Llamar Ahora</a>
    </div>
  </div>
</section>
<%@ include file="layout/footer.jsp" %>
