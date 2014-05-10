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

or rename sqleo-start.txt to:

  sqleo-start.bat 
  sqleo-start.sh
  sqleo-start.command

With the expected LookAndFeel 
(Warning Metal is set by default because it is the only one that has been fully tested)


These instructions assume that the 1.6 version of the java
command is in your path.  If it isn't, then you should either
specify the complete path to the java command or update your
PATH environment variable.


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

