To Build with ant on windows :
1. Checkout SQLeo source
2. launch
	set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_30
	set ANT_HOME=D:\apache-ant-1.8.3
	set PATH=%PATH%;%ANT_HOME%\bin
	ant dist

To Build with eclipse :
1. Checkout SQLeo source and add an empty lib folder on project root path.
2. In Eclipe goto File->New->Other->Java Project from existing ant build file 
   and select the ant build file.
3. To run the application: Run com.sqleo.environment.Application.java
4. To export as JAR : Select project -> export as jar (deselect svn files)
   and then select Main class (com.sqleo.environment.Application.java)

To run SQLeo Visual Query Builder, launch:

  java -jar SQLeoVQB.jar

or rename sqleo-start-xxx.yyy to:

  sqleo-start-xxx.bat 
  sqleo-start-xxx.sh
  sqleo-start-xxx.command

With the expected LookAndFeel 
(Warning Metal is set by default because it is the only one that has been fully tested)


These instructions assume that the 1.6 version of the java
command is in your path.  If it isn't, then you should either
specify the complete path to the java command or update your
PATH environment variable.

SQLeoVQB.2017.09.rc1.zip: 2017-09-17

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2017.09/
	
	#403 	Update pivottable.js to version 2.14.0
	#402 	Update csvjdbc driver to 1.0.32


SQLeoVQB.2017.03.rc1.zip: 2017-03-21

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2017.03/
	
	#401 	SQLeoPivotDiff
	#389 	Designer: filter with OR condition is changed in AND
	#388 	Designer: enable "remove" on right click on table
	#377 	Designer: reverse query with ON condition with values is wrong
	#325 	create Aliases for SQL queries
			see sql/aliases.sql 


SQLeoVQB.2016.12.rc1.zip: 2016-12-30

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2016.12/

	#330 Content windows: java.lang.NumberFormatException: For input string "0.1"


SQLeoVQB.2016.11.rc1.zip: 2016-11-29

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2016.11/

	#396 Embeded MySQL driver update from MariaDB 1.1.6 to 1.4.6
	#394 Designer: reversing query doesn't warn on closed connection
	#393 Designer: reversing query never ends
	#387 Java : version 1.7 by default
	#381 Add Cancel query for "export to HTML Pivot"
	#356 Query builder: support for "NOT column"
	#219 Refactor Beginner User guide


SQLeoVQB.2016.10.rc1.zip: 2016-10-23

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2016.10/

	#386 command editor: current connexion should be emptyed after disconnect
	#384 automated SQLparser testing
	#382 SQL History: add a Copy an open in editor
	#379 Designer: removing a table should remove all related columns
	#351 Content Window: SQLexception: Statement was canceled or the session timed out
	#349 Content Window: add AutoSavepoint in Populate filter with values
	#231 SQL history: format SQL from query designer before to save
	#77  Query builder: reverse "where a.x >= b.y" is wrong


SQLeoVQB.2016.09.rc1.zip: 2016-09-28

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2016.09/

	#380 Export Pivot: set fetchsize to 1000
	#331 Content window: jump doesn't work
	#120 Query Builder: changing table alias is not applyed to condition ...
	#xxx Updated Translations for French, Serbian (by Dejan Zdravkovic), Spanish (Miguel Angel Gil Rios)


SQLeoVQB.2016.08.rc1.zip: 2016-08-28

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2016.08/

	#378 New russian translation (by Vladimir Solovyov)
	#376 Add Cancel query for "count records" and "export"
	#300 Better translation for Content Window
	#228 Designer: reverse query returns nothing


SQLeoVQB.2016.07.rc1.zip: 2016-07-11

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2016.07/

	#375 Content Window: Prevent error "OutOfMemoryError: Java heap space" when opening a big MySQL table


SQLeoVQB.2016.05.rc1.zip: 2016-06-28

  Thanks Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2016.05/

	374 Main Icon bar: not refreshed when closing windows
	371 Data comparer: "Only different values" is wrong with NULL values on H2
	367 org.postgresql.util.PSQLException: ERROR: schema "dbms_output" does not exist
	11 Cancel query doesn’t work


