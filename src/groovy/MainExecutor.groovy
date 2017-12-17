import org.json.*

def currProject = testRunner.testCase.testSuite.project
currProject.testSuites["InitializerSuite"].testCases["Initializer"].testSteps["ExcelParser"].run(testRunner, context)
currProject.testSuites["InitializerSuite"].testCases["Initializer"].testSteps["RESTSuiteCreator"].run(testRunner, context)

def excelParser = context.ExcelParser
def tCases = excelParser.getTestCases()
//printTestCases(tCases)

def creator = context.RESTSuiteCreator
creator.executeSuiteCreation(tCases) 

def executor = new MainExecutor(log, context, currProject)
executor.executeSoapUITestCases(tCases)

def printTestCases(tCases) {
	log.info("num test cases: " + tCases.size())
	for(def tCase in tCases) {
		log.info("test case ID: " + tCase.ID)
		log.info("request name: " + tCase.requestName)
		log.info("input data: " + tCase.inputData)
		log.info("expected results: " + tCase.expectedResults)
		log.info("next test case")
	}
}

class MainExecutor {
	def log
	def context
	def project
	def restSuite

	MainExecutor(log, context, project) {
		this.log = log
		this.context = context
		this.project = project
		this.restSuite = project.testSuites["RESTSuite"]
	}

	def executeSoapUITestCases(tCases) {
		for(tCase in tCases){
			def soapUITCase = restSuite.testCases[tCase.ID]
			executeSoapUITestCase(tCase)
		}
	}

	def executeSoapUITestCase(tCase) {
		def testStep = restSuite.testCases[tCase.ID].testSteps[tCase.requestName]
		if(testStep.name.startsWith("Post") || testStep.name.startsWith("Put")){
			testStep.httpRequest.requestContent = tCase.inputData
		}

		if(testStep.name.startsWith("Put") || testStep.name.equals("Get Comment")){
			def id = getCachedValue(tCase.refTCaseID)
			if(id != null){
				testStep.setPropertyValue("id", id.toString())
			}
		}

		runTestStep(tCase.ID, testStep)
	}

	def runTestStep(tCaseID, testStep) {
		def result = testStep.run(context.testRunner, context)
		def status = testStep.httpRequest.response.responseHeaders["#status#"].toString()
		def response = testStep.getPropertyValue("Response")

		if(testStep.name.startsWith("Post") && status.contains("Created")){
			cachePOSTResponseData(tCaseID, response)
		}
	}

	def cachePOSTResponseData(tCaseID, response) {
		def jObject = new JSONObject(response)
		restSuite.setPropertyValue(tCaseID, jObject.get("id").toString())
		project.save()
	}

	def getCachedValue(propName) {
		return restSuite.getPropertyValue(propName)
	}
}

