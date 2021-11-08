def call(body)
{
	
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
    	body.delegate = config
    	body()
      def workspace = "%CD%"
      def sonarRunner = config.sonar_Runner
      def MSBuildPath =  config.MS_BuildPath
      def sonarProjectName = config.sonar_projectName
      def excludePath = config.exclude_Path
      def publishPath = config.publish_Path
      def srcpath = config.src_path
      def openCover = config.open_Cover
      def NUnitConsole = config.nunitConsole
      def reportGenerator= config.report_Generator
      def SonarRunner = config.sonar_Runner

    	

      withSonarQubeEnv('Sonarqube-Server-Prod') {
            
         script {
            bat """ 
            dir
            "${openCover}" -output:coverage.xml  -register:administrator -filter:"+[EDR*]*-[*Test]*" -target:"${NUnitConsole}" -targetargs:".\\EDRHub_UnitTestProject\\bin\\Release\\EDRHub_UnitTestProject.dll -process=Multiple"  """ 
            bat """ "${sonarRunner}" begin /v:1.0 /k:"${sonarProjectName}" /n:"${sonarProjectName}" /d:sonar.verbose=false /d:sonar.branch.name="${BRANCH_NAME}" /d:sonar.coverage.exclusions="**\\EDRHub.BLL\\**","**\\EDRHub.Common\\**" """
            bat """ "${MSBuildPath}" "${workspace}\\${srcpath}" /t:Rebuild  /p:PublishProfile="${workspace}\\${publishPath}" /p:ExcludeFromPackageFiles="${excludePath}" /p:Configuration=Release /m """
            bat """ "${sonarRunner}" end """
         }
      }
}
