node {

	setMavenThreeAndJavaEleven()

	stage ('Build App')
	{
		echo 'Running Build App'
								
		checkout scm
		
		bat 'mvn clean package -DskipTests'
	}
	
	stage ('Run unit test')
	{
		echo 'Running Unit Tests'

		bat 'mvn clean test -Dtest=HolaControllerTest'
					
		step([$class: 'JUnitResultArchiver', testResults: "**/surefire-reports/*.xml"]) 
			
	}

	stage ('Run Integration test')
	{
		echo 'Running Integration Tests'

		bat 'mvn clean verify'
									
		step([$class: 'JUnitResultArchiver', testResults: '**/failsafe-reports/TEST-*.xml'])

	}
	
	stage('Calidad de codigo (SAST)') 
	{    
		echo 'Auditando código de WebApp'    

		withSonarQubeEnv {
			bat 'mvn clean package sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=93435e72689060d9036214b788c7e00f7bcc2e62'
		}
	}
	
	
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
	
	stage('Generate Cucumber report') {
cucumber buildStatus: 'UNSTABLE',
reportTitle: 'Cucumber tests',
fileIncludePattern: '**/*.json',
trendsLimit: 10,
classifications: [
[
'key': 'Browser',
'value': 'Chrome'
]
]
}
	
	
	stage ('Vulnerabilidades en librerias (SAST) - Owasp Dependency Check')
	{     
		echo 'Lanzando pruebas de seguridad de aplicacion estáticas: owasp-dependency-check' 
		
		try {
			bat 'mvn dependency-check:check -DskipTests'   
			
			dependencyCheckPublisher pattern: '**/target/dependency-check-report.xml'
		
			//step([$class: 'DependencyCheckPublisher', unstableTotalAll: '0'])
											
			publishHTML(target: [
				allowMissing: false, 
				alwaysLinkToLastBuild: true, 
				keepAll: true, 
				reportDir: 'target', 
				reportFiles: 'dependency-check-report.html', 
				reportName: 'Vulnerabilidades en librerias'])			
			
		
		} catch (Exception e) {
			dependencyCheckPublisher pattern: '**/target/dependency-check-report.xml'
			
			publishHTML(target: [
				allowMissing: false, 
				alwaysLinkToLastBuild: true, 
				keepAll: true, 
				reportDir: 'target', 
				reportFiles: 'dependency-check-report.html', 
				reportName: 'Vulnerabilidades en librerias'])					
		}
	}
	
	
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
	

}



/**
 * Set maven 3 and Java 11 in path
 */
def setMavenThreeAndJavaEleven() {
	env.M2_HOME="${tool 'maven-3'}"
	env.JAVA_HOME="${tool 'jdk11'}"
	env.PATH="${env.JAVA_HOME}/bin;${env.PATH}"
	env.PATH="${env.M2_HOME}/bin;${env.PATH}"
	echo '*** env.PATH: ' + env.PATH
}
