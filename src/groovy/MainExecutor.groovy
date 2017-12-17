def currProject = testRunner.testCase.testSuite.project
currProject.testSuites["InitializerSuite"].testCases["Initializer"].testSteps["ExcelParser"].run(testRunner, context)
currProject.testSuites["InitializerSuite"].testCases["Initializer"].testSteps["RESTSuiteCreator"].run(testRunner, context)

def excelParser = context.ExcelParser
def tCases = excelParser.getTestCases()
//printTestCases(tCases)

def creator = context.RESTSuiteCreator
creator.executeSuiteCreation(tCases) 

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

