<?php
/**
 * Id: wc_static_vars.php
 * Don't change any of these values or things won't work on the JDBC side.
 *
 * Released under the GNU General Public License
 *
 * @package jdbwc.includes
 * @author Tim Gall
 * @version 2007-10-01
 * @version 2010-03-27 16:10 (+10 GMT)
 * @copyright (C) 2008 Oz-DevWorX (Tim Gall)
 */

//incase the server ignores .htaccess directives
if(!defined('WC_INDEX')){
  header('Location: index.php');
  exit();
}

define('WC_VERSION', '1.0.0-3 RC2');
define('WC_ERROR', 'ERROR# ');
define('WC_AUTH', 'jdbwcAuth');
define('WC_ACTION', 'jdbwcAction');
define('WC_GET_ROWS', 'getRows');
define('WC_GET_KEYS', 'getKeys');
define('WC_DEBUG_TAG', 'debug');
define('WC_SESSION', 'jdbwcSession'); // session key
define('WC_SALT_LENGTH', 16);//dont change this!!!

//custom error codes (for error handling)
define('WC_ERROR_HACK', 1);
define('WC_ERROR_PATH', 2);
define('WC_ERROR_CREDS', 3);
define('WC_ERROR_SESS', 4);
define('WC_ERROR_DB_CON', 5);
define('WC_ERROR_DB_QRY', 6);
//custom error strings (for error handling)
define('WC_ERROR_HACK_STR', 'ERROR-HACK: ');
define('WC_ERROR_PATH_STR', 'ERROR-PATH: ');
define('WC_ERROR_CREDS_STR', 'ERROR-BAD-CREDENTIALS: ');
define('WC_ERROR_SESS_STR', 'ERROR-SESSION: ');
define('WC_ERROR_DB_CON_STR', 'ERROR-DB-CONNECTION: ');
define('WC_ERROR_DB_QRY_STR', 'ERROR-DB-QUERY: ');
define('WC_ERROR_UNKNOWN_STR', 'ERROR-Unknown: ');
define('WC_EX_STR', 'PHP EXCEPTION: ');

/** void (empty string) */
define('WC_VD', '#_040');
/**  , (comma) */
define('WC_CA', '#_002C');
/** \r\n (carriage return + newline) */
define('WC_CN', '#_CN');
/** \r (carriage return) */
define('WC_CR', '#_CR');
/** \n (newline) */
define('WC_NL', '#_NL');
/** \f (form feed) */
define('WC_FF', '#_FF');
/** " (double quote) */
define('WC_DQ', '#_0022');
/** ' (single quote) */
define('WC_SQ', '#_0027');

/** end of line */
define('WC_EOL', "_EOL__");
/** end of file */
define('WC_EOF', "_EOF__");
?>