# SoapUI-REST-Sample
A project structure for data-driven testing of REST APIs using SoapUI is provided.
Sample REST APIs with mock data have been used from https://jsonplaceholder.typicode.com/.
For test purposes the remote schema has been loaded.
Take a look at https://github.com/typicode/json-server for installing and running this json server. 


### Requirements and Setup
* Install SoapUI 5.3+
* Add external some external jars to the 'bin/ext' folder of the SoapUI installation directory
    * org.apache.poi
        * poi  
        * poi-ooxml
        * poi-ooxml-schemas
    * org.glassfish
        * javax.json
    * javax.json
        * javax.json-api
    * org.json 
        * json 
* Import **Sample-REST-Project-soapui-project.xml** from SoapUI
### Run SoapUI Testcases
* If the json server is running run **MainExecutor.groovy** inside **InitializerSuite**
