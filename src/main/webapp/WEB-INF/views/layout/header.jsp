<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>ArquiDent</title>
  <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
  <link href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@7.1.0/css/all.min.css" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
  <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/images/arquident-isotipo.png">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/erp.css?v=marca-3">
</head>
<body>
  <nav class="topbar">
    <div class="topbar-inner max-w-7xl mx-auto px-4 flex items-center justify-between gap-4">
      <div class="flex items-center gap-3">
        <a href="${pageContext.request.contextPath}/inicio" class="brand" aria-label="ArquiDent — Inicio">
          <img class="brand-symbol" src="${pageContext.request.contextPath}/images/arquident-isotipo.png" width="46" height="46" alt="">
          <span class="brand-wordmark">ArquiDent<small>Clínica dental</small></span>
        </a>
      </div>
      <c:set var="rutaActual" value="${requestScope['javax.servlet.forward.servlet_path']}"/>
      <div class="navlinks" aria-label="Navegación principal">
        <a class="${rutaActual == '/inicio' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/inicio">Inicio</a>
        <a class="${rutaActual == '/servicios' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/servicios">Servicios</a>
        <a class="${rutaActual == '/nosotros' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/nosotros">Nosotros</a>
        <a class="${rutaActual == '/faq' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/faq">FAQ</a>
        <c:if test="${sessionScope.usuario != null}">
          <c:if test="${sessionScope.rolUsuario != 'paciente'}"><a class="${rutaActual == '/pacientes' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/pacientes">Pacientes</a></c:if>
          <a class="${rutaActual == '/citas' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/citas">Citas</a>
          <c:if test="${sessionScope.rolUsuario == 'administrador'}"><a class="${rutaActual == '/recursos' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/recursos">Recursos</a></c:if>
          <c:if test="${sessionScope.rolUsuario == 'secretaria' || sessionScope.rolUsuario == 'administrador'}"><a class="${rutaActual == '/caja' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/caja">Caja</a></c:if>
        </c:if>
      </div>
      <div class="topbar-actions flex items-center gap-3">
        <c:choose>
          <c:when test="${sessionScope.usuario != null}">
            <span class="nav-user text-white text-sm hidden md:inline"><i class="fas fa-circle-user mr-1"></i><c:out value="${sessionScope.usuario.nombre}"/></span>
            <form method="post" action="${pageContext.request.contextPath}/logout" style="display:inline">
              <input type="hidden" name="csrf" value="${sessionScope.csrf}">
              <button type="submit" class="btn btn-ghost btn-sm"><i class="fas fa-sign-out-alt mr-1"></i>Cerrar Sesión</button>
            </form>
          </c:when>
          <c:otherwise>
            <a href="${pageContext.request.contextPath}/login" class="btn btn-ghost btn-sm"><i class="fas fa-sign-in-alt mr-1"></i>Iniciar Sesión</a>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </nav>