SQLeoVQB.2016.04.rc1.zip: 2016-04-19

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2016.04/

	370 Preferences: values > 999 were ignored
	368 Data comparer: empty error message box displayed
	364 Metadata explorer: search is Upper case, ko with lower case object names
	358 Command editor: support dbms_output with PostgreSQL /​ orafce
	357 Command editor: /​* text */​ in front of query prevent result from being displayed
	297 Improved German translation (by Philipp Blaszczyk)


SQLeoVQB.2016.03.rc1.zip: 2016-03-02

  Thanks Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2016.03/

	352 Data comparer: set background color according to connection
	350 Content Window: add Column Alias suppport in Populate filter with values
	348,353 Data comparer: allow million lines comparison
	121 Menu: all windows are not reachable using right and left arrows


SQLeoVQB.2016.02.rc1.zip: 2016-02-13

  Thanks to Eder Jorge and Anudeep Gade for their contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2016.02/

	321,340 All windows: background color per datasource set
	341 Background color in soft grey for READ ONLY connections
	318 Content Window: filters prepopulated with data values
	338 SQLeo Start up GUI does nothing
	339 Preferences: auto-commit set to off by default
	344 Content window: count records fails on query with ORDER BY (monetDB)
	327,342	command editor, data comparer: set fetch size to 1000 for performances


SQLeoVQB.2016.01.rc1.zip: 2016-01-19

  Thanks to Eder Jorge for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2016.01/

	337 Auto-Savepoint: activate/​disable in preferences
	333 command editor: Commit/​Rollback buttons KO out of initial connection
	326 create READ ONLY connections


SQLeoVQB.2015.12.rc1.zip: 2016-01-02

  Thanks to Eder Jorge for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2015.12/

	336 Better MAC OS integration (by Eder Jorge)
		use sqleo-start-MacOS.txt to start SQLeo on OSX

	335 New portuguese brasilian translation (by Eder Jorge)
	334 Content Window: java.lang.ArrayIndexOutOfBoundsException with Update or delete
	329 Query builder /​ Command editor: avoid PostgreSQL ERROR: current transaction is aborted
		All Statements use Auto SavePoints 
		rem: Also works with MonetDB

	328 Command editor: support for PostgreSQL Anonymous block DO $$
	327 Data Comparer: performance problem with Oracle


SQLeoVQB.2015.10.rc1.zip: 2015-10-31

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2015.10/

	320 data comparer: use H2 as working datasource for big datasets
	319 data comparer: display aggregate text in resulting columns
	317 command editor: set output default format
	316 command editor: let user choose output file extension
	315 command editor: set a message in text log when data sent to grid
	277 SQLeo display available shortcuts
	255 Data comparer: save working datasource

SQLeoVQB.2015.09.rc1.zip: 2015-09-17

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2015.09/

	314 command editor: limit rows is broken
	312 command editor: permit to switch between TEXT Window an GRID
	311 command editor: add CLEAR command
	306 command editor: add INPUT <filename.sql> command
	309 data comparer: check that connexions are set before to start
	282 Query Builder: add Warning message when trying to run many SQL
	313 Grid content /​ output: resultset is not closed at end of query
	278 copy and open in editor: choose file extension in preferences 
	183 copy and open in editor: line feed are lost 


SQLeoVQB.2015.08.rc1.zip: 2015-08-25

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2015.08/

	310 new portuguese translation (by Bruce Hyatt)
	307 Improved Italian translation (by Sinhuè Angelo Rossi)
	286 command editor: support for OUTPUT and choose DATASOURCE in script


SQLeoVQB.2015.07.rc1.zip: 2015-08-07

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2015.07/

	302 data comparer broken with Oracle
	301 test with (automatic) translation in Arabic, Chinese, Hindi, Russian
	297 Improved German translation (by Philipp Blaszczyk)
	294 Print Version name in link when new version found
	217 Give possibility to translate Metadata Explorer (translation available in French only)


