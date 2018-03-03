<?php
/**
 * Improved mySql implementations. This class is a singleton.
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

class DbMysqli extends DbCore{

  private static $_singleton;
  private $_db;
  private $_dbName;

  /**
   * Create a static db connection
   *
   * @param string $server - severs name EG: localhost is common for network databases
   * @param string $username - database username
   * @param string $password - database password
   * @param string $database - database name
   * @return MySqli Database Connection
   */
  private function __construct($server = WC_DB_SERVER, $username = WC_DB_USERNAME, $password = WC_DB_PASSWORD, $database = WC_DB){
    $this->_dbName = $database;
    $this->_db = mysqli_connect($server, $username, $password, $database);

    /* check connection is open */
    if (mysqli_connect_errno()) {
      $this->throw_error(WC_ERROR_DB_CON, "MYSQLI Connection failed: " . mysqli_connect_error(), mysqli_connect_errno());
    }
    if (mysqli_errno($this->_db)) {
      $this->throw_error(WC_ERROR_DB_CON, "MYSQLI Initialisation Error: " . mysqli_error($this->_db), mysqli_errno($this->_db));
    }

    if (!isset($this->_db)){
      $this->throw_error(WC_ERROR_DB_CON, "UNKNOWN ERROR - Unable to connect to MYSQLI database server!");
    }
  }

  /**
   * Init an instance of this class.
   * If an instance already exists, it is returned instead of a new one.
   */
  static function getInstance(){
    if(is_null(self::$_singleton)){
      self::$_singleton = new DbMysqli();
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
      return mysqli_select_db($this->_db, $this->_dbName);
    }
  }

  /**
   * Returns an unquoted database safe value
   *
   * @param string $value - A database safe value
   */
  function input($value) {
    if(isset($value) && preg_match("/^\d+?$/",$value)){
      return $value;
    }
    return mysqli_real_escape_string($value);
  }

  /**
   * The number of affected rows from the last query.
   *
   * @param $results - ignored in this method.
   * @return Integer - the number of rows affected by the last query
   */
  function affectedRows($results = ''){
    return mysqli_affected_rows($this->_db);
  }

  /**
   * Close the database and free resources
   *
   * @return true on success.
   */
  function close() {
    return mysqli_close($this->_db);
  }

  /**
   * Get the thread ID for this database
   *
   * @return the thread ID
   */
  function thread_id(){
    return mysqli_thread_id($this->_db);
  }

  /**
   * List tables in this database.
   * Updated: 2012-03-11 Removed empty $databaseName placeholder
   * that was causing errors in PHP 5.2.13 (and possibly other versions < 5.3
   *
   * @param mixed $databaseName is ignored.
   * @return A single column ResultSet.
   */
  function list_tables($databaseName){
    return $this->query('SHOW TABLES;');
  }

  /**
   * Execute a routine query.
   * For Mysqli we use the multi_query method.
   *
   * @param string $query
   * @return void
   */
  function exec_routine($query){
    $this->cleanupQuery();

    /* execute multi query */
    if (!mysqli_multi_query($this->_db, $query)) {
      if (mysqli_errno($this->_db)) {
        $this->throw_error(WC_ERROR_DB_QRY, " MYSQLI - " . mysqli_error($this->_db), mysqli_errno($this->_db), $query);
      }else{
        $this->throw_error(WC_ERROR_DB_QRY, 'MYSQLI - Something went wrong while trying to execute the routine statement but no error was thrown. Check your sql syntax first.');
      }
    }
    $this->cleanupQueryMulti();
  }

  /**
   * Cleans up after mysqli.
   * Forces unread items into a destroyable location.
   * Without it mysqli_free_result is unable to completely free up resources.
   */
  private function cleanupQuery(){
    if(mysqli_more_results($this->_db)){
      if($result = mysqli_store_result($this->_db)
      || $result = mysqli_use_result($this->_db)
      ){
        mysqli_free_result($result);
      }
    }
  }

  /**
   * Cleans up after a multiquery.
   * This is important after running mysqli multi_queries
   */
  private function cleanupQueryMulti(){
    $this->cleanupQuery();
    if(mysqli_more_results($this->_db)){
      while(mysqli_next_result($this->_db)){
        $this->cleanupQuery();
      }
    }
  }

  /**
   * Perform a database query and return the results.
   *
   * @param mixed $query
   * @return A resultset or the number of affected rows
   */
  function query($query) {
    $this->cleanupQuery();

    $result = mysqli_query($this->_db, $query, MYSQLI_STORE_RESULT) or $this->throw_error(WC_ERROR_DB_QRY, " MYSQLI - " . mysqli_error($this->_db), mysqli_errno($this->_db), $query);

    return $result;
  }

  /**
   * Execute a query that expects to return a resultset.
   *
   * @param mixed $query
   * @return A resultset
   */
  function queryResult($query) {
    return query($query);
  }

  /**
   * Fetch the current resultset row as an array
   *
   * @param mixed $query
   * @return a resultset row as an associative array
   */
  function fetch_array($query) {
    return mysqli_fetch_array($query, MYSQL_ASSOC);
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
    if($this->data_seek($result, $row)){
      $item_data = mysqli_fetch_row($result);
      $item_data = $item_data[$field];
    }else{
      $item_data = NULL;
    }
    return $item_data;
  }

  /**
   * The number of rows in this resultset
   *
   * @param mixed $query
   * @return int - the number of resultset rows
   */
  function num_rows($query) {
    return mysqli_num_rows($query);
  }

  /**
   * Move the resultset pointer to a given row.
   *
   * @param resultset $result
   * @param int $row_number
   * @return boolean - true on success
   */
  function data_seek($result, $row_number) {
    return mysqli_data_seek($result, $row_number);
  }

  /**
   * Get the last autoindexed index number
   *
   * @return int - autoindex ID
   */
  function insert_id() {
    // i noticed there was a bug with mysql_insert_id() in php-5.2.2 (should have been fixed in 5.1)
    // just incase, take the same precaution for mysqli
    if (version_compare(PHP_VERSION, '5.0.0', '>=')) {
      $id = $this->result("select LAST_INSERT_ID() as insert_id;", 0, 0);
    } else {
      $id = mysqli_insert_id($this->_db);
    }
    return $id;
  }

  /**
   * Free up resources associated with this resultset
   *
   * @param resultset $result
   * @return boolean - true on success
   */
  function free_result($result) {
    return mysqli_free_result($result);
  }

  /**
   * Gets information on a resultsets columns
   *
   * @param resultset $result
   * @return metadata information about a column
   */
  function fetch_fields($result) {
    return mysqli_fetch_field($result);
  }
}
?>