def call(body)
{
def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
    	body.delegate = config
    	body()
    	def MSBuildPath = config.MS_BuildPath
    	def PublishProfile = config.PublishProfile
    	def excludePath = config.exclude_Path
      def publishPath = config.publish_Path

    	
// FYI : here srcpath variable is again getting the value of src_path which gets updated in pipeline.groovy

    //	def msbuildtoolv17path = config.msbuildtoolv17_path
    	
       		bat """ 
       		"C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\Community\\MSBuild\\Current\\Bin\\MSBuild.exe" /t:clean "${srcpath}"
       		"C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\Community\\MSBuild\\Current\\Bin\\MSBuild.exe" "${srcpath}" /m /p:configuration="release" /p:Platform=\"Any CPU\" /p:ProductVersion=1.0.0.%BUILD_NUMBER% /T:Build /p:DeployOnBuild=true /p:WebPublishMethod=FileSystem /p:SkipInvalidConfigurations=true /p:DeleteExistingFiles=true"
       		"""

}
