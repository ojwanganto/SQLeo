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

or launch

  sqleo-start.bat 
  sqleo-start.sh
  sqleo-start.command

With the expected LookAndFeel 
(Warning Metal is set by default because it is the only one that has been fully tested)


These instructions assume that the 1.6 version of the java
command is in your path.  If it isn't, then you should either
specify the complete path to the java command or update your
PATH environment variable.


SQLeoVQB.2013.02.rc02.zip: 2013-03-08

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2013.02/
	96 	Content window: refresh (F5) when all rows are fetched returns blank screen
	103 	Improve SQLeo to check for new version available on server
		and Add donate shortcut
	104 	Query Builder: Fails to save .sql on Mac OS X
			LookAndFeel forced by default to Metal for all OS


SQLeoVQB.2013.02.rc01.zip: 2013-02-22

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
		Not finished yet (joins with Drived tables are not drawned in grap)

	83 Query builder: reverse SQL, remove -- comments 
	84 Query builder still broken after reconnect 
	85 Query Builder: check for PK when saving join to file
	88 Propose Definition from query designer rigth click
	92 Query Builder: reverse SQL syntax for table ALIAS in joins is case sensitiv
	93 Query Builder: reverse SQL for WHERE (NOT) EXISTS to be added 
	100 Content Window: "sort by" doesn't override initial order by from syntax
	102 Query builder: reverse SQL, support OVER( PARTITION ... ORDER BY ...) 



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

