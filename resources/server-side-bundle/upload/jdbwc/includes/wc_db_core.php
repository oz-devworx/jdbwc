<?php
/**
 * Check for the db type and load the appropriate class.
 *
 * Released under the GNU General Public License
 *
 * @package jdbwc.includes
 * @author Tim Gall
 * @version 2007-10
 * @version 2008-05-06 19:34 (+10 GMT)
 * @copyright (C) 2008 Oz-DevWorX (Tim Gall)
 */

//incase the server ignores .htaccess directives
if(!defined('WC_INDEX')){
  header('Location: index.php');
  exit();
}

//core db class
require_once(WC_PRI_INC . 'classes/DbCore.php');

$dbType = isset($_POST['dbType']) ? (int)$_POST['dbType'] : 0;
switch ($dbType) {
  case 2: // type 2 is postgreSQL
    require_once (WC_PRI_INC . 'classes/DbPostgre.php');
    $dbHandler = DbPostgre::getInstance();
    break;

  case 0:
  case 1: // type 0 & 1 & default are mySQL.
  default:
    if(class_exists('mysqli')){
      require_once (WC_PRI_INC . 'classes/DbMysqli.php');// improved mysql implementation
      $dbHandler = DbMysqli::getInstance();
    }else{
      require_once (WC_PRI_INC . 'classes/DbMysql.php');// standard mysql implementation
      $dbHandler = DbMysql::getInstance();
    }
    break;
}
?>