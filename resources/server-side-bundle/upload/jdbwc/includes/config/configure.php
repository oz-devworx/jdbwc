<?php
/**
 * JDBWC Gateway configuration information.
 *
 * Released under the GNU General Public License
 *
 * @package jdbwc.includes.config
 * @author Tim Gall
 * @version 2007-12-12 10:42 (+10 GMT)
 * @version 2008-06-30 12:07 (+10 GMT)
 * @version 2010-03-29 00:58 (+10 GMT)
 * @copyright (C) 2008 Oz-DevWorX (Tim Gall)
 */

//incase the server ignores .htaccess directives
if(!defined('WC_INDEX')){
  header('Location: index.php');
  exit();
}

define('WC_USER', 'xxxxxxxxxxxxxx');
define('WC_PASS', 'xxxxxxxxxxxxxx');

define('WC_WSITE_DIR', '/testing/'); // absolute public web path (without domain part) with trailing /
define('WC_STORE_SESSIONS', 'files'); //'files' or 'sqlite' (make sure the one you use is a registered save handler in phpinfo())

/* This affects JDBC date handling.
* To set your timezone use an entry from the following chart:
* http://php.net/manual/en/timezones.php
* To use the servers timezone (assuming its set in php.ini)
* change the value to an empty string. EG: define('WC_TZ', '');
*/
define('WC_TZ', 'Australia/Brisbane');


/////////// THE REST SHOULDN'T REQUIRE ANY CHANGES /////////////

// these 2 can be safely ignored unless you change the default directory locations
define('WC_LOG_PATH', realpath('./includes/wc_logs') . '/');// absolute file path with trailing /
define('WC_FILE_SESSIONS', realpath('./includes/sessions')); // absolute file path without trailing /

$WC_SESS_LIMIT=0; //(int)max sessions. 0 to remove limits
$WC_SESS_LIFE=14400;//(int)in seconds



/* these shouldn't need changing */
define('WC_PRI_INC', 'includes/'); // jdbwc includes folder
define('WC_DATABASES', 'config/databases.php'); // db config info
define('WC_LOG', WC_LOG_PATH.'event.log');
define('WC_LOG_ARCHIVE', WC_LOG_PATH.'archive.event.log');
?>