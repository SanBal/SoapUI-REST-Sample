import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.ss.util.*
import javax.json.*

class ExcelParser{   
	def log
	Sheet sheet

	ExcelParser(paramLog, context){
		log = paramLog
		def FILE_PATH = context.expand('${projectDir}') + '/src/resources/TestData.xlsx'
		log.info(FILE_PATH)
		FileInputStream excelFile = new FileInputStream(new File(FILE_PATH));
		Workbook workbook = new XSSFWorkbook(excelFile);
		sheet = workbook.getSheetAt(0);
	}
    
	def getTestCases() { 
		List<TestCase> tCases = new ArrayList<>()
		def rowCount = sheet.getLastRowNum() + 1;
		def colCount = sheet.getRow(0).getLastCellNum();
		//log.info(rowCount + " " + colCount);

		def tCaseID = sheet.getRow(0).getCell(2).getStringCellValue()
		def requestName = sheet.getRow(4).getCell(2).getStringCellValue()
		def tCase = new TestCase(tCaseID, requestName)

		def jObject = Json.createObjectBuilder()
		def jArrayMap = [:]
		def jArrayObjectsMap = [:]
		def jArraysKeys = ['Pers']

		initializeJsonArrayMap(jArrayMap, jArraysKeys)
		initializeJsonArrayObjectsMap(jArrayObjectsMap, jArraysKeys)

		for(int colIndex = 2; colIndex < colCount; colIndex++) {
			def currTCaseID = sheet.getRow(0).getCell(colIndex).getStringCellValue()
			if(!currTCaseID.isEmpty() && currTCaseID != tCase.ID){
				tCases << tCase 
				requestName = sheet.getRow(4).getCell(colIndex).getStringCellValue()
				tCase = new TestCase(currTCaseID, requestName)
			}
			for(int rowIndex = 7; rowIndex < rowCount; rowIndex++) {
				Row row = sheet.getRow(rowIndex)
				Cell cJsonType = row.getCell(1)
				Cell cValue = row.getCell(colIndex)

				if(cJsonType != null && cValue != null) {
					cValue.setCellType(Cell.CELL_TYPE_STRING)
					//log.info(cJsonType.getStringCellValue())
					//log.info(cValue.getStringCellValue())	
					addValueToJsonObject(cJsonType.getStringCellValue(), cValue.getStringCellValue(), jArraysKeys, jArrayObjectsMap, jObject)					
				}
			}

			addJsonArrayObjectsToJsonArray(jArraysKeys, jArrayObjectsMap, jArrayMap)
			
			Cell cNextAddContext = sheet.getRow(2).getCell(colIndex + 1)
			log.info(tCase.ID)
			if(cNextAddContext != null && cNextAddContext.getStringCellValue().contains("Add")) {
				continue
			}
			
			addJsonArraysToJsonObject(jArraysKeys, jArrayMap, jObject)
			//log.info(jObject.build().toString())

			Cell cContext = sheet.getRow(1).getCell(colIndex)
			Cell cAddContext = sheet.getRow(2).getCell(colIndex)
			def jString = jObject.build().toString()
			if(cContext.getStringCellValue()?.trim()) {
				//log.info(cContext.getStringCellValue())
				addJsonToTestCase(jString, cContext.getStringCellValue(), tCase)
			}else if(cAddContext.getStringCellValue()?.trim()) {
				//log.info(cAddContext.getStringCellValue())
				addJsonToTestCase(jString, cAddContext.getStringCellValue(), tCase)
			}

			if(colIndex == colCount - 1) {
				 tCases << tCase 
			}
		}
	
		return tCases                           
	}

	def addValueToJsonObject(jType, value, jArraysKeys, jArrayObjectsMap, jObject) {
		if(isJsonTypeJsonArrayType(jArraysKeys, jType)){
			for(key in jArraysKeys) {
				def jArrayObject = jArrayObjectsMap."$key"
				jType = jType.replace(key, "").toString()
				if(jType == "plz"){
					value = value.toInteger()
				}else if(jType.toLowerCase().contains("date")) {
					value = getDateString(value.toInteger())
				}
				jArrayObject.add(jType, value)
			}
		}else{
			if(jType == "postID"){
				value = value.toInteger()
			}
			jObject.add(jType, value)
		}
	}

	def getDateString(days) {
		def dateValue = new Date() + days
		return dateValue.format("yyyy-MM-dd")
	}

	def initializeJsonArrayMap(jArrayMap, jArraysKeys) {
		jArraysKeys.each {key ->
			jArrayMap."$key" = Json.createArrayBuilder()
		}
	}

	def initializeJsonArrayObjectsMap(jArrayObjectsMap, jArraysKeys) {
		jArraysKeys.each {key ->
			jArrayObjectsMap."$key" = Json.createObjectBuilder()
		}
	}

	def isJsonTypeJsonArrayType(jArraysKeys, jType) {
		boolean isJsonArrayType = false
		jArraysKeys.each {key ->
			if(jType.contains("$key")){
				isJsonArrayType =  true
			}
		}
		return isJsonArrayType
	}

	def addJsonArrayObjectsToJsonArray(jArraysKeys, jArrayObjectsMap, jArrayMap) {
		jArraysKeys.each {key ->
			def jArrayObject = jArrayObjectsMap."$key".build()
			def jArray = jArrayMap."$key"
			jArray.add(jArrayObject)
		}
	}

	def addJsonArraysToJsonObject(jArraysKeys, jArrayMap, jObject) {
		jArraysKeys.each {key ->
			def jArray = jArrayMap."$key".build()
			jObject.add("$key", jArray)
		}
	}

	def addJsonToTestCase(jString, contextVal, tCase) {
		if(contextVal.contains("Input")){
			tCase.inputData = jString 
		}else if(contextVal.contains("Result")){
			tCase.expectedResults = jString
		}
	}
	
	class TestCase{
		String ID
		String requestName
		String inputData;
		String expectedResults;

		public TestCase(ID, requestName) {
			this.ID = ID
			this.requestName = requestName
		}
	}
}

context.setProperty("ExcelParser", new ExcelParser(log, context))