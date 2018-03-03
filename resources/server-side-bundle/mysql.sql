-- DONT UPLOAD THIS FILE TO YOUR WEB SERVER. 
-- Its for the jdbwctest package if you want to test the driver.
-- Its for inserting into the web servers database only.
-- Otherwise you can ignore this file.

--
-- Table structure for table `test01`
--

DROP TABLE IF EXISTS `test01`;
CREATE TABLE IF NOT EXISTS `test01` (
  `myidx` int(11) NOT NULL AUTO_INCREMENT,
  `valkey` varchar(32) NOT NULL DEFAULT '',
  `expiry` int(11) unsigned NOT NULL DEFAULT '0',
  `value` text,
  PRIMARY KEY (`myidx`),
  UNIQUE KEY `valkey` (`valkey`),
  KEY `expiry` (`expiry`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `test02`
--

DROP TABLE IF EXISTS `test02`;
CREATE TABLE IF NOT EXISTS `test02` (
  `test02_id` int(11) NOT NULL AUTO_INCREMENT,
  `valkey` varchar(32) NOT NULL,
  `accessed` datetime NOT NULL,
  PRIMARY KEY (`test02_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;
