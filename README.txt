To Build with eclipse :
1. Checkout SQLeo source
2. Create a new project SQLeoVQBuilder in eclipse 
3. Import sources
4. Configure build path - Libraries - Add External JARS - Select path to your installed jdk...Java\jr6\lib\rt.jar
5. Build project
6. Export as jar and deselect svn files and select Main class (Application.java)

To run SQLeo Visual Query Builder, launch:

  java -jar SQLeoVQB.jar

These instructions assume that the 1.6 version of the java
command is in your path.  If it isn't, then you should either
specify the complete path to the java command or update your
PATH environment variable.

SQLeoVQB.2012.01Beta02.zip: 2012-03-06

  For changes included see tickets at  
     http://sourceforge.net/p/sqleo/tickets/milestone/2012.01Beta/

  Join definition in a csv file is available now:
	
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

    - SQLeo needs to be restarted to take FKdefinition.csv changes into account.

  For any remark, thanks to post at
    http://sourceforge.net/p/sqleo/discussion/general/thread/40383554/


SQLeoVQB.2012.01Beta01.zip: 2012-02-04

  Join definition in a csv file (partly) available.


SQLeoVQB.2012.01Beta00.zip: 2012-01-27

  Development is not finished, but the application is usable for most parts.
  Join definition in a csv file is not available today.

