# Pasos para realizar la práctica

## OWASP DEPENDENCY CHECK

### 1.- Actualización de fichero Jenkinsfile para añadir nuevo stage de auditoría de seguridad (SAST).

	--
	stage ('Vulnerabilidades en librerias (SAST) - Owasp Dependency Check')
	{     
		echo 'Lanzando pruebas de seguridad de aplicacion estáticas: owasp-dependency-check' 
		
		try {
			bat 'mvn dependency-check:check -DskipTests'   
			
			dependencyCheckPublisher pattern: '**/target/dependency-check-report.xml'
		
			//step([$class: 'DependencyCheckPublisher', unstableTotalAll: '0'])
											
			/*publishHTML(target: [
				allowMissing: false, 
				alwaysLinkToLastBuild: true, 
				keepAll: true, 
				reportDir: 'target', 
				reportFiles: 'dependency-check-report.html', 
				reportName: 'Vulnerabilidades en librer\u00edas']) 
				}
			*/
		
		} catch (Exception e) {
			dependencyCheckPublisher pattern: '**/target/dependency-check-report.xml'					
		}
	}
	--

### 2.- Actualización de fichero pom.xml para añadir dependencia de owasp dependency check

	--
	<build>	
		<plugins>

			...
	
			<plugin>
			  <groupId>org.owasp</groupId>
			  <artifactId>dependency-check-maven</artifactId>
			  <version>7.0.4</version>
			  <configuration>					
					<failBuildOnCVSS>7</failBuildOnCVSS>			
					<!-- Fallar pipeline si hay nuevas vulnerabilidades críticas -->
					<failedNewCritical>true</failedNewCritical>							
					<!-- Generar todos los formatos del informe -->
					<format>ALL</format>					
					<!-- Validar si estamos utilizando la última versión -->					
					<versionCheckEnabled>true</versionCheckEnabled>
					<!-- No usar Nexus Analyzer -->
					<centralAnalyzerEnabled>false</centralAnalyzerEnabled>
			  </configuration>
			</plugin>
	
			...
	
		</plugins>
	</build>
	--
	
	--
	<reporting>
	
	...
	
		<plugin>
			<groupId>org.owasp</groupId>
			<artifactId>dependency-check-maven</artifactId>
			<version>7.0.4</version>
			<configuration>					
				<!-- Generar todos los formatos del informe -->
				<format>ALL</format>
				<!-- No usar Nexus Analyzer -->
				<centralAnalyzerEnabled>false</centralAnalyzerEnabled>
				<!-- Validar si estamos utilizando la última versión -->
				<versionCheckEnabled>true</versionCheckEnabled>
			</configuration>			
			<reportSets>
				<reportSet>
					<reports>
						<report>aggregate</report>
					</reports>
				</reportSet>
			</reportSets>				
		</plugin>
	
	...
	</reporting>
	--

### 3.- Ejecutamos manualmente comando Maven desde consola de comandos y validamos que se generar el informe de vulnerabilidades.

	--
	mvn dependency-check:check -DskipTests
	--

	y visualizamos informe generado "dependency-check-report.html" en la carpeta /target/


### 4.- Instalación de OWASP Dependency check plugin en Jenkins

	4.1 Accedemos a la página de plugins de Jenkins: http://localhost:8080/pluginManager/
	
	4.2 buscamos e instalamos el plugin de OWASP Dependency Check

### 5.- Creamos nuevo job multibranch en Jenkins para validar que se ejecuta el plugin de owasp dependency check y se genera el informe de vulnerabilidades.




## OWASP ZAP

### 1.- Actualización de fichero Jenkinsfile para añadir nuevo stage de auditoría de seguridad en tiempo de ejecución (DAST).

	--
	stage('Pruebas de seguridad de aplicaci\u00f3n din\u00E1micas (DAST)') {

		echo 'Lanzando pruebas de seguridad de aplicaci\u00f3n din\u00E1micas'

		def userInput = input(message: '¿Lanzar pruebas de seguridad?', ok: 'Continue',
								parameters: [booleanParam(defaultValue: false,
								description: 'Selecciona la opci\u00f3n de Ejecutar or Continuar con la siguiente fase',name: 'Ejecutar')])
		echo ("procede: " + userInput)

		if (userInput)
		{
			try {
				bat 'mvn br.com.softplan.security.zap:zap-maven-plugin:analyze -DskipTests'

				publishHTML(target: [
					allowMissing: true, 
					alwaysLinkToLastBuild: true, 
					keepAll: true, 
					reportDir: 'target/zap-reports', 
					reportFiles: 'zapReport.html', 
					reportName: 'OWASP ZAP Scan'])
			} catch(error) {
				echo "Las pruebas han fallado"

				retry(5) {
					input "Fallo en la pruebas de seguridad,¿intentar de nuevo?"

				bat 'mvn br.com.softplan.security.zap:zap-maven-plugin:analyze -DskipTests'

				publishHTML(target: [
					allowMissing: true, 
					alwaysLinkToLastBuild: true, 
					keepAll: true, 
					reportDir: 'target/zap-reports', 
					reportFiles: 'zapReport.html', 
					reportName: 'OWASP ZAP Scan']) 
				}
			}
		}
	} 
	--

