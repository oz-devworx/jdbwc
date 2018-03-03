<?php
/**
 * PostgreSql core implementation. This class is a singleton.
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

class DbPostgre extends DbCore{

  private static $_singleton;
  private $_db;

  /**
   * Create a static db connection
   *
   * @param string $server - severs name EG: localhost is common for network databases
   * @param string $username - database username
   * @param string $password - database password
   * @param string $database - database name
   * @return PostgreSQL Database Connection
   */
  private function __construct($server = WC_DB_SERVER, $username = WC_DB_USERNAME, $password = WC_DB_PASSWORD, $database = WC_DB){
    $this->_db = pg_connect("host=$server dbname=$database user=$username password=$password");

    if (!isset($this->_db)){
      $this->throw_error(WC_ERROR_DB_CON, "Unable to connect to database DbPostgre server!");
    }
  }

  /**
   * Init an instance of this class.
   * If an instance already exists, it is returned instead of a new one.
   */
  static function getInstance(){
    if(is_null(self::$_singleton)){
      self::$_singleton = new DbPostgre();
    }
    return self::$_singleton;
  }

  /**
   * Set the default DB.
   * PostgreSQL has no method for this as its probably not required.
   * It is required for this class because we are implementing an abstract parent.
   *
   * @return true
   */
  function select_db(){
    return true;
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
    return pg_escape_string($this->_db, $value);
  }

  /**
   * The number of affected rows from the last query.
   *
   * @param $results - A resultset
   * @return Integer - the number of rows affected by the last query
   */
  function affectedRows($results = ''){
    return pg_affected_rows($results);
  }

  /**
   * Close the database and free resources
   *
   * @return true on success.
   */
  function close() {
    return pg_close($this->_db);
  }

  /**
   * Get the thread ID for this database
   *
   * @return the thread ID
   */
  function thread_id(){
    return pg_get_pid($this->_db);
  }

  /**
   * List tables in this database
   *
   * @param mixed $databaseName is ignored.
   * @return A single column ResultSet.
   */
  function list_tables($databaseName){
    return $this->query("SELECT relname FROM pg_class WHERE relname !~ '^(pg_|sql_)' AND relkind = 'r';", $this->_db);
  }

  /**
   * Perform a database query and return the results.
   *
   * @param mixed $query
   * @return A resultset or the number of affected rows
   */
  function query($query) {
    $result = pg_query($this->_db, $query) or $this->throw_error(WC_ERROR_DB_QRY, "PostgreSQL - " . pg_errormessage(), 0, $query);

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
    return pg_fetch_array($query, null, PGSQL_ASSOC);
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
    return pg_result($result, $row, $field);
  }

  /**
   * Execute a routine query.
   *
   * @param mixed $query
   * @return void
   */
  function exec_routine($query){
    return $this->query($query);
  }

  /**
   * The number of rows in this resultset
   *
   * @param mixed $query
   * @return int - the number of resultset rows
   */
  function num_rows($query) {
    return pg_num_rows($query);
  }

  /**
   * Move the resultset pointer to a given row.
   *
   * @param resultset $result
   * @param int $row_number
   * @return boolean - true on success
   */
  function data_seek($result, $row_number) {
    return pg_result_seek($result, $row_number);
  }

  /**
   * Get the last autoindexed index number
   *
   * @return int - autoindex ID
   */
  function insert_id() {
    $id_query = $this->query("select LASTVAL() as insert_id;");
    $id_result = $this->fetch_array($id_query);

    return $id_result['insert_id'];
  }

  /**
   * Free up resources associated with this resultset
   *
   * @param resultset $result
   * @return boolean - true on success
   */
  function free_result($query) {
    return pg_free_result($query);
  }

  /**
   * Gets information on a resultsets columns
   *
   * @param resultset $result
   * @return metadata information about a column
   */
  function fetch_fields($query) {
    return false;
  }
}
?>