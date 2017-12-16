import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.*
import org.apache.poi.ss.util.*

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
		def requestName = sheet.getRow(3).getCell(2).getStringCellValue()
		def tCase = new TestCase(tCaseID, requestName)

		for(int colIndex = 2; colIndex < colCount; colIndex++) {
			def currTCaseID = sheet.getRow(0).getCell(colIndex).getStringCellValue()
			if(!currTCaseID.isEmpty() && currTCaseID != tCase.ID){
				tCases.add(tCase) 
				requestName = sheet.getRow(3).getCell(colIndex).getStringCellValue()
				tCase = new TestCase(currTCaseID, requestName)
			}
			for(int rowIndex = 6; rowIndex < rowCount; rowIndex++) {
				Row row = sheet.getRow(rowIndex)
				Cell cJsonType = row.getCell(1)
				Cell cValue = row.getCell(colIndex)

				if(cJsonType != null && cValue != null) {
					cValue.setCellType(Cell.CELL_TYPE_STRING)
					//log.info(cJsonType.getStringCellValue())
					//log.info(cValue.getStringCellValue())						
				}
			}

			if(colIndex == colCount - 1) {
				 tCases.add(tCase) 
			}
		}
	
		return tCases                           
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