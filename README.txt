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

These instructions assume that the 1.6 version of the java
command is in your path.  If it isn't, then you should either
specify the complete path to the java command or update your
PATH environment variable.

SQLeoVQB.2012.07Beta02.zip: 2012-09-17

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2012.07Beta/
	10  	Content window should fetch only first rows  
	38 	Content/Preview: remove left scrolling bar
	63 	Preferences: defaults modification
	64 	Content: exporting modified field from grid is wrong
	66 	use csvjdbc version 1.0-11 (bundled with SQLeo)
		see https://sourceforge.net/p/sqleo/discussion/general/thread/325d5cd1/
	68 	Command editor: Increase column display size for DATES and TIMESTAMPS types.


SQLeoVQB.2012.07Beta01.zip: 2012-08-03

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2012.07Beta/
	7 	After saving / restoring from xlq tables locations have change (permuttated)
	21 	Save joins from query builder to CSV
	32 	Command Editor: ask to save on exit
	41 	Propose Content from query designer rigth click
	56 	"Undo CTRL-Z/ Redo CTRL-Y" text on command editor/ syntax editor 


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

