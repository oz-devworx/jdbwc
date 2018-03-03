<?php
/**
 * Static function collection that provides much of the core jdbWC functionality.
 *
 * Released under the GNU General Public License
 *
 * @package jdbwc.includes.classes
 * @author Tim Gall
 * @version 2008-02-06 18:31 (+10 GMT)
 * @version 2008-05-30 14:19 (+10 GMT)
 * @copyright (C) 2008 Oz-DevWorX (Tim Gall)
 */

//incase the server ignores .htaccess directives
if(!defined('WC_INDEX')){
  header('Location: index.php');
  exit();
}

final class WcApiCore{

  private function __construct(){
    //disallow class construction. consumable methods are static
  }

  /**
   * Catchall exception logging.
   * Exceptions are logged & relayed back to the JDBC side.
   * The connection is also zapped.
   *
   * @param object $e - An Exception instance
   */
  static function wcLogExceptions(Exception $e){
    LogWriter::write($e->getMessage() .' in ' . $e->getFile() . ' on line ' . $e->getLine(), WC_EX_STR . $e->getCode());
    echo WC_ERROR . WC_EX_STR . $e->getCode() . ' - ' . $e->getMessage() .' in ' . $e->getFile() . ' on line ' . $e->getLine();
    WcApiCore::wcKillConnection();
  }

  /**
   * Catchall PHP errors.
   * Errors are logged & relayed back to the JDBC side.
   * The connection is also zapped for E_USER_ERROR. (??probably should be zapped for all??)
   *
   * @param int $errno Error code
   * @param string $errstr Error message
   * @param string $errfile [optional]
   * @param mixed $errline [optional]
   */
  static function wcErrorHandler($errno, $errstr, $errfile, $errline){
    switch ($errno) {
      case E_USER_ERROR:
        $message = WC_ERROR . "$errstr\n\tFatal error on line $errline in file $errfile\nAborting...";
        LogWriter::write($message, 'PHP E_USER_ERROR: '.$errno);
        echo "$message\n";
        WcApiCore::wcKillConnection();
        break;

      case E_USER_WARNING:
        $message = WC_ERROR . $errstr;
        LogWriter::write($message, 'PHP E_USER_WARNING: '.$errno);
        echo "$message\n";
        break;

      case E_USER_NOTICE:
        $message = WC_ERROR . $errstr;
        LogWriter::write($message, 'PHP E_USER_NOTICE: '.$errno);
        echo "$message\n";
        break;

      case E_NOTICE:
        $message = WC_ERROR . $errstr;
        LogWriter::write($message, 'PHP E_NOTICE: '.$errno);
        //dont display E_NOTICE in errors, just log it for debugging
        break;


      default:
        $message = WC_ERROR . $errstr;
        LogWriter::write($message, 'PHP Unknown error: '.$errno);
        echo "$message\n";
        break;
    }

    /* Don't execute PHP internal error handler */
    return true;
  }

