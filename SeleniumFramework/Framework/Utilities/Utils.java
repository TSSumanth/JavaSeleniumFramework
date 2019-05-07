package Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	private static File f;
	public static String errorlog;
	public static StringBuilder log=new StringBuilder();
	public static String ResultsFolderLocation;
	public static Workbook runManagerWorkbook;
	public static Workbook dataSheetWorkbook;
	public static Sheet runManagerMainSheet;
	public static ArrayList<String> scenarioNames;
	public static ArrayList<String> testCaseNames;
	public static ArrayList<String> keywords;
	public static Map<String,String> keywordswithdetails;
	public static String currentKeyword;

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
			obj.getTestCaseNames(scenarioNames.get(i));
			System.out.println(testCaseNames);
			obj.invokeDataSheet(scenarioNames.get(i));
			for(String testcasename:testCaseNames)
			{
				obj.getKeywords(testcasename);
				System.out.println(keywords);
				System.out.println(keywordswithdetails);
				obj.fetchAndInvokeKeyWord(keywords);
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
		if(value == null)
		{
			log.append("Global Setting "+ key +" is not available in the properties file");
		}
		return value;
	}
	
	//Get Current date and time 
	public void createResultsFolder()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date date = new Date();
		String output = dateFormat.format(date).toString();
		ResultsFolderLocation =  getGlobalSetting("ResultsLocattion") +"//"+output;
		f= new File(ResultsFolderLocation);
		if(f.mkdir())
		{
			log.append("Results Folder Created");
		}
		else
		{
			log.append("Results Folder not Created");
		}
		System.out.println(output);
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
		
	}
	
	/*
	 * Get test case names for a given scenario
	 */
	protected static void getTestCaseNames(String ScenarioName)
	{
	  //ScenarioName==> if column2(Execute).row(1 to n) is          yes then get column1(TestCaseName)
	 // update ==> testCaseNames
		File f = new File(projectDirectory+"//RunManagers//Runmanager.xlsx");
		FileInputStream fis;
		Sheet sheet = null;
		try {
			fis = new FileInputStream(f);
			XSSFWorkbook workbook = new XSSFWorkbook (fis);
			sheet = workbook.getSheet(ScenarioName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int lastRow=sheet.getLastRowNum();
		testCaseNames = new ArrayList<String>();
		for(int rowNum=0;rowNum<=lastRow;rowNum++)
		{
			if(sheet.getRow(rowNum).getCell(1).getStringCellValue().equalsIgnoreCase("Yes"))
			{
				String str = sheet.getRow(rowNum).getCell(0).getStringCellValue();
				testCaseNames.add(str);
			}
		}
	}

	

	/*
	 *invoke a data sheet for a given scenario 
	 */
	protected static void invokeDataSheet(String ScenarioName) 
	{
	 File f = new File(projectDirectory+"//DataTables//"+ScenarioName+".xlsx");	
	  FileInputStream fis;
		try
		{
			fis = new FileInputStream(f);
			dataSheetWorkbook= new XSSFWorkbook(fis);
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
	protected static void getKeywords(String TestCaseName)
	{
	   Sheet businessFlowSheet = dataSheetWorkbook.getSheet("BusinessFlow");
	   int lastRow=businessFlowSheet.getLastRowNum();
	   keywords = new ArrayList<String>();
	   keywordswithdetails = new HashMap<String,String>();
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
					keywordswithdetails.put(keyworddetails,str);
					keywords.add(str);
				}
				break;
			}
		} 
	}
	
	/*
	 * Fetch and invoke the respective keywords from business components folder
	 */
	
	protected static void fetchAndInvokeKeyWord(ArrayList<String> keywords)   
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