### 2.- Actualización de fichero pom.xml para añadir dependencia de owasp ZAP

	--
	<plugin>
		<groupId>br.com.softplan.security.zap</groupId>
		<artifactId>zap-maven-plugin</artifactId>
		<version>1.2.1-0</version>
		<configuration>
		<zapPort>8082</zapPort>
		<targetUrl>http://localhost:8081/</targetUrl>
		<context>http://localhost:8081/petclinic/*</context>
		<activeScanStartingPointUrl>http://localhost:8081/</activeScanStartingPointUrl>
		<spiderStartingPointUrl>http://localhost:8081/</spiderStartingPointUrl>
		<zapPath>C:\temp_fp\OWASP\Zed Attack Proxy</zapPath>
		<shouldRunAjaxSpider>true</shouldRunAjaxSpider>
		</configuration>				
	</plugin>	
	--

### 3.- Instalación y configuración de OWASP ZAP en entorno local.

	Descargamos el instalador desde la página oficial: https://www.zaproxy.org/  (Versión utilizada 2.11.1 Windows (64) installer)

	Arrancamos OWASP ZAP desde el directorio de instalación: C:\temp_fp\OWASP\Zed Attack Proxy\ZAP.exe
	
	Especificar puerto si el 8080 ya está siendo utilizado por otra app.
	
	Abrimos la ventana de Options desde el menú “Tools”.

	Seleccionamos “AJAX Spider” y cambiamos el valor de navegador por  “Chrome Headless”:


### 4.- Ejecutamos manualmente comando Maven desde consola de comandos y validamos que se generar el informe de vulnerabilidades.

	- (?) Antes de ejecutar el comando validar que tanto la App Spring Boot como OWASP ZAP se están ejecutando.

	--
	mvn br.com.softplan.security.zap:zap-maven-plugin:analyze -DskipTests
	--
	
	Ruta: C:\temp_fp\GitHub\practica_04\target\zap-reports

	Fichero: zapReport.html


### 5.- Lanzamos nuevo build en Jenkins para validar que se ejecuta el stage de OWASP ZAP y se genera el informe de vulnerabilidades.


## FindSecbugs

### 1.- Actualización de fichero Jenkinsfile para añadir nuevo stage de Site que nos mostrará el informe generado por el plugin de FindSecBugs (SAST).

	--
	stage ('Documentacion de proyecto') 
	{
		echo 'Generando documentacionn de proyecto'
	 
		bat 'mvn site:site -DskipTests'  

		publishHTML(target: [
			allowMissing: false, 
			alwaysLinkToLastBuild: true, 
			keepAll: true, 
			reportDir: 'target/site', 
			reportFiles: 'index.html', 
			reportName: 'Documentacion de proyecto'])   
	}
	--

### 2.- Actualización de fichero pom.xml para añadir plugin de FindSecBugs

	Sección build

	--
	<plugin>
	  <groupId>com.github.spotbugs</groupId>
	  <artifactId>spotbugs-maven-plugin</artifactId>
	  <version>4.6.0.0</version>
	  <configuration>
		<includeFilterFile>src/main/resources/spotbugs-security-include.xml</includeFilterFile>
		<excludeFilterFile>src/main/resources/spotbugs-security-exclude.xml</excludeFilterFile>
		<plugins>
			<plugin>
				<groupId>com.h3xstream.findsecbugs</groupId>
				<artifactId>findsecbugs-plugin</artifactId>
				<version>1.12.0</version>
			</plugin>
		</plugins>
	  </configuration>
	  <dependencies>
		<!-- overwrite dependency on spotbugs if you want to specify the version of spotbugs -->
		<dependency>
		  <groupId>com.github.spotbugs</groupId>
		  <artifactId>spotbugs</artifactId>
		  <version>4.6.0</version>
		</dependency>
	  </dependencies>
	</plugin>	
	--


	Sección reporting
	--         
	<!-- Spotbugs report -->
	<plugin>
		<groupId>com.github.spotbugs</groupId>
		<artifactId>spotbugs-maven-plugin</artifactId>
		<version>4.2.0</version>
		<configuration>
			<effort>Max</effort>
			<threshold>Low</threshold>
			<failOnError>false</failOnError>
			<includeFilterFile>src/main/resources/spotbugs-security-include.xml</includeFilterFile>
			<excludeFilterFile>src/main/resources/spotbugs-security-exclude.xml</excludeFilterFile>
			<xmlOutput>true</xmlOutput>
			<!-- Optional directory to put spotbugs xdoc xml report -->
			<xmlOutputDirectory>target/site</xmlOutputDirectory>
			<plugins>
				<plugin>
					<groupId>com.h3xstream.findsecbugs</groupId>
					<artifactId>findsecbugs-plugin</artifactId>
					<version>LATEST</version> <!-- Auto-update to the latest stable -->
				</plugin>
			</plugins>
		</configuration>
	</plugin>

	--


