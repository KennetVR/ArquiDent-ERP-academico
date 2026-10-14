<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ include file="../layout/header.jsp" %>
<section class="auth">
  <div class="auth-card" style="max-width:520px">
    <div class="auth-head"><img class="auth-logo" src="${pageContext.request.contextPath}/images/arquident-logo.png" width="624" height="462" alt="ArquiDent · Clínica dental"><h1 style="font-size:22px">Crear Cuenta</h1><p>Regístrate en ArquiDent</p></div>
    <div class="auth-body">
      <c:if test="${not empty error}"><div class="alert alert-error"><i class="fas fa-exclamation-triangle mt-0.5"></i><span>${error}</span></div></c:if>
      <c:if test="${not empty mensaje}"><div class="alert alert-ok"><i class="fas fa-check-circle mt-0.5"></i><span>${mensaje}</span></div></c:if>
      <form method="post" action="${pageContext.request.contextPath}/registro">
        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
        <div class="field-row">
          <div class="field"><label>Nombre Completo *</label><input name="nombre" required minlength="2" maxlength="100" placeholder="Ingresa nombre completo"></div>
          <div class="field"><label>DNI *</label><input name="dni" required pattern="[0-9]{8}" maxlength="8" placeholder="12345678"></div>
        </div>
        <div class="field-row">
          <div class="field"><label>Correo Electrónico *</label><input type="email" name="correo" required maxlength="100" placeholder="ejemplo@correo.com"></div>
          <div class="field"><label>Teléfono *</label><input name="telefono" required maxlength="15" placeholder="987654321"></div>
        </div>
        <div class="field"><label>Dirección</label><input name="direccion" maxlength="500" placeholder="Ingresa tu dirección completa"></div>
        <div class="field-row">
          <div class="field"><label>Fecha de nacimiento</label><input type="date" name="nacimiento"></div>
          <div class="field"><label>Confirmar contraseña *</label><input type="password" id="confirm" required maxlength="128"></div>
        </div>
        <div class="field">
          <label>Contraseña *</label>
          <input type="password" name="contrasena" id="pass" required minlength="12" maxlength="128" placeholder="Mínimo 12 caracteres">
          <ul class="req-list">
            <li id="r1">Mínimo 12 caracteres</li>
            <li id="r2">Una letra mayúscula</li>
            <li id="r3">Una letra minúscula</li>
            <li id="r4">Un número</li>
            <li id="r5">Un carácter especial</li>
            <li id="r6">Coincide con la confirmación</li>
          </ul>
        </div>
        <button type="submit" class="btn btn-primary btn-block" style="background:linear-gradient(135deg,#667eea,#764ba2);color:#fff"><i class="fas fa-user-plus"></i>Crear Cuenta</button>
      </form>
      <script>
        const p=document.getElementById('pass'),c=document.getElementById('confirm');
        const rules=[
          ['r1',v=>v.length>=12],['r2',v=>/[A-Z]/.test(v)],['r3',v=>/[a-z]/.test(v)],
          ['r4',v=>/[0-9]/.test(v)],['r5',v=>/[^A-Za-z0-9]/.test(v)],['r6',v=>v.length>0&&v===c.value]
        ];
        const upd=()=>{rules.forEach(([id,ok])=>document.getElementById(id).classList.toggle('ok',ok(p.value)))};
        p.addEventListener('input',upd);c.addEventListener('input',upd);
        document.querySelector('form').addEventListener('submit',e=>{
          if(!rules.every(([,ok])=>ok(p.value))){e.preventDefault();alert('La contraseña no cumple los requisitos.')}
        });
      </script>
    </div>
  </div>
</section>
<%@ include file="../layout/footer.jsp" %>
