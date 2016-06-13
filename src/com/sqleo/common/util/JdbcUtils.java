package com.sqleo.common.util;

import java.sql.SQLException;
import java.sql.Statement;

import com.sqleo.environment.Application;

public class JdbcUtils {
	
	public static void cancel(final Statement stmt){
		if(null == stmt)
			return;
		final Thread cancelThread = new Thread(new Runnable() {
			@Override
			public void run() {
				cancelInternal(stmt);
			}
		});
		cancelThread.start();
	}
	
	private static void cancelInternal(final Statement stmt){
		if(stmt!=null){
			try {
				stmt.cancel();
			} catch (final SQLException e) {
				Application.println(e, false);
			}
		}
	}

}
