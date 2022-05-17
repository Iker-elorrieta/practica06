# Pasos para realizar el ejercicio

## BDD - CUCUMBER TESTS

### 1.- Actualización de fichero Jenkinsfile para añadir nuevo stage de ejecución de tests BDD

	--

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.junit</groupId>
				<artifactId>junit-bom</artifactId>
				<version>5.8.2</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
			<dependency>
				<groupId>io.cucumber</groupId>
				<artifactId>cucumber-bom</artifactId>
				<version>7.3.4</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	--

	--

	<dependencies>
		…
		<dependency>
			<groupId>io.cucumber</groupId>
			<artifactId>cucumber-java</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>io.cucumber</groupId>
			<artifactId>cucumber-junit-platform-engine</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>io.cucumber</groupId>
			<artifactId>cucumber-spring</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.junit.platform</groupId>
			<artifactId>junit-platform-suite</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.junit.jupiter</groupId>
			<artifactId>junit-jupiter</artifactId>
			<scope>test</scope>
		</dependency>
		…
	</dependencies>

	--

### 2.- Reutilizamos los pasos y las clases de java definidos en el ejercicio 5.

Repetimos el paso 4 del ejercicio 5 creando el fichero “is_it_friday_yet.feature” en la siguiente ruta: “src/test/resources/hellocucumber/” 

Copiamos el directorio “hellocucumber” que contine las clases RunCucumberTest.java y “StepDefinitions.java” manteniendo la ruta en el destino.


### 3.- Validamos que la ejecución de los tests en la consola de comandos

Modificamos el contenido de la clase RunCucumberTest.java por el que se muestra a continuación:

	--
	package hellocucumber;

	import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
	import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

	import org.junit.platform.suite.api.ConfigurationParameter;
	import org.junit.platform.suite.api.IncludeEngines;
	import org.junit.platform.suite.api.SelectClasspathResource;
	import org.junit.platform.suite.api.Suite;

	import io.cucumber.spring.CucumberContextConfiguration;

	@Suite
	@IncludeEngines("cucumber")
	@SelectClasspathResource("hellocucumber")
	@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
	@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "json:target/cucumber.json")	
	@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "hellocucumber")
	@CucumberContextConfiguration
	public class RunCucumberTest {
	}
	--

Ejecutamos comando Maven: mvn clean test

Los informes de resultados de Cucumber se generarán en el directorio: \target\surefire-reports


### 4.- Creamos nuevo pipeline en Jenkins para probar la ejecución de tests con Cucumber

Los tests de Cucumber se ejecutarán en el stage “Run Integration test” al utilizarse el comando “mvn clean verify”
