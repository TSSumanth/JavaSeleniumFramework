/**
 * 
 */
package Utilities;

/**
 * @author SaiSumanthReddyT
 *
 */
public class SerialExecutionLibrary extends Utils implements Runnable
{
	/*
	 * Run Method
	 */
	public void run()
	{
		Utils utils = new Utils();
		for(int i=0;i<scenarioNames.size();i++)
		{
			utils.getTestCaseNames(utils,scenarioNames.get(i));
			System.out.println(utils.testCaseNames);
			utils.invokeDataSheet(utils,scenarioNames.get(i));
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
