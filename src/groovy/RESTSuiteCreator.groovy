import com.eviware.soapui.config.*
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory

/**
 * Creates the 'RESTSuite' with SoapUI test cases and REST steps to run the REST APIs.
 */
class RESTSuiteCreator {
	def log
	def context
	def project
	def final suiteName = "RESTSuite"
	
	RESTSuiteCreator(paramLog, paramContext) {
		log = paramLog
		context = paramContext
		project = context.testCase.testSuite.project
		log.info(project)
	}

	def executeSuiteCreation(tCases) {
		if(project.testSuites["RESTSuite"] == null){
			createSuite()
		}

		createSoapUITestCases(tCases)
	}

	def createSuite() {
		project.addNewTestSuite(suiteName)
		project.save() 
	}

	def createSoapUITestCases(tCases) {
		def suite = project.testSuites["RESTSuite"]
		for(tCase in tCases){
			if(suite.testCases[tCase.ID] == null){
				def soapUITCase = suite.addNewTestCase(tCase.ID)
				def request = getRESTRequest(tCase.requestName)
	
				def config = RestRequestStepFactory.createConfig(request, request.name)
				soapUITCase.insertTestStep(config, 0)
				soapUITCase.testSteps[request.name].addAssertion("Script Assertion")
				//log.info(request.name)
			}
		}

		project.save()
	}

	def getRESTRequest(requestName) {
		for(def resource in project.getInterfaceList()[0].getResourceList()){
			for(def request in resource.getRequestList()){
				if(requestName == request.name){
					return request
				}
			}
		}
		return null
	}
}

context.setProperty("RESTSuiteCreator", new RESTSuiteCreator(log, context))