SQLeoVQB.2015.06.rc1.zip: 2015-06-27

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2015.06/

	293 Query builder: adding csvjdbc table without schema
	292 Preferences: selected langage in combo is only modifieable by user
	291 Command script to use SQLeo as a portable app
	290 Input boxes, labels, panels width/​height are not resized with increased Font size
	289 Resize icons in metadata tree and query builder tree
	227 set FONT_SIZE_PERCENTAGE default to 110 and ICON_SIZE_PERCENTAGE to 160
	173 query builder where filter should be synchronized after use of grid filter


SQLeoVQB.2015.05.rc1.zip: 2015-05-27

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2015.05/

	288 Better translation for Data Comparer
	287 SQL history: npe when trying to delete a record
	285 Command Editor: support for SHOW syntax with MySQL
	281 Improve display on High resolution monitors
		Note that dialog box need to be manually resized

	268 Command Editor: multiple inserts returns only one status line
	172 query Builder: add "sync columns with select" for group by 


SQLeoVQB.2015.04.rc1.zip: 2015-04-17

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2015.04/

	283 Content window: deleted row should be colored in RED before being applyed to db
	280 Serbian translation (by DEJAN ZDRAVKOVIĆ)
	279 Query Builder: Add (database independent) group_concat syntax
	275 command editor : format sql selected
	274 sqleo query result shows oracle blob objects as null
	270 Transform complex UPDATE in SELECT (for testing)
	269 Query Builder: Add (database independent) pivot syntax
	267 Content window: Update in grid with 2 varchar fields as update criterias fails
	262 command editor: display latest connection in connections list (if set in preferences)
	237 command editor: CTRL ENTER should select previous or current statement not all (Fix)
	72 Query Builder: reverse "case when ..." is wrong (Fix)
	46 Content window: can't update or delete on NULL column
	27 refresh of screen CONTENT/​PREVIEW after action
	19 message: no update criteria defined! when inserting


SQLeoVQB.2015.03.rc1.zip: 2015-03-27

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2015.03/

	264 Command Editor: keep user resized windows (after execution)
	260 Data comparer: add diff status for each line
		minimun csvjdbc-1.0-23.jar driver version (found in /lib directory) is needed
	256 Data comparer: add user alias for source and target
	242 command Editor: remember column sizes in datagrid after refres
	241 command Editor: F5 for refreshing datagrid content
	237 command Editor: CTRL ENTER should select previous or current statement not all
		all statements should be separated with semicolons ;
	234 SQLeo should not parse sql when loading sql file at first time


SQLeoVQB.2015.02.rc1.zip: 2015-02-20

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2015.02/

	262 Command Editor - store gridoutput preference, display latest connection in connections list
	258 Sending query result to pivottable.html for OLAP purpose  
	257 Command Editor/​ Data comparer: display current opened file name in title


SQLeoVQB.2015.01.rc1.zip: 2015-01-25

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2015.01/


	253 SQL history: data is sorted by day name not by timestamp
	251 Command editor: Display Query duration (in text mode)
	249 Data comparer
		- save configuration in xml format,
		- load configaration in xml format,
		- limit Source and Target queries to 100 rows in Public edition

	247 File recent queries: choose to open file in Query Builder OR Command Editor OR Data comparer


SQLeoVQB.2014.12.rc1.zip: 2014-12-07

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2014.12/

	250 Firebird: show content fails with GDS Exception 335544569
		Jdbc driver needs to be re-created for users of previous releases

	244 command editor: trim blanks before SELECT to go in datagrid
	243 command editor: not always showing bottom of text area
	239 compare table data (pivot table)


SQLeoVQB.2014.11.rc1a.zip: 2014-11-22

	For low resolution screens

	227 reverted FONT_SIZE_PERCENTAGE default from 110 to 100
	225 reverted ICON_SIZE_PERCENTAGE default from 160 to 100
	