### 3.-  Creación de ficheros xml de configuración en la carpeta /src/main/resources:

	spotbugs-security-exclude.xml
	--
	<FindBugsFilter>
	</FindBugsFilter>
	--
	
	spotbugs-security-include.xml
	--
	<FindBugsFilter>
		<Match>
			<Bug category="SECURITY"/>
		</Match>
	</FindBugsFilter>
	--

### 4.- Ejecutamos manualmente comando Maven desde consola de comandos y validamos que se generar el informe de vulnerabilidades en formato xml.

	--
	mvn spotbugs:spotbugs
	--

### 5.- Editamos pom.xml file para añadir plugin “maven-project-info-reports-plugin” en la sección de reporting

	--
	<plugin>
		<groupId>org.apache.maven.plugins</groupId>
		<artifactId>maven-project-info-reports-plugin</artifactId>
		<version>3.2.2</version>
		<configuration>
			<dependencyLocationsEnabled>false</dependencyLocationsEnabled>
		</configuration>
		<reportSets>
			<reportSet>
				<reports>
					<report>ci-management</report>
					<report>dependencies</report>
					<report>dependency-info</report>	                        
					<report>dependency-convergence</report>
					<report>dependency-management</report>
					<report>distribution-management</report>
					<report>help</report>
					<report>index</report>
					<report>issue-management</report>							
					<report>licenses</report>
					<report>modules</report>
					<report>plugin-management</report>
					<report>plugins</report>
					<report>summary</report>
					<report>team</report>
				</reports>
			</reportSet>
		</reportSets>
	</plugin>
	--


### 6.- Editamos pom.xml file para añadir plugin “maven-site-plugin” en la sección de build

	--
	<plugin>
	  <groupId>org.apache.maven.plugins</groupId>
	  <artifactId>maven-site-plugin</artifactId>
	  <version>3.12.0</version>
	</plugin>	
	--

### 7.- Ejecutamos el siguiente comando para validar que se genera el Site

	Comando: mvn site:site -DskipTests
	
	Ruta: C:\temp_fp\GitHub\practica_04\target\site

### 8.- Lanzamos nuevo build desde Jenkins para validar que se ejecuta el nuevo stage de Site.


## JMeter

### 1.- Actualización de fichero Jenkinsfile para añadir nuevo stage de pruebas de rendimiento.

	--
	stage ('Pruebas de rendimiento')
	{
		echo 'Lanzando pruebas de rendimiento'  
		
		bat "mvn test-compile verify -PjmeterDEV -DskipTests=false" 
		
		step([$class: 'ArtifactArchiver', artifacts: 'target/jmeter/reports/**/*.*', fingerprint: true]) 
		
		performanceReport compareBuildPrevious: true,
			configType: 'ART',
			errorFailedThreshold: 5,
			errorUnstableResponseTimeThreshold: '',
			errorUnstableThreshold: 2,
			failBuildIfNoResultFile: false,
			modeOfThreshold: false,
			modePerformancePerTestCase: true,
			modeThroughput: true, nthBuildNumber: 0, parsers: [[$class: 'JMeterParser', glob: "target/jmeter/results/*.csv"]],
			relativeFailedThresholdNegative: 0,
			relativeFailedThresholdPositive: 5,
			relativeUnstableThresholdNegative: 5,
			relativeUnstableThresholdPositive: 0
	}    
	--

