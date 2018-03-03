<?php
/**
 * Standard mySql implementations. This class is a singleton.
 *
 * Released under the GNU General Public License
 *
 * @package jdbwc.includes.classes
 * @author Tim Gall
 * @copyright (C) 2008 Oz-DevWorX (Tim Gall)
 */

//incase the server ignores .htaccess directives
if(!defined('WC_INDEX')){
  header('Location: index.php');
  exit();
}

class DbMysql extends DbCore{

  private static $_singleton;
  private $_db;
  private $_dbName;

  /**
   * Create a static db connection
   *
   * @param mixed $server
   * @param mixed $username
   * @param mixed $password
   * @param mixed $database
   * @return MySql Database Connection
   */
  private function __construct($server = WC_DB_SERVER, $username = WC_DB_USERNAME, $password = WC_DB_PASSWORD, $database = WC_DB){
    $this->_dbName = $database;
    $this->_db = mysql_connect($server, $username, $password, 'db_link');
    if (isset($this->_db)){
      mysql_select_db($database);
    }else{
      $this->throw_error(WC_ERROR_DB_CON, "Unable to connect to database DbMysql server!");
    }
  }

  /**
   * Init an instance of this class.
   * If an instance already exists, it is returned instead of a new one.
   */
  static function getInstance(){
    if(is_null(self::$_singleton)){
      self::$_singleton = new DbMysql();
    }
    return self::$_singleton;
  }

  /**
   * Set the default DB
   *
   * @return true on success.
   */
  function select_db(){
    if ($this->_dbName == $this->result($this->query("SELECT DATABASE();"), 0, 0)) {
      return true;
    }else{
      return mysql_select_db($this->_dbName, $this->_db);
    }
  }

  /**
   * Returns an unquoted database safe value
   *
   * @param mixed $value
   */
  function input($value) {
    if(isset($value) && preg_match("/^\d+?$/",$value)){
      return $value;
    }
    return mysql_real_escape_string($value);
  }

  /**
   * The number of affected rows from the last query.
   *
   * @param $results - ignored in this method.
   * @return Integer - the number of rows affected by the last query
   */
  function affectedRows($results = ''){
    return mysql_affected_rows($this->_db);
  }

  /**
   * Close the database and free resources
   *
   * @return true on success.
   */
  function close() {
    return mysql_close($this->_db);
  }

  /**
   * Get the thread ID for this database
   *
   * @return the thread ID
   */
  function thread_id() {
    return mysql_thread_id($this->_db);
  }

  /**
   * List tables in this database
   *
   * @param mixed $databaseName is ignored.
   * @return A single column ResultSet.
   */
  function list_tables($databaseName) {
    return mysql_list_tables($databaseName);
  }

  /**
   * Execute a routine query.
   *
   * @param mixed $query
   * @return void
   */
  function exec_routine($query) {
    $resultStr = "";

    /* try to execute query */
    if (!mysql_query($query)) {
      if (mysql_errno()) {
        $this->throw_error(WC_ERROR_DB_QRY, '(HINT: If you enable the mysqli extension in your PHP library, this should work)' . mysql_error(), mysql_errno());
      } else {
        $this->throw_error(WC_ERROR_DB_QRY, 'MYSQL Error: Something went wrong while trying to execute the routine statement but no error was thrown. Check your sql syntax first.');
      }
    }
    return $resultStr;
  }

  /**
   * Perform a database query and return the results.
   *
   * @param mixed $query
   * @return A resultset or the number of affected rows
   */
  function query($query) {
    $result = mysql_query($query, $this->_db) or $this->throw_error(WC_ERROR_DB_QRY, mysql_error(), mysql_errno(), $query);

    return $result;
  }

  /**
   * Execute a query that expects to return a resultset.
   *
   * @param mixed $query
   * @return A resultset
   */
  function queryResult($query) {
    return $this->query($query, $this->_db);
  }

  /**
   * Fetch the current resultset row as an array
   *
   * @param mixed $query
   * @return a resultset row as an associative array
   */
  function fetch_array($query) {
    return mysql_fetch_array($query, MYSQL_ASSOC);
  }

  /**
   * Get the result of a simple query. Only 1 value from a resultset row is returned
   * or NULL if the "$row, $field" are not valid indices.
   *
   * @param resultset $result A resultset
   * @param int $row The row number to get a value from
   * @param mixed $field can be the column name or index
   * @return 1 value from a resultset row
   */
  function result($result, $row, $field = '') {
    return mysql_result($result, $row, $field);
  }

  /**
   * The number of rows in this resultset
   *
   * @param mixed $query
   * @return int - the number of resultset rows
   */
  function num_rows($query) {
    return mysql_num_rows($query);
  }

  /**
   * Move the resultset pointer to a given row.
   *
   * @param resultset $result
   * @param int $row_number
   * @return boolean - true on success
   */
  function data_seek($result, $row_number) {
    return mysql_data_seek($result, $row_number);
  }

  /**
   * Get the last autoindexed index number
   *
   * @return int - autoindex ID
   */
  function insert_id() {
    // fixes a bad-behaviour issue with mysql_insert_id() that i noticed in php-5.2.2
    // using the mysql-db is a failsafe method for requesting insert_id's
    if (version_compare(PHP_VERSION, '5.0.0', '>=')) {
      $id = $this->result("select LAST_INSERT_ID() as insert_id;", 0, 0);
    } else {
      $id = mysql_insert_id();
    }
    return $id;
  }

  /**
   * Free up resources associated with this resultset
   *
   * @param resultset $result
   * @return boolean - true on success
   */
  function free_result($query) {
    return mysql_free_result($query);
  }

  /**
   * Gets information on a resultsets columns
   *
   * @param resultset $result
   * @return metadata information about a column
   */
  function fetch_fields($query) {
    return mysql_fetch_field($query);
  }
}
?>