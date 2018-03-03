<?php
/**
 * Remote JDBC Driver to local-database gateway.
 * This script is designed to communicate with a remote desktop application.
 * Its purpose is to handle database access requests from the remote JDBC portion of this Driver.
 *
 * Allows the use of a http or https connection to access databases remotely
 * whilst still maintaining localhost restrictions on the database daemon.
 *
 * Login credentials are required to access this API.
 * Due to a common apache2 config setting (AllowOverride None), all security is
 * now handled by this API.
 * Additional credentials are required to access a database.
 *
 * Released under the GNU General Public License
 *
 * @package jdbwc.includes
 * @author Tim Gall
 * @version 2007-12-17 18:58 (+10 GMT)
 * @version 2008-06-30 12:36 (+10 GMT)
 * @version 2010-04-03 11:36 (+10 GMT)
 * @version 2010-04-17 02:22 (+10 GMT)
 * @copyright (C) 2008 Oz-DevWorX (Tim Gall)
 */

//incase the server ignores .htaccess directives
if(!defined('WC_INDEX')){
  header('Location: index.php');
  exit();
}

//show all errors. They need to show up on the JDBC side
error_reporting(E_ALL);
ini_set('display_errors',1);
ini_set('display_startup_errors',1);

//avoid some XSS issues
ini_set('register_globals', 0);
ini_set('allow_url_fopen', 0);
ini_set('allow_url_include', 0);
ini_set('session.use_trans_sid', 0);
ini_set('session.cache_limiter', 'nocache');

//avoid timeouts and memory overflows
set_time_limit(5400);
////ATM respect php.ini settings
//ini_set('post_max_size', '128M');
//ini_set('memory_limit', '128M');


//load configs
if (file_exists('includes/config/local/configure.php')) {
  require ('includes/config/local/configure.php'); // testing mode configuration data
} else {
  require ('includes/config/configure.php'); // production mode configuration data
}

//setup the session
if(WC_STORE_SESSIONS=='sqlite'){
  ini_set('session.save_handler', 'sqlite');
  session_save_path(WC_FILE_SESSIONS.'/phpsess.db');
}else{
  ini_set('session.save_handler', WC_STORE_SESSIONS);
  session_save_path(WC_FILE_SESSIONS);
}

// page compression (!!!BEFORE ANY OUTPUT!!!)
require_once (WC_PRI_INC . 'wc_accelerator.php');
require_once (WC_PRI_INC . 'classes/LogWriter.php');//events & errors
require_once (WC_PRI_INC . 'wc_static_vars.php');
require_once (WC_PRI_INC . 'classes/WcApiCore.php');

register_shutdown_function('session_write_close');
// session life seems buggy. Ive noticed comments in the php5 docs (user notes) about this as well.
// its not needed as the server will let the JDBC driver know what its session is anyway.
//session_set_cookie_params($WC_SESS_LIFE, WC_WSITE_DIR, getenv('HTTP_HOST'), false, true);
session_name(WC_SESSION);
if(session_start()){
  if(!isset($_SESSION['user'])){
    //archive the logfile if its getting too big
    if(filesize(WC_LOG) > 10000000){
      LogWriter::archive();
    }
    LogWriter::write(session_name() . '=' . session_id() . ' | Handler=' . WC_STORE_SESSIONS, 'SESSION-STARTED');
    $_SESSION['DEBUG_MODE'] = (isset($_POST[WC_DEBUG_TAG]) && $_POST[WC_DEBUG_TAG]=='true') ? true:false;
    if($_SESSION['DEBUG_MODE']=='true') $wc_startTime = microtime(true);
  }
}


//catchalls
set_exception_handler(array('WcApiCore', 'wcLogExceptions'));
set_error_handler(array('WcApiCore', 'wcErrorHandler'), E_ALL);

//fix timezone warnings
date_default_timezone_set(( (WC_TZ=='') ? getenv('date.timezone') : WC_TZ ));

//avoid GLOBALS and HTTP_SESSION_VARS tricks
if ( isset($_POST['GLOBALS'])
|| isset($_FILES['GLOBALS'])
|| isset($_GET['GLOBALS'])
|| isset($_COOKIE['GLOBALS'])
|| (isset($_SESSION) && !is_array($_SESSION))
) {
  WcApiCore::wcCustomHandler(WC_ERROR_HACK, "Hacking attempt using GLOBALS or \$HTTP_SESSION_VARS");
}

//finally: deprecated PHP>=5.3, removed PHP>=6
if(( function_exists("get_magic_quotes_gpc") && get_magic_quotes_gpc() )
  || ( ini_get('magic_quotes_sybase') && ( strtolower(ini_get('magic_quotes_sybase')) != "off" ) )
){
  WcApiCore::stripslashes_deep($_POST);//only using post vars
}


/* Primary PHP authentication.
 * This eliminates the need for apache authentication.
 */