SQLeoVQB.2014.11.rc1.zip: 2014-10-25

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2014.11/

	235 command editor: display query result in data grid format
	233 Export /​ Import data should be permitted for csv files
	227 set FONT_SIZE_PERCENTAGE default to 110
	226 support for Oracle db link syntax
	225 Bigger icons for high resolution screens (and others)
	174 query builder: add export to csv in syntax window
	 91 Query Builder: add support for UNION ALL 


SQLeoVQB.2014.10.rc1.zip: 2014-10-02

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2014.10/

	224 Syntax: join type is not highlighted
	223 Designer: force background color to white for all L&F
	220 Syntax: display corresponding pairs of parenthesis in same color
	203 Query builder: wrong filter icons
	 34 SQL history view and log sql queries in a file  


SQLeoVQB.2014.09.rc1.zip: 2014-09-09

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2014.09/

	221 limit font size increase to 200
	218 MySQL ready ( with mariaDB jdbc driver included)
	214 Sign saved images
	210 Query syntax: /* comments */ should be colored in green
	 76 Query builder: save table position and pack as a comment in SQL


SQLeoVQB.2014.08.rc1.zip: 2014-08-23

  Thanks to 
	- Philipp Blaszczyk, Marcin Chojnacki, Miguel Angel Gil Rios for translations
	- Anudeep Gade for his contribution to the code of this release

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2014.08/

	195 Fix command editor fails in executing queries which are multi-line.
	 44 New Translations of the Menus/Query Builder/Preferences in
		- GERMAN by Philipp Blaszczyk
		- POLISH by Marcin Chojnacki
		- SPANISH by Miguel Angel Gil Rios


SQLeoVQB.2014.07.rc1.zip: 2014-07-15

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2014.07/

	215 New responsive web site:
		http://sqleo.sourceforge.net/index.html
	209 reverse query: syntax ON AND
	208 Designer: improve arrange spring
	206 change donation link from paypal to:
		http://sqleo.sourceforge.net/support.html
			

SQLeoVQB.2014.06.rc1.zip: 2014-05-27

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2014.06/

	206 	change donation link from paypal to a web page with all instructions
	202 	Font size percentage modification for high resolution screens
			font size should not be more than 130% yet
	201 	Query builder: adding a join between two subqueries not working
	200 	Query Builder: adding/changing Alias for Subquery not possible
	199 	Reverse query: repetitiv message "table or alias not found"


SQLeoVQB.2014.05.rc1.zip: 2014-05-09

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2014.05/

	191 Reverse query: display missing columns for where conditions
	158 Reverse query: fails with function on ANSI join field
	142 Reverse query: values in ON conditions generates error


SQLeoVQB.2014.04.rc1.zip: 2014-03-31

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2014.04/


	186,192	Query builder: support multi schemas for MySQL
		  --> try the modified MariaDB jdbc driver available in lib directory

	151,194	Query Builder: derived table syntax is not tab formated
		  --> improved subqueries formatting

	150,160	Reverse query: display missing columns for joins
		  --> graph is dislayed in red when table or column not found
		  --> permits to reverse any SQL query without being connected
			
	189 	Command editor result - Add option for find dialog to search
	144 	Reverse query: column alias without AS not recognized for functions

SQLeoVQB.2014.03.rc1.zip: 2014-03-01

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2014.03/

	187 	Sql scripts: add queries on MySQL information_schema
	184 	designer syntax: find / replace is slow and consume too much memory
	182 	Import: is wrong with UTF-8 files
	180 	Add reconnect option in metadataexplorer
	179 	change donation currency $ --> €
	167 	Data grid: open cell in text editor
	153 	query builder: error during add expression on standard table
	45 	export table data to file transform non ANSI chars to ?

	For Export/import in UTF-8 use command line
		java -Dfile.encoding=UTF-8 -jar SQLeoVQB.jar
	as described in sqleo-start.txt