  /**
   * Custom error logging.
   * Errors are logged & relayed back to the JDBC side.
   * The connection is also zapped.
   *
   * @param int $errno Error code
   * @param string $errstr Error message
   * @param int $db_error_code [optional] For database error codes
   */
  static function wcCustomHandler($errno, $errstr, $db_error_code=0){
    switch ($errno) {
      case WC_ERROR_HACK:
        $message = WC_ERROR . WC_ERROR_HACK_STR.$errno . ' - '.$errstr;
        LogWriter::write(WC_ERROR . $errstr, WC_ERROR_HACK_STR.$errno);
        break;

      case WC_ERROR_CREDS:
        $message = WC_ERROR . WC_ERROR_CREDS_STR.$errno . ' - '.$errstr;
        LogWriter::write(WC_ERROR . $errstr, WC_ERROR_CREDS_STR.$errno);
        break;

      case WC_ERROR_PATH:
        $message = WC_ERROR . WC_ERROR_PATH_STR.$errno . ' - '.$errstr;
        LogWriter::write(WC_ERROR . $errstr, WC_ERROR_PATH_STR.$errno);
        break;

      case WC_ERROR_DB_CON:
        $message = WC_ERROR . WC_ERROR_DB_CON_STR.$db_error_code . ' - '.$errstr;
        LogWriter::write(WC_ERROR . $errstr, WC_ERROR_DB_CON_STR.$db_error_code);
        break;

      case WC_ERROR_DB_QRY:
        $message = WC_ERROR . WC_ERROR_DB_QRY_STR.$db_error_code . ' - '.$errstr;
        LogWriter::write(WC_ERROR . $errstr, WC_ERROR_DB_QRY_STR.$db_error_code);
        break;

      case WC_ERROR_SESS:
        $message = WC_ERROR . WC_ERROR_SESS_STR.$errno . ' - '.$errstr;
        LogWriter::write(WC_ERROR . $errstr, WC_ERROR_SESS_STR.$errno);
        break;


      default:
        $message = WC_ERROR . WC_ERROR_UNKNOWN_STR.$errno . ' - '.$errstr;
        LogWriter::write(WC_ERROR . $errstr, WC_ERROR_UNKNOWN_STR.$errno);
        break;
    }
    //close sessions & bail out. Avoids retaining old session data
    echo("$message\n");//relay the error back to the JDBC side
    WcApiCore::wcKillConnection();
  }

  /**
   *
   * @param string $userName - value should be verified first
   * @param string $password - value should be verified first
   * @return boolean. true on success.
   */
  static function verifyLogin($userName, $password){
    if(!isset($_SESSION['user'])){
      $_SESSION['user']['jdbcUserName'] = $userName;
      $_SESSION['user']['jdbcPassword'] = $password;

      $_SESSION['user']['wcUserName'] = self::secureHash(WC_USER, $userName);
      $_SESSION['user']['wcPassword'] = self::secureHash(WC_PASS, $password);
    }
//        LogWriter::write('JDBCUser:'.$_SESSION['user']['jdbcUserName'].' | JDBCPass:'.$_SESSION['user']['jdbcPassword'], 'DEBUG-INFO');
//        LogWriter::write('WCUser  :'.$_SESSION['user']['wcUserName'].' | WCPass  :'.$_SESSION['user']['wcPassword'], 'DEBUG-INFO');
//        LogWriter::write(var_export($_POST, true), 'DEBUG-INFO');

    return self::verifyUser();
  }

  /**
   * Verify the username and password is authentic.
   */
  static function verifyUser(){
    if(isset($_SESSION['user']) && sizeof($_SESSION['user'])==4){
      if(strcmp($_SESSION['user']['jdbcUserName'].$_SESSION['user']['jdbcPassword'], $_SESSION['user']['wcUserName'].$_SESSION['user']['wcPassword'])==0){
        return true;
      }
    }
    return false;
  }

  /**
   *
   * @param string $jdbcCreds - value should be verified first
   * @param string $wcCreds
   * @return boolean. true on success.
   */
  static function verifyDBCreds($jdbcCreds, $wcCreds){

    if(!isset($_SESSION['dbCreds'])){
      $_SESSION['dbCreds']['jdbcCreds'] = $jdbcCreds;
      $_SESSION['dbCreds']['wcCreds'] = self::secureHash($wcCreds, $jdbcCreds);
    }

    if (strcmp($_SESSION['dbCreds']['wcCreds'], $_SESSION['dbCreds']['jdbcCreds'])==0) {
      return true;
    }

    return false;
  }

  /**
   * Generate a secure salt-based sha256 hash.
   *
   * @param $plainText
   * @param $salt
   */
  static function secureHash($plainText, $salt){
    if (strlen($salt) > WC_SALT_LENGTH){
      $salt = substr($salt, 0, WC_SALT_LENGTH);

      return $salt . hash("sha256", $salt . $plainText, FALSE);
    }
    return null;
  }

  /**
   * Terminate the current session and any handles relating to it.
   */
  static function wcKillConnection() {
    global $dbHandler;

    //log session ends
    LogWriter::write(session_name() . '=' . session_id(), 'SESSION ENDED');
    session_destroy();

    if(isset($dbHandler)) $dbHandler->close();
    exit(0);
  }

