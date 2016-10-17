package com.sqleo.querybuilder.syntax;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.sqleo.environment.io.FileStreamSQL;
import com.sqleo.querybuilder.QueryModel;

public class SQLParserTest extends AbstractSQLeoTest{

	private static final int TOTAL_TEST_CASES = 4 ;
	
	@Override
	protected String getTestResourcesFolder() {
		return "sqlparser";
	}

	@Test
	public void testParserTestCases() throws ClassNotFoundException, IOException{

		for(int i = 1 ; i <= TOTAL_TEST_CASES ; i++){

			final String inputFile = "testcase"+i+".sql";
			final String expectedFile = "testcase"+i+"-expected.sql";
			
			final String inputFileName = getFileName(inputFile);
			final String expectedFileName = getFileName(expectedFile);

			System.out.println("\n--------------- "+inputFile+" --------------- ");
			testParserWithExpectedResult(inputFileName, expectedFileName);
		}
	}
	
	private void testParserWithExpectedResult(final String inputFileName, final String expectedFileName) throws ClassNotFoundException, IOException{

		//QueryBuilder.useAlwaysQuote = true;
		//QueryBuilder.identifierQuoteString = new String("\"");
		
		final QueryModel qm = FileStreamSQL.read(inputFileName);
		final String actual = qm.toString(true);
		
		// FileStreamSQL.writeSQL(expectedFileName+".gen", actual);
		System.out.println(actual);
		
		final String expected = FileStreamSQL.readSQL(expectedFileName);
		
		Assert.assertEquals(expected, actual);
		
	}

}


