package Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Utils {

	private static Properties globalConfigurationSettings = new Properties();
	private static String projectDirectory = System.getProperty("user.dir");	
	public String errorlog;
	public StringBuilder executionlog = new StringBuilder();
	public static String ResultsFolderLocation;
	public static String ExecutionLogFileLocation;
	public static Workbook runManagerWorkbook;
	public Workbook dataSheetWorkbook;
	public static Sheet runManagerMainSheet;
	public static ArrayList<String> scenarioNames;
	public static Map<String,String> scenarioExecutionType;
	public ArrayList<String> testCaseNames;
	public ArrayList<String> keywords;
	public Map<String,String> keywordswithdetails;
	public String currentKeyword;
	public String testScenarioFolderLocation;
	public String testcaseFolderLocation;

	public static Map<String,String> scenarioDetails;
	public static void main(String[] args) {
		
		Utils obj = new Utils();
		obj.readGlobalConfigurationSettingsPropertiesFile("location");
		System.out.println(obj.getGlobalSetting("TakescreenshotsForPassedSteps"));
		obj.createResultsFolder();
		obj.invoke_RunManager();
		obj.getScenarioNames();
		System.out.println(scenarioNames);
		obj.getScenarioDescriptions();
		System.out.println(scenarioDetails);
		for(int i=0;i<scenarioNames.size();i++)
		{
			obj.getTestCaseNames(obj,scenarioNames.get(i));
			System.out.println(obj.testCaseNames);
			obj.invokeDataSheet(obj,scenarioNames.get(i));
			for(String testcasename:obj.testCaseNames)
			{
				obj.getKeywords(obj,testcasename);
				System.out.println(obj.keywords);
				System.out.println(obj.keywordswithdetails);
				obj.fetchAndInvokeKeyWord(obj.keywords);
			}
			
		}
		
	}
	
	public String getProjectDirectory()
	{
		return Utils.projectDirectory;
	}
	
	// Read and load global configuration settings properties file
	public void readGlobalConfigurationSettingsPropertiesFile(String location)
	{
		location = Utils.projectDirectory +"//GlobalConfigurationSettings.properties";
		FileInputStream fis;
		try {
			fis = new FileInputStream(location);
			globalConfigurationSettings.load(fis);
		} catch (FileNotFoundException e) {
			errorlog = errorlog + e.getMessage();
		} catch (IOException e) {
			errorlog = errorlog + e.getMessage();
		}
	}
	
	//get global configuration properties
	public String getGlobalSetting(String key)
	{
		String value = Utils.globalConfigurationSettings.getProperty(key);
		return value;
	}
	
	//Get Current date and time and create a results folder
	public void createResultsFolder()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date date = new Date();
		String output = dateFormat.format(date).toString();
		ResultsFolderLocation =  getGlobalSetting("ResultsLocattion") +"//"+output;
		File f= new File(ResultsFolderLocation);
		if(f.mkdir())
		{
			executionlog.append("Results Folder Created"+"\n");
		}
		else
		{
			executionlog.append("Results Folder not Created"+"\n");
		}
	}
	
		//Create execution text file
	public void createExecutiontextFile()
	{
			ExecutionLogFileLocation=ResultsFolderLocation+"//executionlog.txt";
			File f= new File(ExecutionLogFileLocation);
			try {
				f.createNewFile();
				executionlog.append("Execution Text File Created \n");
			} catch (IOException e) {
				executionlog.append("Execution Text File not Created : "+e.getMessage() +"\n");
			}
			FileWriter fr = null;
			try {
				// Below constructor argument decides whether to append or override
				fr = new FileWriter(f, true);
				fr.write(executionlog.toString());

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					fr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	}
	
	public static void logExecutionMessage(String message)
	{
		File f= new File(ExecutionLogFileLocation);
		FileWriter fr = null;
		try {
			// Below constructor argument decides whether to append or override
			fr = new FileWriter(f, true);
			fr.write(message+ "\n");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
		
		
	
	/*
	 * to invoke runmanager sheet
	 */
	protected void invoke_RunManager()
	{
		  File f = new File(projectDirectory+"//RunManagers//Runmanager.xlsx");
		  try
		  {
			FileInputStream fis = new FileInputStream(f);
			runManagerWorkbook = new XSSFWorkbook(fis);
			runManagerMainSheet = runManagerWorkbook.getSheet("MainSheet");
			logExecutionMessage("Run Manager Sheet Invoked");
			
		  } 
		  catch (FileNotFoundException e) 
		  {
			e.printStackTrace();
		  }
		  catch (IOException e) 
		  {
			e.printStackTrace();
		
		  }
	}
	
	/*
	 * to get scenario names from main sheet in Run manager excel file
	 */
	protected void getScenarioNames()
	{
		int lastRow=runManagerMainSheet.getLastRowNum();
		scenarioNames = new ArrayList<String>();
		for(int rowNum=0;rowNum<=lastRow;rowNum++)
		{
			if(runManagerMainSheet.getRow(rowNum).getCell(1).getStringCellValue().toUpperCase().equals("YES"))
			{
				String str = runManagerMainSheet.getRow(rowNum).getCell(0).getStringCellValue();
				scenarioNames.add(str);
			}
		}
		logExecutionMessage("List of Scenarios that will be execcuted: \n");
		logExecutionMessage(scenarioNames.toString());
	}
	
	/*
	 * to get scenario descriptions from main sheet in Run manager excel file
	 */
	protected void getScenarioDescriptions()
	{
		int lastRow=runManagerMainSheet.getLastRowNum();
		scenarioDetails = new HashMap<String,String>();
		for(String scenarioname:scenarioNames)
		{
			for(int rowNum=0;rowNum<=lastRow;rowNum++)
			{
				if(runManagerMainSheet.getRow(rowNum).getCell(0).getStringCellValue().equalsIgnoreCase(scenarioname))
				{
					XSSFRow row=(XSSFRow) runManagerMainSheet.getRow(0);
					int lastCol=row.getLastCellNum();
					for(int colNum=1;colNum<lastCol;colNum++ )
					{
						if(runManagerMainSheet.getRow(0).getCell(colNum).getStringCellValue().equalsIgnoreCase("Description"))
						{
							String value = runManagerMainSheet.getRow(rowNum).getCell(colNum).getStringCellValue();
							scenarioDetails.put(scenarioname, value);
						}
							
					}
				}
			}
		}
		logExecutionMessage("List of scenarios with Description ");
		logExecutionMessage(scenarioDetails.toString());
	}
	
	/*
	 * to get scenario descriptions from main sheet in Run manager excel file
	 */
	protected void getScenarioExecutionType()
	{
		int lastRow=runManagerMainSheet.getLastRowNum();
		scenarioExecutionType = new HashMap<String,String>();
		for(String scenarioname:scenarioNames)
		{
			for(int rowNum=0;rowNum<=lastRow;rowNum++)
			{
				if(runManagerMainSheet.getRow(rowNum).getCell(0).getStringCellValue().equalsIgnoreCase(scenarioname))
				{
					XSSFRow row=(XSSFRow) runManagerMainSheet.getRow(0);
					int lastCol=row.getLastCellNum();
					for(int colNum=1;colNum<lastCol;colNum++ )
					{
						if(runManagerMainSheet.getRow(0).getCell(colNum).getStringCellValue().equalsIgnoreCase("ExecutionType"))
						{
							String value = runManagerMainSheet.getRow(rowNum).getCell(colNum).getStringCellValue();
							scenarioExecutionType.put(scenarioname, value);
						}
							
					}
				}
			}
		}
		logExecutionMessage("List of scenarios with Execution Types ");
		logExecutionMessage(scenarioExecutionType.toString());
	}
	
	//Get Current date and time 
		public void createTestScenarioFolder(Utils utils,String scenarioName)
		{
			utils.testScenarioFolderLocation = ResultsFolderLocation + scenarioName;
			File f= new File(utils.testScenarioFolderLocation);
			if(f.mkdir())
			{
				logExecutionMessage("Scenario Folder Created");
			}
			else
			{
				logExecutionMessage("Scenario Folder not Created");
			}
		}
		
		//Get Current date and time 
		public void createTestCaseFolder(Utils utils,String scenarioName)
				{
					utils.testcaseFolderLocation = ResultsFolderLocation + scenarioName;
					File f= new File(utils.testcaseFolderLocation);
					if(f.mkdir())
					{
						logExecutionMessage("Test Case Folder Created");
					}
					else
					{
						logExecutionMessage("Test Case Folder not Created");
					}
				}
	
	/*
	 * Get test case names for a given scenario
	 */
	protected void getTestCaseNames(Utils utils,String ScenarioName)
	{
	  	File f = new File(projectDirectory+"//RunManagers//Runmanager.xlsx");
		FileInputStream fis;
		Sheet sheet = null;
		try {
			fis = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook (fis);
			sheet = workbook.getSheet(ScenarioName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int lastRow=sheet.getLastRowNum();
		utils.testCaseNames = new ArrayList<String>();
		for(int rowNum=0;rowNum<=lastRow;rowNum++)
		{
			if(sheet.getRow(rowNum).getCell(1).getStringCellValue().equalsIgnoreCase("Yes"))
			{
				String str = sheet.getRow(rowNum).getCell(0).getStringCellValue();
				utils.testCaseNames.add(str);
			}
		}
	}

	

	/*
	 *invoke a data sheet for a given scenario 
	 */
	protected void invokeDataSheet(Utils utils,String ScenarioName) 
	{
	 File f = new File(projectDirectory+"//DataTables//"+ScenarioName+".xlsx");	
	  FileInputStream fis;
		try
		{
			fis = new FileInputStream(f);
			utils.dataSheetWorkbook= new XSSFWorkbook(fis);
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	

	/*
	 *to get keywords from a test case 
	 */
	protected void getKeywords(Utils utils,String TestCaseName)
	{
	   Sheet businessFlowSheet = utils.dataSheetWorkbook.getSheet("BusinessFlow");
	   int lastRow=businessFlowSheet.getLastRowNum();
	   utils.keywords = new ArrayList<String>();
	   utils.keywordswithdetails = new HashMap<String,String>();
		for(int rowNum=0;rowNum<=lastRow;rowNum++)
		{
			if(businessFlowSheet.getRow(rowNum).getCell(0).getStringCellValue().equalsIgnoreCase(TestCaseName))
			{
				XSSFRow row=(XSSFRow) businessFlowSheet.getRow(rowNum);
				int lastCol=row.getLastCellNum();
				for(int colNum=1;colNum<lastCol;colNum++ )
				{
					String keyworddetails= businessFlowSheet.getRow(0).getCell(colNum).getStringCellValue();
					String str = businessFlowSheet.getRow(rowNum).getCell(colNum).getStringCellValue();
					utils.keywordswithdetails.put(keyworddetails,str);
					utils.keywords.add(str);
				}
				break;
			}
		} 
	}
	
	/*
	 * Fetch and invoke the respective keywords from business components folder
	 */
	
	protected void fetchAndInvokeKeyWord(ArrayList<String> keywords)   
	{
		boolean flag=false;
		
		//Target BusinessComponents folder and get the list of files present on BusinessComponents folder
		File f = new File(projectDirectory+ "//BusinessComponents//FunctionalLibrary");
		String[] filelist = f.list();
		
		//in an arraylist variable add all .classfiles
		ArrayList<String> classfiles = new ArrayList<String>();
		for(String files:filelist)
		{
			if(files.contains(".java"))
			{
				classfiles.add(files);
			}
		}
		
		//Create an object class array with the same size of arraylist
		Object[] supcomobj = new Object[classfiles.size()];
	
		//Create object for each business component class file
		int objlen=0;
		Iterator itr = classfiles.iterator();
			while(itr.hasNext())
			{
				String file =(String) itr.next();
				String ClassName = "FunctionalLibrary."+file.split("\\.")[0]; 
				Class<?> buscomClass = null;
				try 
				{
					buscomClass = Class.forName(ClassName);
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
				// convert string classname to class
				try 
				{
					supcomobj[objlen] = buscomClass.newInstance();
				} 
				catch (InstantiationException | IllegalAccessException e) 
				{
					e.printStackTrace();
				}
				objlen++;
			}
        
        //for each method in to invoke 
        for(String method: keywords)
	        {
        		//searching in each class object
	        	for(int i=0; i<supcomobj.length;i++)
		        	{
		        		Method[] methods = supcomobj[i].getClass().getDeclaredMethods();
		        			//searching every method in class
		        			for(Method methodsinbuscom:methods)
				        		{
				        			String metname = methodsinbuscom.getName();
				        			if(metname.equals(method))
				        			{
				        				Method setNameMethod = null;
										try 
										{
											setNameMethod = supcomobj[i].getClass().getMethod(metname);
										} 
										catch (NoSuchMethodException | SecurityException e)
										{
											e.printStackTrace();
										}
											try 
											{
												setNameMethod.invoke(supcomobj[i]);
											} 
											catch (Exception e) 
											{
												e.printStackTrace();
											}
											catch (Error e) 
											{
												e.printStackTrace();
											}			                    	
				                    	flag=true;
				                    	break;
				        			}
				        		}
		        		if(flag==true)
			        		{
			        			flag=false;
			        			break;
			        		}
		        		//for last class object if method was not found the return error message
		        		if(i==(supcomobj.length-1))
		        			{
		        			  System.out.println("Method not found");
		        			}
		        	}
	        }
	}

}
