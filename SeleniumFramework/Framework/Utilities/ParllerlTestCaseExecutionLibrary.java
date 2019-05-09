/**
 * 
 */
package Utilities;

/**
 * @author SaiSumanthReddyT
 *
 */
public class ParllerlTestCaseExecutionLibrary extends Utils implements Runnable
{
	/*
	 * Run Method
	 */
	Utils utils;
	public ParllerlTestCaseExecutionLibrary(Utils utils) {
		this.utils=utils;
	}
	public void run()
	{
			for(String testcasename:utils.testCaseNames)
			{
				utils.getKeywords(utils,testcasename);
				System.out.println(utils.keywords);
				System.out.println(utils.keywordswithdetails);
				utils.fetchAndInvokeKeyWord(utils.keywords);
			}
			
			for(String testcasename:utils.testCaseNames)
			{
				
			}
	}
}