if(!isset($_SESSION['user']) || sizeof($_SESSION['user'])<4 || !WcApiCore::verifyUser()){
  if(isset($_POST['su']) && isset($_POST['sp']) && preg_match('/[a-z0-9]+/ui', $_POST['su'].$_POST['sp']) && WcApiCore::verifyLogin($_POST['su'], $_POST['sp'])){
    LogWriter::write('Access has been granted for this user. Connection uses '.((getenv('HTTPS')=='on') ? 'SSL':'NONSSL'), 'ACCESS-GRANTED');
  }else{
    //wcCustomHandler calls destroy sessions, close db (if open) and exit the script.
    WcApiCore::wcCustomHandler(WC_ERROR_CREDS, 'Invalid LOGIN Credentials or No Credentials supplied. Check your username and password.');
  }
}

/* Secondary PHP authentication */
if(isset($_POST['auth']) && preg_match('/[a-z0-9]+/ui', $_POST['auth'])){
  $safe_auth = $_POST['auth'];
  $authState = false;
  require (WC_PRI_INC . WC_DATABASES);

  $requested_database = WcApiCore::wcFindDataBase($safe_auth, $database_array);
}else{
  WcApiCore::wcCustomHandler(WC_ERROR_HACK, 'SESSION REFUSED - The session was refused because the credentials string is empty or uses non valid characters.');
}

if ($requested_database != null && sizeof($requested_database) == 3) {
  define('WC_DB', $requested_database['wc_database']);
  define('WC_DB_USERNAME', $requested_database['wc_user']);
  define('WC_DB_PASSWORD', $requested_database['wc_password']);
  $authState = true;

  /* select the correct db class and start a connection */
  require_once (WC_PRI_INC . 'wc_db_core.php');


  if (!isset($_SESSION[WC_AUTH])) {
    $_SESSION[WC_AUTH] = $safe_auth;

    /* LIMIT SESSIONS. Checks once per new session. */
    if (WC_STORE_SESSIONS=='sqlite' && $WC_SESS_LIMIT > 0) {

      if($sessdb = sqlite_open(WC_FILE_SESSIONS.'/phpsess.db', 0666, $sqliterror)){
        $sessquery = "select count(sess_id) as total, min(updated) as wait_for from session_data where updated < " . (time()-$WC_SESS_LIFE);
        $sessres = sqlite_query($sessdb, $sessquery, SQLITE_BOTH, $sqliterror) or $dbHandler->throw_error(WC_ERROR_DB_QRY, 'SQLITE SESSION ERROR - ' . $sqliterror, 0, $sessquery);
        $sessdata = sqlite_fetch_array($sessres);

        $sess_cnt = (int)$sessdata['total'];
        $sess_wait = (int)$sessdata['wait_for'];
        sqlite_close($sessdb);
      }else{
        $dbHandler->throw_error(WC_ERROR_DB_CON, 'SQLITE SESSION ERROR - ' . $sqliterror);
      }

      if ($sess_cnt > $WC_SESS_LIMIT) {
        WcApiCore::wcCustomHandler(WC_ERROR_SESS, "TOO MANY SESSIONS - Theres already $sess_cnt active Connection" . (($sess_cnt > 1) ? "s " : " ") . "in progress\n\tTry again in " . date('H:i:s', $sess_wait) . ' (hours:minutes:seconds)');
      }
    }


    if($_SESSION['DEBUG_MODE']=='true'){
      $_SESSION['workStart'] = $wc_startTime;
      $_SESSION['memUse'] = array();
    }

    switch ($dbType) {
      case 2: // type 2 is PostgreSQL.
        echo $dbHandler->exec_results("SELECT textcat(version(), " . "'|PHP-" . phpversion() . "|JDBWC " . WC_VERSION . "|".date_default_timezone_get()."|".getenv('SERVER_PROTOCOL')."') AS SERVER_VERSIONS;");
        LogWriter::write('This user has PostgreSQL as the db type.', 'DB-TYPE');
        break;

      case 0:
      case 1: // types "0 & 1 & default" are MySQL".
      default:
        echo $dbHandler->exec_results("SELECT CONCAT('MySQL-', VERSION(), " . "'|PHP-" . phpversion() . "|JDBWC " . WC_VERSION . "|".date_default_timezone_get()."|".getenv('SERVER_PROTOCOL')."') AS SERVER_VERSIONS;");
        LogWriter::write('This user has MySQL as the db type.', 'DB-TYPE');
        break;
    }
  }

}else{
  WcApiCore::wcCustomHandler(WC_ERROR_CREDS, "Invalid DATABASE Credentials or No Credentials supplied. Check your database details.");
}

//all API actions are contained in the following file
require_once(WC_PRI_INC . 'wc_api_actions.php');
WcApiCore::wcCleanUp();
?>