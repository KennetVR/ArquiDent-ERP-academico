package pe.edu.utp.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

@WebListener
public class DatabaseCleanupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Se ejecuta cuando la aplicación se inicia
        System.out.println("ArquiDent: Aplicación iniciada correctamente");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Se ejecuta cuando la aplicación se cierra
        System.out.println("ArquiDent: Limpiando recursos de base de datos...");
        
        // Limpiar drivers JDBC registrados
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                System.out.println("ArquiDent: Driver JDBC desregistrado: " + driver);
            } catch (SQLException e) {
                System.err.println("ArquiDent: Error al desregistrar driver JDBC: " + e.getMessage());
            }
        }
        
        // Intentar detener el hilo de limpieza de MySQL
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
            System.out.println("ArquiDent: Hilo de limpieza de MySQL detenido correctamente");
        } catch (Exception e) {
            System.err.println("ArquiDent: Error al detener hilo de limpieza de MySQL: " + e.getMessage());
        }
    }
}
