<?php
/**
 * Makes it easier to be able to implement a range of DB backends
 * and ensures we only have 1 set of methods in memory at a time.
 * Implementations should use a singleton pattern.
 *
 * Released under the GNU General Public License
 *
 * @package jdbwc.includes.classes
 * @author Tim Gall
 * @copyright (C) 2010 Oz-DevWorX (Tim Gall)
 */

//incase the server ignores .htaccess directives
if(!defined('WC_INDEX')){
  header('Location: index.php');
  exit();
}

abstract class DbCore{

  /**
   * Handles errors for db methods.
   *
   * @param int $errcode - a static wc error code
   * @param mixed $errmess - database error message
   * @param mixed $errno - [optional] database error number
   * @param mixed $query - [optional] query that caused the error (if any)
   */
  function throw_error($errcode, $errmess, $errno='', $query='') {
    if($query!=''){
      $errmess .= "\n\tQUERY: " . $query;
    }
    WcApiCore::wcCustomHandler($errcode, $errmess, $errno);
  }

  /**
   * Execute a single query with result & return the
   * rows as a .csv string.
   *
   * @param mixed $sqlString
   * @return
   */
  function exec_results($query) {
    $sqlQuery = $this->query($query);
    $resultSize = $this->num_rows($sqlQuery);
    return WcApiCore::wcBuildCSV($sqlQuery, $resultSize);
  }

  /**
   * Process a single batch of sql queries.
   * Can be INSERT, UPDATE, DELETE, EXECUTE.
   * $query should use complete inserts. Multiple rows per statement is ok:
   * EG: INSERT INTO xyz (cell_01, cell_02, cell_03) VALUES ('abc', 'def', 'ghi'), ('jk', 'lmn', 'opq'), etc;
   *
   * @param mixed $query
   * @return A complex resultset bundle containing the rows affected by each query
   * as a comma seperated list of integers on the first line.
   * The rest of the resturn string is a set of double-newline seperated
   * .csv tables produced by each select query in the batch.
   */
  function exec_batch($query, $getGeneratedKeys = false) {
    $batchResults = "";
    $firstLine = "";
    $generatedKeys = "";
    $query_split = explode(";", trim($query));
    $qCntLimit = sizeof($query_split);
    $qCnt = 0;
    foreach ($query_split as $sqlString) {
      $sqlString = trim($sqlString);
      if ($sqlString  != null && $sqlString != '') {
        $rowsAffected = 0;
        $sqlQuery = $this->query($sqlString . ";");
        $sqlComa = ',';//($qCnt < $qCntLimit-1) ? ',':'';

        $cline = '';
        if(strlen($sqlString)>=4){
          $cline = substr($sqlString, 0, 4);
        }

        if ((strlen($cline)==4) &&
        strcasecmp($cline, 'show') == 0 ||
        strcasecmp($cline, 'exec') == 0 ||
        strcasecmp($cline, 'sele') == 0 || // select
        strcasecmp($cline, 'expl') == 0 || // explain
        strcasecmp($cline, 'desc') == 0 || // describe
        strcasecmp($cline, 'data') == 0) // database()
        {
          $rowsAffected = $this->num_rows($sqlQuery);
          $batchResults .= WcApiCore::wcBuildCSV($sqlQuery, $rowsAffected);
        } else {
          if ($getGeneratedKeys) {
            if (substr_compare($sqlString, 'insert', 0, 6, true) == 0) {
              $generatedKeys .= $this->insert_id() . $sqlComa;
            } else {
              $generatedKeys .= '-1' . $sqlComa;
            }
          }
          $rowsAffected = $this->affectedRows($sqlQuery);
          $batchResults .= "_" . WC_EOF;
        }
        $firstLine .= $rowsAffected . ",";
        $qCnt++;
      }
    }
    if ($getGeneratedKeys && compare($generatedKeys, "") != 0) {
      $generatedKeys .= WC_EOF;
    }
    return $firstLine . WC_EOF . $generatedKeys . $batchResults;
  }

  /**
   * Process a single SQL Transaction (usually consists of a batch of sql queries)
   * Can be INSERT, UPDATE, DELETE, EXECUTE.
   * $query should use complete inserts. Multiple rows per statement is ok:
   * EG: INSERT INTO xyz (cell_01, cell_02, cell_03) VALUES ('abc', 'def', 'ghi'), ('jk', 'lmn', 'opq'), etc;
   *
   * @param mixed $query
   * @return
   */
  function exec_transaction($query) {
    return $this->exec_batch($query);
  }

  /**
   * Select the default db for use during this connection.
   *
   * @return true on success.
   */
  abstract function select_db();

  /**
   * Returns an escaped database safe value.
   * To comply with the rest of this API, don't quote unquoted values.
   *
   * @param mixed $value
   */
  abstract function input($value);

  /**
   * Rows affected by the last query
   *
   * @return Integer - the number of rows affected by the last query
   */
  abstract function affectedRows($results = '');

  /**
   * Close the database connection
   *
   * @param string $link
   * @return true on success
   */
  abstract function close();

  /**
   * List tables in this database
   *
   * @param mixed $databaseName is ignored.
   * @return A single column ResultSet.
   */
  abstract function list_tables($dbName);

  /**
   * Get the thread ID for this database
   *
   * @return the thread ID
   */
  abstract function thread_id();

  /**
   * Perform a DDL or DML query
   *
   * @param mixed $query
   * @return A resultset or the number of affected rows
   */
  abstract function query($query);

  /**
   * Execute a query that expects to return a resultset.
   *
   * @param mixed $query
   * @return A resultset
   */
  abstract function queryResult($query);

  /**
   * Fetch the current resultset row as an array
   *
   * @param mixed $query
   * @return a resultset row as an associative array
   */
  abstract function fetch_array($query);

  /**
   * Get the result of a simple query. Only 1 value from a resultset row is returned
   * or NULL if the "$row, $field" are not valid indices.
   *
   * @param resultset $result A resultset
   * @param int $row The row number to get a value from
   * @param mixed $field can be the column name or index
   * @return 1 value from a resultset row
   */
  abstract function result($result, $row, $field = '');

  /**
   * Execute a routine.
   *
   * @param mixed $query
   * @return void
   */
  abstract function exec_routine($query);

  /**
   * The number of rows in this resultset
   *
   * @param mixed $query
   * @return int - the number of resultset rows
   */
  abstract function num_rows($query);

  /**
   * Move the resultset pointer to a given row.
   *
   * @param resultset $result
   * @param int $row_number
   * @return boolean - true on success
   */
  abstract function data_seek($result, $row_number);

  /**
   * Get the last autoindexed index number
   *
   * @return int - autoindex ID
   */
  abstract function insert_id();

  /**
   * Free up resources associated with this resultset
   *
   * @param resultset $result
   * @return boolean - true on success
   */
  abstract function free_result($result);

  /**
   * Fetch column information from a resultset
   *
   * @param mixed $result
   * @return metadata information about a column
   */
  abstract function fetch_fields($result);
}
?>