package com.sqleo.querybuilder.syntax;

import java.net.URL;

import org.junit.Before;
import org.junit.Ignore;

import com.sqleo.environment.Preferences;

@Ignore
public abstract class AbstractSQLeoTest {
	
	protected String getFileName(final String fileName){
		final URL url = this.getClass().getResource("/"+getTestResourcesFolder()+"/"+fileName);
		return url.getFile();
	}
	
	abstract String getTestResourcesFolder();
	
	@Before
	public void init(){
		Preferences.loadDefaults();
	}

}
