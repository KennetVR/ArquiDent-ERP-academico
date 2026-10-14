<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ include file="layout/header.jsp" %>
<div class="hero">
  <div class="max-w-7xl mx-auto px-4">
    <h1>Preguntas Frecuentes</h1>
    <p>Encuentra respuestas a las preguntas más comunes sobre nuestros servicios</p>
  </div>
</div>
<section class="max-w-3xl mx-auto px-4 py-10 w-full">
  <div class="stack">
    <c:forEach var="f" items="${faqs}">
      <div class="card">
        <details>
          <summary class="cursor-pointer flex items-center gap-3 font-medium text-sm">
            <span class="w-8 h-8 rounded-full bg-indigo-50 grid place-items-center flex-none"><i class="fas fa-question text-indigo-500 text-sm"></i></span>
            ${f.pregunta}
            <i class="fas fa-chevron-down ml-auto text-gray-400 text-xs"></i>
          </summary>
          <p class="text-sm text-gray-600 mt-3 pl-11">${f.respuesta}</p>
        </details>
      </div>
    </c:forEach>
  </div>
  <c:if test="${empty faqs}"><div class="card"><div class="empty"><i class="fas fa-question-circle"></i><p>Sin preguntas registradas.</p></div></div></c:if>
</section>
<section class="py-12" style="background:linear-gradient(135deg,#667eea,#764ba2)">
  <div class="max-w-2xl mx-auto px-4 text-center text-white">
    <h2 class="text-2xl font-semibold mb-2">¿No encontraste lo que buscabas?</h2>
    <p class="text-indigo-100 text-sm mb-5">Si tienes alguna pregunta específica, no dudes en contactarnos directamente.</p>
    <div class="flex gap-3 justify-center flex-wrap">
      <a href="${pageContext.request.contextPath}/inicio" class="btn btn-primary"><i class="fas fa-phone mr-1"></i>Llamar Ahora</a>
    </div>
  </div>
</section>
<%@ include file="layout/footer.jsp" %>
