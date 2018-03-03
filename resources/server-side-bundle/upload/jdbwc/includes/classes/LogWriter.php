<?php
/**
 * Logging class for jdbWC
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

final class LogWriter {

  private function __construct(){
    //private constructor forces static class use
  }

  /**
   * Write data to the active log file using the PHP default logging method.
   *
   * @param string $message - message to write
   * @param string $type - A descriptive tag for the type of info being written
   */
  public static function write($message, $type) {
    error_log(date('Y-m-d H:i:s(\G\M\TP)') . ' [' . $type . '] [IP:' . LogWriter::getIP() . '] ' . $message . PHP_EOL, 3, WC_LOG);
  }

  /**
   * Return a log file as a string.
   * @param bit $type - 0 for current, 1 for archived
   */
  public static function get($type=0){
    if($type==0){
      return file_get_contents(WC_LOG);
    }else{
      return file_get_contents(WC_LOG_ARCHIVE);
    }
  }

  /**
   * Alias for archive(). Truncates the active log and archives the old data.
   */
  public static function clear(){
    LogWriter::archive();
  }

  /**
   * Truncates the active log and archives the old data.
   */
  public static function archive(){
    $archive = LogWriter::get(0);
    file_put_contents(WC_LOG_ARCHIVE, PHP_EOL.PHP_EOL.'ARCHIVED '.date('Y-m-d H:i:s(\G\M\TP)').PHP_EOL.$archive, FILE_APPEND);
    file_put_contents(WC_LOG, '');
  }

  private static function getIP() {
    if (getenv('REMOTE_ADDR')) {
      $ip = getenv('REMOTE_ADDR');
    } elseif (getenv('HTTP_CLIENT_IP')) {
      $ip = getenv('HTTP_CLIENT_IP');
    } else {
      $ip = getenv('HTTP_X_FORWARDED_FOR');
    }
    return $ip;
  }
}
?>