SQLeoVQB.2014.02.rc1.zip: 2014-02-04

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2014.02/

	176 	editor : add line number view and line highlighter
	171 	export forget column alias in header
	169 	Add CANCEL to "Do you want to save query to a file ?" yes/no
	168 	remove .bat extensions in zip to be able to send it to gmail users
	165 	export csv: propose option for "no text" if null
	162 	export csv: enclose text by ""
	107 	Content window: column size should be preserved during 
			execute or data refresh (F5)
	28 	trim space during export doen't trim


SQLeoVQB.2014.01.rc1.zip: 2014-01-11

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2014.01/

	164 	export csv: remove current block choice
	161 	query builder: add shortcut CTRL-ENTER for execute
	152 	colored part of left or rigth joins is wrong
	146 	add button to send data result in excel
	60 	refactor data CONTENT window inside QUERY one
	59 	Add support for execute procedure and PL/SQL blocks

	Starting with this release, Query builder is limited to 3 tables max per window.
	Please Donate to get the full featured version.


SQLeoVQB.2013.12.rc1.zip: 2013-12-17

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2013.12/

	159 	Autocomplete-generate join conditions automatically by command
	155 	blanks are added in select text during reverse SQL to graph
	149 	Create a menu button to open new query builder window
	148 	Reverse query: enable offline mode
	147 	Find and replace All never ends
	143 	Designer: relations rendering linear links


SQLeoVQB.2013.11.rc1.zip: 2013-11-24

  Thanks to Anudeep Gade for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2013.11/
	139 	auto-complete: Alias and columns proposal on the fly
	137 	add find and replace option for builder syntax
	131 	Command Editor > Auto complete with multiple connections at same time
	122 	Designer: arrange spring fails with derived tables
	80 	Query builder: reverse SQL, add derived tables subqueries support


SQLeoVQB.2013.10.rc2.zip: 2013-10-24

  Bug Fixes included for:
	135 	Reverse query: fails with subqueries WITH CTE format
	130 	Auto-complete with table aliases
	90 	Reverse query: add support for EXTRACT(year from date)

  New ticket added:
	140 unmappable character for encoding UTF8 


SQLeoVQB.2013.10.rc1.zip: 2013-10-18

  Thanks to Bao Nguyen and Anudeep Gade for their contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2013.10/
	136 	add support for SQLite3
			Query Builder tested with xerial sqlite-jdbc-3.7.2.jar
			driver class:	org.sqlite.JDBC
			url format:	jdbc:sqlite:dbname.db
	135 	Reverse query: fails with subqueries WITH CTE format
	132 	Auto-complete should use choosen schema when no schema in syntax
	130 	Auto-complete with table aliases
	90 	Reverse query: add support for EXTRACT(year from date)


SQLeoVQB.2013.09.rc01.zip: 2013-09-16

  Thanks to Bao Nguyen for his contribution to this release
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2013.09/
	134 	save join to file: schema is forgotten with option "no schema in query" 
	123 	Reverse query: select (subquery) x from ... looses alias
	72 	Query Builder: reverse "case when ..." is wrong 


SQLeoVQB.2013.08.rc01.zip: 2013-08-11

  Thanks to Anudeep Gade for this new feature and fixes.
  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2013.08/

	129 	Command editor connections dropdown has duplicates when socket read timeout occurs.
	128 	Add a preference to disable asking "ask before exit" everytime.
	110 	Auto-complete(intelli-sense) SQL in editor
		  works with connected user schema objects, 
		  or db objects if db doesn't support schema (MySQL, Firebird)
		  supported syntax is (lowercase) in command editor and query builder (syntax tab)
			select table.col from table where table.col ... 


