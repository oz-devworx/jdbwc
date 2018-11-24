**********************************************************************
* Id: README.txt
* Subject: JDBWC 1.0.0.2_2-beta
* Author: Tim Gall (Oz-DevWorX)
* Date: 2008-07-09 15:32 (+10 GMT)
* Revised: 2008-09-08 00:34 by Tim Gall (Corrections & Requirements)
* Revised: 2008-10-17 17:51 by Tim Gall (Requirements)
* Revised: 2008-10-30 16:02 by Tim Gall (Installation & Use)
* Simple-Description: Java JDBC Driver
* Copyright (C) 2008 Tim Gall (Oz-DevWorX)
* JDBWC is Released under the GNU General Public License
* ********************************************************************
* This file is part of JDBWC.
*
* You should have received a copy of the GNU General Public License
* along with JDBWC.  If not, see <http://www.gnu.org/licenses/>.
**********************************************************************

==============================================
CONTENTS:
1) Intro to this document
2) System Requirements
3) Installation
4) Basic Use
5) Limitations
6) TODO
7) BUGS
8) TIPS

==============================================
==============================================
1) Intro to this document:
----------------------------------------------
Code examples in this doc will be easier 
to read with wordwrap turned off.

Throughout this document there are some descriptive
terms that are used frequently. Thier meanings
are clarified below.

Client Side: 
	the Java application that uses the JDBC
	part of the JDBWC Driver.

Server-Side: 
	the web-server hosting the database.

Upstream: 
	data going from the Client-side to the Server-side.

Downstream: 
	data going from the Server-side to the Client-side

==============================================
==============================================
2) System Requirements:
----------------------------------------------
Client-side: 
	Java JRE >= 1.6.0_10 (Sun Microsystems Inc. or equivalent)

Server-side: 
	Apache-web-server >= 2 
	PHP >= 5.x.x with MySQL >= 5.x.x and/or PostgreSQL >= 7.4.x

If your using MySQL you should enable the 
PHP mysqli extension for full metadata functionality.
The existence of the mysqli extension is determined by the JDBWC Driver.

==============================================
==============================================
3) Installation:
----------------------------------------------
Client-side:
------------
a) Unpack the compressed download.
b) Copy the lib folder "jdbwc"
   to a location relative to your Java application files.
c) add "jdbwc.jar" to your applications classpath.
   * If your app is an executable jar, you would usually
     include the file in your jar manifest's class-path
     using a relative path
     EG: Class-Path: jdbwc.jar
   * If your app is not a packaged jar, you will need to organise
     updating your classpath via some other standard mechanism.
   
Notes: 
	The required "jdbwc" support files will be added to your 
	classpath by the jdbwc driver.
	You only need to Class-Path "jdbwc.jar"
	
	If you want to use the DataHandler package, read the documents
	in its META-INF folder for details.


Server-side:
------------
Once the Server-side portion is installed you can forget about it.
It only interacts with the JDBWC's JDBC Driver portion.

The web compressed file named "web.zip" you recieved with 
the JDBWC download needs to end up on your web-server in a
decompressed state.
Theres a number of ways you could do this.
1) If you can decompress files on your server, do the following,
   otherwise swap step 'a' with step 'b':
   
   a) Upload the (compressed) file/s to a public location on your web-server.
   b) Decompress the file.
   c) Set apache folder permissions on the folder named "jdbwc"
      that you just unpacked. Record the username and password
      you use for use in your Java application.
   d) Edit the following files:

"jdbwc/includes/config/configure.php"
	Change the following entries in the configure.php file to match your desired settings.
	-------------
	define('WC_DEBUG_MODE', 'false');
	define('WC_DB_SERVER', 'localhost'); // usually localhost
	define('WC_PCONNECT', 'false'); // persistent connections? not supported by mysqli; leave as false for now.
	
	define('WC_SESSION', 'jdbwcSession'); // session key
	define('WC_STORE_SESSIONS', 'database'); // leave empty '' for file handler or set to 'database'
	define('WC_TABLE_SESSIONS', 'sessions'); // sessions table name (if using 'database')
	define('WC_FILE_SESSIONS', '/tmp'); // absolute path without trailing /
	
	$WC_SESS_LIMIT = 5; // limit active sessions. Set to 0 to remove limits.
	
	define('WC_WSITE_DIR', '/admin/'); // absolute path with trailing /
	-------------


"jdbwc/includes/config/databases.php"
	Read the comments in the databases.php file and change the array values to 
	match your database credentials. Examples are included.
	You will also need the credential details for the Java application side 
	so you should keep a record of them.


The "jdbwc/includes/config/" folder contains a sub folder named local.
The local folder contains a near identical set of configure.php and databases.php files
for developers using this driver, to assist them when testing with 
a local or mirror or thier live webserver. 

!IMPORTANT:
You should delete the "jdbwc/includes/config/local" folder on production servers.
 

==============================================
==============================================
4) Basic Use:
----------------------------------------------
JDBWC is a JDBC Driver, so the standard JDBC syntax applies for general use.

