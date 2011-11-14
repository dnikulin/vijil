// Copyright (C) 2011  Dmitri Nikulin
//
// This file is part of Vijil.
//
// Vijil is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Vijil is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Vijil.  If not, see <http://www.gnu.org/licenses/>.
//
// Repository:     https://github.com/dnikulin/vijil
// Email:          dnikulin+vijil@gmail.com

package com.dnikulin.vijil.tools;

import java.util.regex.Pattern;

public final class CleanString {
    public static final Pattern weirdWhitePattern =
        Pattern.compile("[\\s\u00a0\u2002\u2003\u2009]+");

    public static final Pattern uniJunkPattern =
        Pattern.compile("[\uf000-\uffff]+");

    public static final Pattern whitePattern =
        Pattern.compile("\\s+");

    public static final Pattern paragraphPattern =
        Pattern.compile("\\s*\r?\n\\s*\r?\n\\s*");

    public static final Pattern referencePattern =
        Pattern.compile("\\[[\\s\\-]*[0-9]*[\\s\\-]*\\]");

    public static final Pattern nonLetterPattern =
        Pattern.compile("[^\\p{L}]+");

    public static final Pattern nonLetterNumberPattern =
        Pattern.compile("[^0-9\\p{L}]+");

    public static String cleanString(String s) {
        return cleanQuotes(cleanWhite(cleanReferences(s))).trim();
    }

    public static String fixNewlines(String s) {
        return s.
        // See http://en.wikipedia.org/wiki/Crlf#Representations
        replace("\r\n", "\n"). // Windows CR-LF.
        replace('\r', '\n');   // MacOS CR.
    }

    public static String unaccent(String s) {
        return s.
        replace("Æ", "AE").
        replace("æ", "ae").
        replace("œ", "oe").
        replace("ç", "c").
        replace("ς", "c").
        replace("Ç", "C").
        replace("Ð", "D").
        replace("Ñ", "N").
        replace("ñ", "n").
        replace("Ý", "Y").
        replaceAll("[ÀÁÂÃÄÅ]",  "A").
        replaceAll("[àáâãäå]",  "a").
        replaceAll("[ÈÉÊË]",    "E").
        replaceAll("[èéêëə]",   "e").
        replaceAll("[ÌÍÎÏ]",    "I").
        replaceAll("[ìíîï]",    "i").
        replaceAll("[ÒÓÔÕÖØ]",  "O").
        replaceAll("[òóôõöøð°]","o").
        replaceAll("[ÙÚÛÜ]",    "U").
        replaceAll("[ùúûü]",    "u").
        replaceAll("[ýÿ]",      "y");
    }

    public static String cleanQuotes(String s) {
        return s.
        // See http://en.wikipedia.org/wiki/Quotation_mark
        replaceAll("[‘’`']+", "'").
        replaceAll("[“”„]+", "\"").
        // See http://en.wikipedia.org/wiki/Guillemets
        replaceAll("‹", "<").
        replaceAll("›", ">").
        replaceAll("«", "<<").
        replaceAll("»", ">>");
    }

    public static String cleanDashes(String s) {
        // See http://en.wikipedia.org/wiki/Dash#Common_dashes
        return s.replaceAll("[\u002d\u2012\u2013\u2014\u2015\u2053]+", "-");
    }

    public static String cleanWeirdWhite(String s) {
        return weirdWhitePattern.matcher(s).replaceAll(" ");
    }

    public static String cleanWeird(String s) {
        return uniJunkPattern.matcher(s).replaceAll("");
    }

    public static String cleanNoTrimWhite(String s) {
        return whitePattern.matcher(cleanWeird(cleanWeirdWhite(s))).replaceAll(" ");
    }

    public static String cleanWhite(String s) {
        return cleanNoTrimWhite(s).trim();
    }

    public static String cleanReferences(String s) {
        return referencePattern.matcher(s).replaceAll(" ");
    }

    public static String getLetters(String s) {
        return nonLetterPattern.matcher(s.toLowerCase()).replaceAll("");
    }

    public static String getLettersDigits(String s) {
        return nonLetterNumberPattern.matcher(s.toLowerCase()).replaceAll("");
    }

    public static String[] splitParagraphs(String s) {
        return paragraphPattern.split(fixNewlines(s));
    }

    private CleanString() {}
}
