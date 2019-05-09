package Utilities;

import java.util.Set;

public class ExecutionManager {

	public static void main(String[] args)
	{
		System.out.println("Current thread Name"+ Thread.currentThread());
		Utils utils = new Utils();
		utils.readGlobalConfigurationSettingsPropertiesFile("location");
		utils.createResultsFolder();
		utils.createExecutiontextFile();
		utils.invoke_RunManager();
		utils.getScenarioNames();
		utils.getScenarioDescriptions();
		utils.getScenarioExecutionType();
		
		if(utils.getGlobalSetting("PrallelExecution").toUpperCase().equals("FALSE"))
		{
			SerialExecutionLibrary serialthread = new SerialExecutionLibrary();
			serialthread.createResultsFolder();
			Thread serial = new Thread(serialthread);
			serial.start();
			try {
				serial.join();
			} catch (InterruptedException e) {
				Utils.logExecutionMessage("Exeception occured while waiting for serial execution thread to complete \n"+e.getMessage());
			}
			System.out.println("Serial Thread Executed");
		}
		else {
			Thread[] parllel = new Thread[20]; 
			for(int i=0;i<Utils.scenarioNames.size();i++)
			{
				Utils parllelutils = new Utils();
				utils.getTestCaseNames(parllelutils,Utils.scenarioNames.get(i));
				System.out.println(parllelutils.testCaseNames);
				utils.invokeDataSheet(parllelutils,Utils.scenarioNames.get(i));
				ParllerlExecutionLibrary parllelthread = new ParllerlExecutionLibrary(parllelutils,Utils.scenarioNames.get(i));
				parllelthread.createResultsFolder();
				parllel[i] = new Thread(parllelthread);
				parllel[i].start();
				System.out.println("Parllel Thread Executed");
			}
			
			/*Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
			System.out.println(threadSet);
			for (Thread thread : threadSet) {
			    try {
			    	System.out.println(thread);
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}*/
		}
	}
}