### 2.- Actualización de fichero pom.xml para añadir plugins de JMeter

	--
	<profiles>
		<profile>
			<id>jmeterDEV</id>			
			<build>
				<plugins>
					<plugin>
						<groupId>com.lazerycode.jmeter</groupId>
						<artifactId>jmeter-maven-plugin</artifactId>
						<version>2.7.0</version>						
						<executions>
							<execution>
								<id>jmeter-tests</id>
								<goals>
									<goal>jmeter</goal>
								</goals>
								<phase>integration-test</phase>
							</execution>
							<execution>
								<id>jmeter-check-results</id>
								<goals>
									<goal>results</goal>
								</goals>
							</execution>
						</executions>
						<configuration>
							<generateReports>true</generateReports> 
							<resultFilesLocations>${project.build.directory}/jmeter/results</resultFilesLocations>
							<errorRateThresholdInPercent>1</errorRateThresholdInPercent>
							<jMeterProcessJVMSettings>
							  <xms>1024</xms>
							  <xmx>1024</xmx>
							  <arguments>
								<argument>-Xprof</argument>
								<argument>-Xfuture</argument>
							  </arguments>
							</jMeterProcessJVMSettings>
							<skipTests>false</skipTests>
							<testResultsTimestamp>true</testResultsTimestamp>				 
							<overrideRootLogLevel>DEBUG</overrideRootLogLevel>				 
							<suppressJMeterOutput>false</suppressJMeterOutput>				 
							<ignoreResultFailures>true</ignoreResultFailures>				
							<propertiesJMeter>
								<log_level.jmeter>DEBUG</log_level.jmeter>
								<jmeter.save.saveservice.thread_counts>true</jmeter.save.saveservice.thread_counts>								
							</propertiesJMeter>
							<jmeterPlugins>
								<plugin>
									<groupId>kg.apc</groupId>
									<artifactId>jmeter-plugins</artifactId>
								</plugin>
							</jmeterPlugins>	
							<!-- Adding jar to the /lib directory-->
							<testFilesIncluded>
								<jMeterTestFile>democn_test_plan.jmx</jMeterTestFile>								
							</testFilesIncluded>
						</configuration>
						<dependencies>		
							<dependency>
								<groupId>junit</groupId>
								<artifactId>junit</artifactId>
								<version>4.12</version>								
							</dependency>
							<dependency>
							  <groupId>kg.apc</groupId>
							  <artifactId>jmeter-plugins</artifactId>
							  <version>1.0.0</version>
							  <exclusions>
									<exclusion>
										<groupId>kg.apc</groupId>
										<artifactId>perfmon</artifactId>
									</exclusion>
									<exclusion>
										<groupId>org.apache.hadoop</groupId>
										<artifactId>hadoop-core</artifactId>
									</exclusion>
									<exclusion>
										<groupId>org.apache.hbase</groupId>
										<artifactId>hbase</artifactId>
									</exclusion>
									<exclusion>
										<groupId>org.apache.jmeter</groupId>
										<artifactId>jorphan</artifactId>
									</exclusion>
									<exclusion>
										<groupId>org.apache.bsf</groupId>
										<artifactId>bsf-api</artifactId>
									</exclusion>
									<exclusion>
										<groupId>org.bouncycastle</groupId>
										<artifactId>bcmail-jdk15</artifactId>
									</exclusion>
									<exclusion>
										<groupId>org.bouncycastle</groupId>
										<artifactId>bcprov-jdk15</artifactId>
									</exclusion>
									<exclusion>
										<groupId>javax.activation</groupId>
										<artifactId>activation</artifactId>
									</exclusion>
									<exclusion>
										<groupId>commons-logging</groupId>
										<artifactId>commons-logging</artifactId>
									</exclusion>
								</exclusions>
							</dependency>
						</dependencies>
					</plugin>									
				</plugins>
			</build>
		</profile>
	</profiles>
	--

### 3.- Instalación de Jmeter desde link: https://jmeter.apache.org. Ej. apache-jmeter-5.4.3.zip
 
	Ej. apache-jmeter-5.4.3.zip
	
	Descrompimos el fichero zip en nuestro directorio de trabajo.
 
 
### 4.- Creación de ficheros plan de JMeter en una nueva carpeta /src/main/jmeter

	- Arrancamos Jmeter desde la carpate de instalación: C:\temp_fp\apache-jmeter-5.4.3\bin\jmeter.bat
	- Creación de ficheros plan de JMeter en una nueva carpeta /src/main/jmeter
	- Abrimos el plan proporcionado para la práctica: democn_test_plan.jmx
	

### 5.- Ejecutamos manualmente comando Maven desde consola de comandos y validamos que se generar el informe de JMeter

	--
	mvn test-compile verify -PjmeterDEV -DskipTests=false
	--

### 6.- Lanzamos nuevo build desde Jenkins para validar que se ejecuta el nuevo stage de Site.


	