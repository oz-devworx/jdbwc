<?php
/**
 * LOCAL DATABASE FILE:
 * Usually only for developers who need to test thier application offline
 * using thier local test server. If you dont have one setup and require one,
 * check out www.apachefriends.org;
 * PostgreSQL needs to be installed seperately if you require it.
 *
 * Contains the database settings for each database you wish to allow remote access to.
 * If its not listed in this file, the database will not be accessible to the JDBC Driver.
 *
 * HOW TO:
 * add databases you want to be accessed to the $database_array[]
 * EG:
 ***********************************************************************
 * $database_array[] = array('wc_database' => 'database01NameHere',
 *                 'wc_user' => 'userName01Here',
 *                 'wc_password' => 'passWord01Here');
 *
 * $database_array[] = array('wc_database' => 'database02NameHere',
 *                 'wc_user' => 'userName02Here',
 *                 'wc_password' => 'passWord02Here');
 *
 * etc...
 ***********************************************************************
 *
 * Released under the GNU General Public License
 *
 * @package jdbwc.includes.config.local
 * @author Tim Gall
 * @version 2007-12-12 10:42 (+10 GMT)
 * @version 2008-06-30 12:07 (+10 GMT)
 * @copyright (C) 2008 Oz-DevWorX (Tim Gall)
 */

//incase the server ignores .htaccess directives
if(!defined('WC_INDEX')){
  header('Location: index.php');
  exit();
}

define('WC_DB_SERVER', 'localhost'); // usually localhost

$database_array = array();

$database_array[] = array('wc_database' => 'xxxxxxxxxxxxxx',
						  'wc_user' => 'xxxxxxxxxxxxxxx',
						  'wc_password' => 'xxxxxxxxxxxxxxx');
?>