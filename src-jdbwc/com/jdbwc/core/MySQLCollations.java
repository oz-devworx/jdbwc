/* ********************************************************************
 * Copyright (C) 2010 Oz-DevWorX (Tim Gall)
 * ********************************************************************
 * This file is part of JDBWC.
 *
 * JDBWC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JDBWC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JDBWC.  If not, see <http://www.gnu.org/licenses/>.
 * ********************************************************************
 */
package com.jdbwc.core;


import com.ozdevworx.dtype.DataHandler;
import com.ozdevworx.dtype.impl.ObjectList;


/**
 * @author Tim Gall
 * @version 2010-05-08
 */
public final class MySQLCollations {

	private final static DataHandler collations = new ObjectList();

	static {
		collations.addData("1", "big5_chinese_ci");
		collations.addData("2", "latin2_czech_cs");
		collations.addData("3", "dec8_swedish_ci");
		collations.addData("4", "cp850_general_ci");
		collations.addData("5", "latin1_german1_ci");
		collations.addData("6", "hp8_english_ci");
		collations.addData("7", "koi8r_general_ci");
		collations.addData("8", "latin1_swedish_ci");
		collations.addData("9", "latin2_general_ci");
		collations.addData("10", "swe7_swedish_ci");
		collations.addData("11", "ascii_general_ci");
		collations.addData("12", "ujis_japanese_ci");
		collations.addData("13", "sjis_japanese_ci");
		collations.addData("14", "cp1251_bulgarian_ci");
		collations.addData("15", "latin1_danish_ci");
		collations.addData("16", "hebrew_general_ci");
		collations.addData("18", "tis620_thai_ci");
		collations.addData("19", "euckr_korean_ci");
		collations.addData("20", "latin7_estonian_cs");
		collations.addData("21", "latin2_hungarian_ci");
		collations.addData("22", "koi8u_general_ci");
		collations.addData("23", "cp1251_ukrainian_ci");
		collations.addData("24", "gb2312_chinese_ci");
		collations.addData("25", "greek_general_ci");
		collations.addData("26", "cp1250_general_ci");
		collations.addData("27", "latin2_croatian_ci");
		collations.addData("28", "gbk_chinese_ci");
		collations.addData("29", "cp1257_lithuanian_ci");
		collations.addData("30", "latin5_turkish_ci");
		collations.addData("31", "latin1_german2_ci");
		collations.addData("32", "armscii8_general_ci");
		collations.addData("33", "utf8_general_ci");
		collations.addData("34", "cp1250_czech_cs");
		collations.addData("35", "ucs2_general_ci");
		collations.addData("36", "cp866_general_ci");
		collations.addData("37", "keybcs2_general_ci");
		collations.addData("38", "macce_general_ci");
		collations.addData("39", "macroman_general_ci");
		collations.addData("40", "cp852_general_ci");
		collations.addData("41", "latin7_general_ci");
		collations.addData("42", "latin7_general_cs");
		collations.addData("43", "macce_bin");
		collations.addData("44", "cp1250_croatian_ci");
		collations.addData("47", "latin1_bin");
		collations.addData("48", "latin1_general_ci");
		collations.addData("49", "latin1_general_cs");
		collations.addData("50", "cp1251_bin");
		collations.addData("51", "cp1251_general_ci");
		collations.addData("52", "cp1251_general_cs");
		collations.addData("53", "macroman_bin");
		collations.addData("57", "cp1256_general_ci");
		collations.addData("58", "cp1257_bin");
		collations.addData("59", "cp1257_general_ci");
		collations.addData("63", "binary");
		collations.addData("64", "armscii8_bin");
		collations.addData("65", "ascii_bin");
		collations.addData("66", "cp1250_bin");
		collations.addData("67", "cp1256_bin");
		collations.addData("68", "cp866_bin");
		collations.addData("69", "dec8_bin");
		collations.addData("70", "greek_bin");
		collations.addData("71", "hebrew_bin");
		collations.addData("72", "hp8_bin");
		collations.addData("73", "keybcs2_bin");
		collations.addData("74", "koi8r_bin");
		collations.addData("75", "koi8u_bin");
		collations.addData("77", "latin2_bin");
		collations.addData("78", "latin5_bin");
		collations.addData("79", "latin7_bin");
		collations.addData("80", "cp850_bin");
		collations.addData("81", "cp852_bin");
		collations.addData("82", "swe7_bin");
		collations.addData("83", "utf8_bin");
		collations.addData("84", "big5_bin");
		collations.addData("85", "euckr_bin");
		collations.addData("86", "gb2312_bin");
		collations.addData("87", "gbk_bin");
		collations.addData("88", "sjis_bin");
		collations.addData("89", "tis620_bin");
		collations.addData("90", "ucs2_bin");
		collations.addData("91", "ujis_bin");
		collations.addData("92", "geostd8_general_ci");
		collations.addData("93", "geostd8_bin");
		collations.addData("94", "latin1_spanish_ci");
		collations.addData("95", "cp932_japanese_ci");
		collations.addData("96", "cp932_bin");
		collations.addData("97", "eucjpms_japanese_ci");
		collations.addData("98", "eucjpms_bin");
		collations.addData("99", "cp1250_polish_ci");
		collations.addData("128", "ucs2_unicode_ci");
		collations.addData("129", "ucs2_icelandic_ci");
		collations.addData("130", "ucs2_latvian_ci");
		collations.addData("131", "ucs2_romanian_ci");
		collations.addData("132", "ucs2_slovenian_ci");
		collations.addData("133", "ucs2_polish_ci");
		collations.addData("134", "ucs2_estonian_ci");
		collations.addData("135", "ucs2_spanish_ci");
		collations.addData("136", "ucs2_swedish_ci");
		collations.addData("137", "ucs2_turkish_ci");
		collations.addData("138", "ucs2_czech_ci");
		collations.addData("139", "ucs2_danish_ci");
		collations.addData("140", "ucs2_lithuanian_ci");
		collations.addData("141", "ucs2_slovak_ci");
		collations.addData("142", "ucs2_spanish2_ci");
		collations.addData("143", "ucs2_roman_ci");
		collations.addData("144", "ucs2_persian_ci");
		collations.addData("145", "ucs2_esperanto_ci");
		collations.addData("146", "ucs2_hungarian_ci");
		collations.addData("192", "utf8_unicode_ci");
		collations.addData("193", "utf8_icelandic_ci");
		collations.addData("194", "utf8_latvian_ci");
		collations.addData("195", "utf8_romanian_ci");
		collations.addData("196", "utf8_slovenian_ci");
		collations.addData("197", "utf8_polish_ci");
		collations.addData("198", "utf8_estonian_ci");
		collations.addData("199", "utf8_spanish_ci");
		collations.addData("200", "utf8_swedish_ci");
		collations.addData("201", "utf8_turkish_ci");
		collations.addData("202", "utf8_czech_ci");
		collations.addData("203", "utf8_danish_ci");
		collations.addData("204", "utf8_lithuanian_ci");
		collations.addData("205", "utf8_slovak_ci");
		collations.addData("206", "utf8_spanish2_ci");
		collations.addData("207", "utf8_roman_ci");
		collations.addData("208", "utf8_persian_ci");
		collations.addData("209", "utf8_esperanto_ci");
		collations.addData("210", "utf8_hungarian_ci");
	}

	public static String getCollation(int collationId) {
		return collations.getString(String.valueOf(collationId));
	}
}
