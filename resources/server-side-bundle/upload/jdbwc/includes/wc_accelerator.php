<?php
/**
 * Provides output compression if the client can accept it.
 *
 * Released under the GNU General Public License
 *
 * @package jdbwc.includes
 * @author Tim Gall
 * @version 2010-03-25 13:52 (+10 GMT)
 * @copyright (C) 2008 Oz-DevWorX (Tim Gall)
 */

//incase the server ignores .htaccess directives
if(!defined('WC_INDEX')){
  header('Location: index.php');
  exit();
}

ob_start("wc_compress_output");

/**
 * Callback function for ob_start()
 * Provides compression (when supported).
 *
 * @param string $buffer The servers output buffer
 * @return string - the contents from the servers output buffer.
 */
function wc_compress_output($buffer) {
  $power = 'JDBWC';//header tracking
  $buffer = trim($buffer);

  $compress_me = FALSE;

  if(extension_loaded('zlib') && (!ini_get('zlib.output_compression') || strtolower(ini_get('zlib.output_compression'))=='off') && ini_get('output_handler') != 'ob_gzhandler'){
    if(find_match(getenv("HTTP_ACCEPT_ENCODING"), 'gzip') || find_match(getenv("HTTP_ACCEPT_ENCODING"), 'x-gzip')){
      $buffer = gzencode($buffer, 3, FORCE_GZIP);
      $compress_me = 'gzip';
    } elseif(find_match(getenv("HTTP_ACCEPT_ENCODING"), 'deflate')){
      $buffer = gzcompress($buffer, 3);
      $compress_me = 'deflate';
    }

    if($compress_me!=FALSE){
      header('Vary: Accept-Encoding', TRUE);
      header('Content-Encoding: ' . $compress_me, TRUE);
      header('Content-Length: ' . strlen($buffer), TRUE); // keep-alive trigger
    }
  }

  header('X-Powered-By: ' . $power);
  header('Content-Type: text/html; charset=utf-8');

  //see: http://php.net/manual/en/function.ob-start.php
  chdir(dirname($_SERVER['SCRIPT_FILENAME']));

  return $buffer;
}

/**
 * Find a value in a delimited string
 *
 * @param string $haystack - The value to search
 * @param string $needle - The value to look for
 * @param string $dlmr - The $search string delimiter seperating entries
 * @return boolean - true if a match is found.
 */
function find_match($haystack, $needle, $dlmr = ',') {
  $tok_set = explode($dlmr, $haystack);
  foreach ($tok_set as $tok)
    if (trim($tok) == $needle) return true;
  return false;
}
?>