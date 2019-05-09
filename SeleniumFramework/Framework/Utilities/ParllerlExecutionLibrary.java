/**
 * 
 */
package Utilities;

/**
 * @author SaiSumanthReddyT
 *
 */
public class ParllerlExecutionLibrary extends Utils implements Runnable
{
	/*
	 * Run Method
	 */
	Utils utils;
	String scenarioname;
	public ParllerlExecutionLibrary(Utils utils,String ScenarioName) {
		this.utils=utils;
		this.scenarioname=ScenarioName;
	}
	public void run()
	{
			String executiontype = Utils.scenarioExecutionType.get(scenarioname);
			if(executiontype == null)
			{
				for(String testcasename:utils.testCaseNames)
				{
					utils.getKeywords(utils,testcasename);
					System.out.println(utils.keywords);
					System.out.println(utils.keywordswithdetails);
					utils.fetchAndInvokeKeyWord(utils.keywords);
				}
			}
			else if(executiontype.toUpperCase().equals("PARLLEL"))
			{
				Thread[] parllelTestcase=new Thread[utils.testCaseNames.size()];
				for(int i=0;i<utils.testCaseNames.size();i++) {
					ParllerlTestCaseExecutionLibrary parllelthread = new ParllerlTestCaseExecutionLibrary(utils);
					parllelthread.createResultsFolder();
					parllelTestcase[i] = new Thread(parllelthread);
					parllelTestcase[i].start();
				}
			}
			else {
				for(String testcasename:utils.testCaseNames)
				{
					utils.getKeywords(utils,testcasename);
					System.out.println(utils.keywords);
					System.out.println(utils.keywordswithdetails);
					utils.fetchAndInvokeKeyWord(utils.keywords);
				}
			}
			
			
	}
}