SQLeoVQB.2013.07.rc01.zip: 2013-07-22

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2013.07/

	127 Query Builder: don't take table alias to csv file when saving join 
	126 Java Web Start compatibility

  Thanks to Bao Nguyen for his contribution:

	119 Reverse query: raise warning when table not found
	117 Reverse query: where a != b is wrong
	116 Reverse query: select count(distinct x) is wrong
	115 replace class com/sun/image/codec/jpeg/* for java7 support on Linux


SQLeoVQB.2013.06.rc01.zip: 2013-06-05

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2013.06/
	118 Designer: arrange spring has wrong effect

	Note: Saving to XLQ format will be desupported soon, and be replaced by:
	76 Query builder: keep table position, sort and pack information as a comment in SQL 

SQLeoVQB.2013.05.rc01.zip: 2013-05-25

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2013.05/
	43 Improve Entity-relation Graph display 
		In graph designer "arrange entities" has been renamed to "arrange grid".
		"arrange spring" added to permit entities arrangement based on a 
		spring/magnetic layout model.


SQLeoVQB.2013.04.rc01.zip: 2013-04-23

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2013.04/

	26 Reverse SQL syntax problems, support added for: 
		JOIN , LEFT JOIN, RIGHT JOIN, FULL JOIN, CROSS JOIN 
	75 Query Builder: Highlight columns with where conditions using a flag or color
	106 Query Builder: reuse same Content window on successives executions
	108 Reverse query: graph for nested derived queries is wrong
	113 Designer: display the direction of the join in graph


SQLeoVQB.2013.02.rc03.zip: 2013-03-23

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2013.02/

	55 Query Builder: Table name is not displayed in graph when table alias is used 
	67 change property file .sqleo to xml clear text
		Two files are now used for configuration:
			.sqleo.xml
			.sqleo.metaview
		Don't forget to save them on a regular basis
 
	71 Content window: add filter value show null 
	73 Query Builder: reverse SQL, add Oracle join type (+) support
		Warning message when transforming a(+) join to INNER join
 
	78 Query builder: do not auto alias fields in (derived table) subqueries 
	79 Content / preview window: add row count on content window
	80 Query builder: reverse SQL, add derived tables subqueries support
		support for CTE (Common Table expression) added to ANSI and SQL92 style
		Not finished yet (joins with Derived tables are not drawned in graph)

	83 Query builder: reverse SQL, remove -- comments 
	84 Query builder still broken after reconnect 
	85 Query Builder: check for PK when saving join to file
	88 Propose Definition from query designer rigth click
	92 Query Builder: reverse SQL syntax for table ALIAS in joins is case sensitiv
	93 Query Builder: reverse SQL for WHERE (NOT) EXISTS to be added 
	96 Content window: refresh (F5) when all rows are fetched returns blank screen
	100 Content Window: "sort by" doesn't override initial order by from syntax
	102 Query builder: reverse SQL, support OVER( PARTITION ... ORDER BY ...) 
	103 Improve SQLeo to check for new version available on server
		and Add donate shortcut

	104 Query Builder: Fails to save .sql on Mac OS X
		LookAndFeel forced by default to Metal for all OS

	103 Fix when checking for new version available on server
	105 Content window: propose "reverse syntax"
	109 Content window: display rowcount when known


SQLeoVQB.2012.07Beta04.zip: 2012-10-19

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2012.07Beta/
	7 	After saving / restoring from xlq tables locations have change (permuttated)
	10  	Content window should fetch only first rows  
	21 	Save joins from query builder to CSV
	30  	Oracle: performance problem when adding tables to graph
			ask for ojdbc14_10.2.0.5.jar (not bundled with SQLeo)
	32 	Command Editor: ask to save on exit
	38 	Content/Preview: remove left scrolling bar
	41 	Propose Content from query designer rigth click
	54  	Query Builder: reverse "sum(c) col_alias" is wrong 
	56 	"Undo CTRL-Z/ Redo CTRL-Y" text on command editor/ syntax editor 
	63 	Preferences: defaults modification
	64 	Content: exporting modified field from grid is wrong
	66 	use csvjdbc version 1.0-11 (bundled with SQLeo)
		see https://sourceforge.net/p/sqleo/discussion/general/thread/325d5cd1/
	68 	Command editor: Increase column display size for DATES and TIMESTAMPS types.
	69 	Exception: oracle.jdbc.driver.OracleResultSetImpl.isClosed()Z
	70  	Exception: ResultSet.isAfterLast() unsupported at org.relique.jdbc.csv.CsvResultSet.isAfterLast
	74	SQLeo application confirm dialog before exit
	81  	Preview windows: NPE at com.sqleo.environment.ctrl.content.ContentView.sort
	82 	Content Window: key down going up to grid (when having next n rows to fetch)
	86 	Query Builder : fix reverse SQL for table and columns with # char
	87  	Query Builder : fix reverse SQL for "cast (x as ...) as Z"
	89  	SQLeo application GUI enhancements (see sqleo-start.bat for Look and Feel modification)


SQLeoVQB.2012.03Beta04.zip: 2012-07-18

   First release of the docs:

   - Help in pdf,docx,html format:
     http://svn.code.sf.net/p/sqleo/code/trunk/doc/howtouse/

   - javadoc from here: 
     http://svn.code.sf.net/p/sqleo/code/trunk/doc/sqleo-javadoc.zip

   - html version of the help document is availabile online from SQLeo website 
     http://sqleo.sourceforge.net/guide.html

   Adapted SQLeo app menubar > help > howtouse to 
     http://sqleo.sourceforge.net/ site.

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2012.03Beta/
	50  	NullPointerException at com.sqleo.environment.Preferences.getBoolean
	51 	Add CsvJdbc in the (default) driver list
	52 	Metadata explorer: java.sqj.SQLException: l' URL oracle indiquée n'est pas valide
	53 	Query Builder: reverse SQL group by t1.a||t2.b is wrong
	57 	Add Apache Derby Embedded in the (default) driver list
	58 	Definition > Indices runs analyze on Oracle to display Indexes
	48	query builder: Incorrect SQL generated when sign "/" or "$" is used
	49	query builder: Order of selected columns is lost when reversing functions
	12 	Group by Alias (from sqleonardo opened bugs)
	13 	reverse SQL for mixed aliased and non-aliased columns (from sqleonardo opened bugs)
	14 	Query, syntax windows execution
	22 	switch between Content windows
	31 	java.lang.StringIndexOutOfBoundsException: String index out of range: -20
	33 	Definition window: add constraints definition (workarround)
	37 	Metadata explorer: propose to add to DEFINITION windows
	39 	Raise exception when Join definition file is not found
	40 	keep least recently used queries list
	47 	query builder: group or order by expression with alias is wrong


SQLeoVQB.2012.01Beta02.zip: 2012-03-06

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2012.01Beta/
	1  	JRE 1.6  	
	2 	resizable windows
	3 	send objects from metadata explorer to query builder
	4 	explicit commit-rollback mode
	5 	added table is not displayed
	6 	removed table from graph remains in SQL text
	9 	Add firebird in the (default) driver list
	29 	Support for natural join (workarround)
	30 	Oracle: performance problem when adding tables to graph (workarround)
	8 	storing joins informations in a csv flat file


  Exemple of using Join definition in a csv file:
	
    - customize FKdefinition.csv regarding your database 
      or use create_table.sql exemple.

    - Define your Join definition file (per datatabase) if needed.
      (now available for non-windows users)

    - modify "table owner" and "ref table owner" columns 
      with the schema name used (let thoses columns empty if none).

    - "table alias" has to be used when many FK are 
      referencing the same table PK
               
    - join type to be defined in INNER(defaul), LEFT, RIGHT, FULL

    - when connected to SQLeo
      set preferences (menu --> tools --> Query Builder)
        auto join ON
        auto alias OFF
        use schema name in syntax definition ON (when database has schema)

    - Open a query (menu --> file --> new query)
	add table FACT
	add other tables using the table click 
		"open all foreign tables" or
		"open all primary tables" or
		"references..."
	select columns, where conditions, group by, order by ...
        execute 
	query can be saved to .xlq, .sql or image
	data results can be sorted, filtered, exported to .txt or .csv
	see the specific feature "jump" that permits to open in a click
	a new window with "referenced data" (using joins and filters)

    - connection to the corresponding database needs to be restarted to 
      take FKdefinition.csv changes into account.

