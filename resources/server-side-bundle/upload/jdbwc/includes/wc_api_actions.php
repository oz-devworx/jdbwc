<?php
/**
 * This file contains the actions to perform
 * when variables are received from the
 * JDBC side of the driver.
 *
 * Released under the GNU General Public License
 *
 * @package jdbwc.includes
 * @author Tim Gall
 * @version 2008-02-06 18:31 (+10 GMT)
 * @version 2008-05-01 13:52 (+10 GMT)
 * @copyright (C) 2008 Oz-DevWorX (Tim Gall)
 */

//incase the server ignores .htaccess directives
if(!defined('WC_INDEX')){
  header('Location: index.php');
  exit();
}

/* if the users database credentials validated */
if ($authState==true) {

  /* this is for a vendor specific extension to the JDBC api */
  $getRowsAffected = false;
  if (isset($_POST[WC_GET_ROWS]) && $_POST[WC_GET_ROWS] == '1') {
    $getRowsAffected = true;
  }
  /* this is for a required JDBC api implementation */
  $getGeneratedKeys = false;
  if (isset($_POST[WC_GET_KEYS]) && $_POST[WC_GET_KEYS] == '1') {
    $getGeneratedKeys = true;
  }

  $sqlString = isset($_POST['sql']) ? $_POST['sql'] : "";

  /* Handle setting and getting data to and from database */
  if(isset($_POST[WC_ACTION])){
    switch ($_POST[WC_ACTION]) {
      /* caters for insert, update, delete execute, savepoint and release savepoint;
       * No resultset will be returned for execute, savepoint or release savepoint */
      case 'query':
        if (!empty($sqlString)) {
          echo $dbHandler->query($sqlString);
        }
        break;

        /* caters for select, insert, update, delete and execute;
         * however resultsets will only be returned for the selects within a batch.
         * Rows-Affected-Count will always be returned for each statement. */
      case 'batch':
        if (!empty($sqlString)) {
          echo $dbHandler->exec_batch($sqlString, $getGeneratedKeys);
        }
        break;

        /* caters for saving stored procedures.
         * The standard php "mysql" functions cant handle the keyword DELIMITER
         * so to allow for storing complex procedures we need to use "mysqli" */
      case 'routine':
        if (!empty($sqlString)) {
          echo $dbHandler->exec_routine($sqlString);
        }
        break;

        /* caters for insert, update, delete and execute;
         * however no resultset will be returned for execute */
      case 'transaction':
        if (!empty($sqlString)) {
          if (strtolower(substr($sqlString, 0, 6)) == 'start ') {
            echo $dbHandler->exec_transaction($sqlString);
          }
        }
        break;

        /* this caters for single resultsets */
      case 'resultset':
        if (!empty($sqlString)) {
          echo $dbHandler->exec_results($sqlString);
        }
        break;

        /* DatabaseMetaData - lists all tables belonging to the open database */
      case 'tables':
        if (!empty($sqlString)) {
          echo $dbHandler->list_tables($sqlString);
        }
        break;

        /* responds to JDBC pings for Connection.isValid() and Connection.isClosed() */
      case 'ping':
        echo 'true';
        break;

        /* just like it sounds, this cleans-up after we have finished
         * and is triggered by java.sql.Connection.close();
         * on the JDBC side */
      case 'cleanup':

        if($_SESSION['DEBUG_MODE']=='true'){
          $timeTaken = microtime(true) - $_SESSION['workStart'];

          echo("--Serverside: Processing took: " . round($timeTaken, 4) . " seconds to complete.\n");
          if (function_exists('memory_get_peak_usage')){
            $_SESSION['memUse'][] = memory_get_peak_usage(true);
            $avg = 0;
            $max = 0;
            $min = -1;
            $n=sizeof($_SESSION['memUse']);
            for($i=0; $i<$n; $i++){
              $avg += $_SESSION['memUse'][$i];
              if($_SESSION['memUse'][$i]>$max){
                $max = $_SESSION['memUse'][$i];
              }
              if($min==-1 && $max>0){
                $min = $max;
              }else if($_SESSION['memUse'][$i]<$min){
                $min = $_SESSION['memUse'][$i];
              }
            }
            echo("--Serverside: Memory Usage was recorded ".$n." times.\n");
            echo("--Serverside: Memory Usage AVERAGE: " . number_format((($avg / $n) / 1048576), 3) . "MB\n");
            echo("--Serverside: Memory Usage MAX: " . number_format(($max / 1048576), 3) . "MB\n");
            echo("--Serverside: Memory Usage MIN: " . number_format(($min / 1048576), 3) . "MB\n");
          }
        }

        WcApiCore::wcKillConnection();
        break;
    }
  }
  if($_SESSION['DEBUG_MODE']=='true') $_SESSION['memUse'][] = function_exists('memory_get_peak_usage') ? memory_get_peak_usage(true) : 0;

} else {
  /* just incase; better to inform user than not */
  WcApiCore::wcCustomHandler(WC_ERROR_CREDS, "Database Credentials are not valid for the available databases. Check your database details.");
}
?>