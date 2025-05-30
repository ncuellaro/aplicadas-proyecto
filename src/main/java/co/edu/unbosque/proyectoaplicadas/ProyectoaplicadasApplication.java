package co.edu.unbosque.proyectoaplicadas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ProyectoaplicadasApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProyectoaplicadasApplication.class, args);
	}

}