  /**
   * Cleanup the connection.
   */
  static function wcCleanUp() {
    global $dbHandler;

    session_write_close();
    if(isset($dbHandler)) $dbHandler->close();
  }

  static function stripslashes_deep($value){
    $value = is_array($value) ?
    array_map('stripslashes_deep', $value) :
    stripslashes($value);

    return $value;
  }

  /**
   * Find the database matching the supplied hash credentials
   *
   * @param string $credentials A secure hash of the actual credentials
   * @param mixed $database_array
   * @return array
   */
  static function wcFindDataBase($jdbcCredentials, $database_array) {
    $found = null;

    foreach ($database_array as $val) {
      $wcCredentials = "";
      foreach ($val as $key2 => $val2) {
        $found[$key2] = $val2;
        $wcCredentials .= $val2;
      }
      if (self::verifyDBCreds($jdbcCredentials, $wcCredentials)) {
        break;
      } else {
        $found = null;
      }
    }
    return $found;
  }

  /**
   * rawResult is in standard .CSV format as a String.
   * This is the formatting specifications
   * for what we need to return to the requesting interface
   * as plaintext echo'd on the output page.
   *
   *    "," seperates entries
   *    "\n" seperates rows
   *    "\n\n" seperates files
   *
   *      EXISTING TEXT SHOULD BE HANDLED LIKE SO:
   *    "\n" should be replaced with "__NL_"
   *      "," should be replaced with "__CA_"
   *
   * @param mixed $sqlQuery
   * @param mixed $cvsRowCnt
   * @return An SQL query as a .csv String
   */
  static function wcBuildCSV($sqlQuery, $cvsRowCnt) {
    global $dbHandler;

    $csvString = "";
    $headersSet = false;
    $headerRow = "";
    $dataRows = "";

    $cvsRow = 0;
    $colCnt = 0;

    if ($cvsRowCnt > 0) {
      while ($sqlResults = $dbHandler->fetch_array($sqlQuery)) {
        $colIdx = 0;
        if (!$headersSet) {
          $colCnt = sizeof($sqlResults);
          foreach ($sqlResults as $key => $val) {
            $headerRow .= $key . ($colIdx < $colCnt - 1 ? "," : WC_EOL);
            $dataRows .= self::wcFinaliseVal($val) . ($colIdx < $colCnt - 1 ? "," : WC_EOL);
            $colIdx++;
          }
          $headersSet = true;
        } else {
          foreach ($sqlResults as $key => $val) {
            $dataRows .= self::wcFinaliseVal($val) . ($colIdx < $colCnt - 1 ? "," : WC_EOL);
            $colIdx++;
          }
        }
        $cvsRow++;
      }
      $csvString = $headerRow . $dataRows . WC_EOF;
    }
    $dbHandler->free_result($sqlQuery);

    return $csvString;
  }


  /**
   * == evaluates spaces and null's as true.<br />
   * === only evaluates null's as true.<br />
   * This allows us to accurately represent null's
   * and empty values on the JDBC side.
   *
   * @param mixed $value
   * @return string
   */
  private static function wcFinaliseVal($value) {
    $valRes;
    if ($value === null) {
      $valRes = 'NULL';
    } else {
      $valRes = $value;
    }
    return self::wcPrepCSVString($valRes);
  }

  /**
   * Prepare a String or Object for ouput as .CSV text.
   *
   * @param mixed $inputStr to prepare for cvs output
   * @return string
   */
  private static function wcPrepCSVString($inputStr) {
    if(""===$inputStr){
      $csvStr = WC_VD;
    }else{
      $search = array(',', "\r\n", "\r", "\n", "\f", '"', "'");
      $replace = array(WC_CA, WC_CN, WC_CR, WC_NL, WC_FF, WC_DQ, WC_SQ);

      $csvStr = str_replace($search, $replace, $inputStr);
    }

    return utf8_encode($csvStr);
  }
}
?>