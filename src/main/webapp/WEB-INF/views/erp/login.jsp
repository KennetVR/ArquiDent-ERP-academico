<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ include file="../layout/header.jsp" %>
<section class="auth">
  <div class="auth-card">
    <div class="auth-head">
      <img class="auth-logo" src="${pageContext.request.contextPath}/images/arquident-logo.png" width="624" height="462" alt="ArquiDent · Clínica dental">
      <h1>Iniciar Sesión</h1>
      <p>Accede a tu cuenta de ArquiDent</p>
    </div>
    <div class="auth-body">
      <c:if test="${not empty error}"><div class="alert alert-error"><i class="fas fa-exclamation-triangle mt-0.5"></i><span>${error}</span></div></c:if>
      <c:if test="${not empty mensaje}"><div class="alert alert-ok"><i class="fas fa-check-circle mt-0.5"></i><span>${mensaje}</span></div></c:if>
      <form method="post" action="${pageContext.request.contextPath}/login">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <div class="field">
          <label for="login-correo">Correo Electrónico</label>
          <input id="login-correo" type="email" name="correo" autocomplete="username" required maxlength="100" placeholder="ejemplo@correo.com">
        </div>
        <div class="field">
          <label for="login-clave">Contraseña</label>
          <input id="login-clave" type="password" name="contrasena" autocomplete="current-password" required maxlength="128" placeholder="Ingresa tu contraseña">
        </div>
        <button type="submit" class="btn btn-solid btn-block"><i class="fas fa-sign-in-alt" aria-hidden="true"></i>Iniciar Sesión</button>
      </form>
    </div>
  </div>
</section>
<%@ include file="../layout/footer.jsp" %>