To start a JDBC connection using JDBWC 
you need to register the driver with the JRE's DriverManager
and then start a new JDBC connection using a JDBWC specific URL.
The required url sytax and parameters are demonstrated in the example below.
You can also pass in a Properties file containing the paramaeters.

Parameters (everything after the ?) can be in any order.
Registering the driver requires a ClassNotFoundException catch,
starting a new Connection requires an SQLException catch.
See the example below.

Connection Propetrty keys:
    ==========================
    * url               (website url EG: https://somedomain.com/admin/ 
                         [In this case, the "admin" folder 
                         at https://somedomain.com
                         should contain the 'web' folder "jdbwc"])
	* port              (server port number. https is usually 443 
	                     and plain-text is usually 80)
	* user              (apache username for 'web' folder "jdbwc")
	* password          (apache password for 'web' folder "jdbwc")
	* databaseName      (Database name)
	* databaseUser      (Database username)
	* databasePassword  (Database password)
	
----------- BEGIN EXAMPLE -------------
Connection connection;
try {

	/* REGISTER the JDBWC Driver with the Java Runtime Engine */
	Class.forName("com.jdbwc.core.Driver");
	
	final String wcDataBase = "somedb_name";
	final String wcUser = "someDbUserName";
	final String wcPass = "someDbPassword";
				
	final int port = 443;
	final String httUrlStr = "https://somedomain.com/subfolder";//subfolder contains web 'jdbwc' folder
	final String user = "Apache 'jdbwc/' User";
	final String pass = "Apache 'jdbwc/' Password";
	
	/* Valid Vendor IDs:
	 * -----------------
	 * USE DEFAULT DB : jdbc:jdbwc://
	 * USE MYSQL DB   : jdbc:jdbwc:mysql//
	 * USE POSTGRESQL : jdbc:jdbwc:postgresql//
	 * -----------------
	 * MySql is the default type.
	 *
	 * If you want debugging ON (its OFF by default), 
	 * include the string debug=true as a parameter.
	 * EG: "&db_password=" + wcPass + "&debug=true"
	 */
	final String jdbwcUrlStr = "jdbc:jdbwc:mysql//" 
		+ httUrlStr 
		+ "?port=" + port 
		+ "&databaseName=" + wcDataBase
		+ "&databaseUser=" + wcUser
		+ "&databasePassword=" + wcPass;
	
	/* INITIALISE a new JDBC Connection using the JDBWC Driver package */
	connection = DriverManager.getConnection(jdbwcUrlStr, user, pass);
	
	
//    /* OR using a Propeties object. Both ways work. */
//    Properties props = new Properties();
//    props.put("url", httUrlStr);
//    props.put("port", String.valueOf(port));
//    props.put("user", user);
//    props.put("pass", pass);
//    props.put("databaseName", wcDataBase);
//    props.put("databaseUser", wcUser);
//    props.put("databasePassword", wcPass);
//    	
//    connection = DriverManager.getConnection(myDriverUrl, props);
	
	
} catch (ClassNotFoundException e) {
	e.printStackTrace();
} catch (SQLException e) {
	e.printStackTrace();
} catch (Exception e) {
	e.printStackTrace();
}
------------ END EXAMPLE --------------


==============================================
==============================================
5) Limitations:
----------------------------------------------
a) No support for newer JDBC 4 data-types yet.
b) Has small data transfer size limits compared to MySQL-Connector/J 
   and the PostgreSQL-JDBC drivers EG: MB compared to GB and TB

==============================================
==============================================
6) TODO:
----------------------------------------------
a) Production testing (in progress until January 2009).
b) Finish PreparedStatement, CallableStatement and ParameterMetaData.
c) Implement support for the streamable data types.
d) Implement support for some of the newer JDBC 4 data types.
e) Recheck ResultSet date, time and timestamp handling with both db types.

==============================================
==============================================
7) BUGS:
----------------------------------------------
I would guess theres a few logical errors but none that
seem to affect basic functionality (that im aware of).
If you notice any, please report them in the 
sourceforge bug-tracking area for the JDBWC project at:
https://sourceforge.net/projects/jdbwc

==============================================
==============================================
8) TIPS:
----------------------------------------------
JDBWC is easy to implement and has been found to
perform at respectable speeds under load. 
* To fully leverage JDBWC you should batch your queries whenever possible. 
The statements batch methods can be used or you can
use strings with ; deliming the queries in an execute or executeQuery method.
Batched select statements will return multiple ResultSets which are 
fetched from the driver in the usual manner with getResultSet and getMoreResults.
The Sun javadocs on JDBC explain these methods in detail.

* I use pagination tequniques with all potentially large or unknown 
ResultSet yeilding queries to keep memory footprints low 
and application responsiveness high.
EG: fetching records in batches. 0 to 100; 100 to 200 ... 999900 to 1000000, etc.
Pagination provides you with built in scalabilty.

Pagination also allows the webserver to serve more clients simultaneously
without the risk of resource hogging. 

==============================================
END OF README