This file uses unix style line breaks. In windows, use wordpad or an IDE/editor to read it.

What's what?
============
Don't rename any folders or files or it might break the Ant script.


1) In your IDE, set the Java source folders. They begin with "src-".
   The PHP sources are in resources/server-side-bundle/upload,

2) The dependencies are in the dependencies folder. Add them to your editors class path (as a user library).
   They also get used by the Ant build so don't move or rename the folder or files.

3) Use the Ant build script to automate the build process, then look for the folder DISTRIBUTION (created during the build process).


src-jdbwc        Contains the Driver source files
src-dataHandler  Contains the DataHandler source files (required by the Driver)
src-jdbwctest    Contains test sources (for testing the Driver)

dependencies     Contains the JDBWC dependencies for this release.
resources        Contains the server-side PHP bundle

build.xml        Ant build file. Builds distribution set with javadocs to a folder named DISTRIBUTION in the same directory as this file.

README_DEV.txt   This file.


For details on driver installation see Install_xxxx.html in the resources folder.
  (After running the build script the Install doc will also be in the distribution dir.)
  
For basic details on using the DataHandler (other than in the driver) see its META-INF/README.

OTHER STUFF:
============
/*
   The following data was collected from MySQL Connector/J.
   I couldn't find much info on XOpen SQL error codes, 
   this seemed to be the most complete list.
   The XOpen CLI spec doesn't cover error codes.
   Once I find XOpen error spec doc it will be included with the source files
   or implemented as an error code class.
   
   All credit goes to Mark Mathews for the original data (below).
 */


S0002 - SQL_STATE_BASE_TABLE_NOT_FOUND
S0001 - SQL_STATE_BASE_TABLE_OR_VIEW_ALREADY_EXISTS
42S02 - SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND
S0021 - SQL_STATE_COLUMN_ALREADY_EXISTS
S0022 - SQL_STATE_COLUMN_NOT_FOUND
08S01 - SQL_STATE_COMMUNICATION_LINK_FAILURE
08007 - SQL_STATE_CONNECTION_FAIL_DURING_TX
08002 - SQL_STATE_CONNECTION_IN_USE
08003 - SQL_STATE_CONNECTION_NOT_OPEN
08004 - SQL_STATE_CONNECTION_REJECTED
01004 - SQL_STATE_DATE_TRUNCATED
22008 - SQL_STATE_DATETIME_FIELD_OVERFLOW
41000 - SQL_STATE_DEADLOCK
01002 - SQL_STATE_DISCONNECT_ERROR
22012 - SQL_STATE_DIVISION_BY_ZERO
S1C00 - SQL_STATE_DRIVER_NOT_CAPABLE
01S01 - SQL_STATE_ERROR_IN_ROW
S1000 - SQL_STATE_GENERAL_ERROR
S1009 - SQL_STATE_ILLEGAL_ARGUMENT
S0011 - SQL_STATE_INDEX_ALREADY_EXISTS
S0012 - SQL_STATE_INDEX_NOT_FOUND
21S01 - SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST
28000 - SQL_STATE_INVALID_AUTH_SPEC
22018 - SQL_STATE_INVALID_CHARACTER_VALUE_FOR_CAST
S1002 - SQL_STATE_INVALID_COLUMN_NUMBER
01S00 - SQL_STATE_INVALID_CONNECTION_ATTRIBUTE
S1001 - SQL_STATE_MEMORY_ALLOCATION_FAILURE
01S04 - SQL_STATE_MORE_THAN_ONE_ROW_UPDATED_OR_DELETED
S0023 - SQL_STATE_NO_DEFAULT_FOR_COLUMN
01S03 - SQL_STATE_NO_ROWS_UPDATED_OR_DELETED
22003 - SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE
01006 - SQL_STATE_PRIVILEGE_NOT_REVOKED
42000 - SQL_STATE_SYNTAX_ERROR
S1T00 - SQL_STATE_TIMEOUT_EXPIRED
08007 - SQL_STATE_TRANSACTION_RESOLUTION_UNKNOWN
08001 - SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE
07001 - SQL_STATE_WRONG_NO_OF_PARAMETERS
2D000 - SQL_STATE_INVALID_TRANSACTION_TERMINATION
