<%@ page contentType="text/html;charset=UTF-8" language="java" %><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ include file="../layout/header.jsp" %>
<section class="py-20">
    <div class="max-w-2xl mx-auto px-4 text-center">
        <div class="bg-white rounded-3xl shadow-2xl p-10">
            <i class="fas fa-exclamation-triangle text-5xl text-red-400 mb-6"></i>
            <h2 class="text-3xl font-bold text-gray-800 mb-4">No se completó la operación</h2>
            <c:if test="${not empty error}">
                <p class="text-gray-600 mb-6">${error}</p>
            </c:if>
            <c:if test="${empty error}">
                <p class="text-gray-600 mb-6">La página solicitada no existe o no tiene permiso para verla.</p>
            </c:if>
            <a href="${pageContext.request.contextPath}/citas" class="bg-primary-500 text-white px-8 py-3 rounded-full font-bold hover:bg-primary-600">Ir a Citas</a>
        </div>
    </div>
</section>
<%@ include file="../layout/footer.jsp" %>
