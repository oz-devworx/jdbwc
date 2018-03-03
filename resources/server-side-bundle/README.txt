This file uses unix style line breaks. In windows, use wordpad or an IDE/editor to read it.

To setup the server-side part of this driver.
=============================================
NOTE: The folder: "jdbwc/includes/config/local" is for testing servers and is not needed
for servers with a registered domain name. Its supplied for convenience when developing.


1) Upload the contents of the upload folder to your webserver. 
They can be uploaded to the public root dir or any subfolder.
EG: http://yourserver.ext/jdbwc, http://yourserver.ext/subfolder/anotherfolder/jdbwc

2) Edit the file: "jdbwc/includes/config/configure.php"
Put in a username and password (create new ones).
Edit the paths and settings to suit your preferences.

3) Edit the file: "jdbwc/includes/config/databases.php"
Put in the details for the databases you want access to.
If theres more than one, follow the directions in the comment to add multiple databases.
The database user must have full permissions (excluding grant perms).

4) You will need to give the .log files in: "jdbwc/includes/wc_logs" full write permissions.
You will also need to give the folder: "jdbwc/includes/sessions" full write permissions and
the "phpsess.db" (sqlite database) write permissions if you chose sqlite as the session handler (in the configure.php file).

Thats about it.
See the bundled jdbwctest.zip file in the distribution folder for details on how to setup your java class/s
to use the jdbwc.jar (JDBC driver). Look in the connect() method for some examples.


What are the sql files for?
===========================

They are for running the bundled java test class.
To use them:

1) Create a database (server-side) and give it a user. 
Record the details for later (database-name, user-name, password).
The database user must have full permissions (excluding grant perms).

2) insert the sql into your new database (the file names indicate what DB type they are for).

3) Add the jdbwc.jar to your java classpath. Theres a MANIFEST.MF file in the JDBWC test folder.
If your running the CoreTest file from within an IDE, add the jdbwc-lib jars to your build path.

4) Update the login details in the CoreTest.connect() method, set any optional parameters you wish to use, compile and run the file.
