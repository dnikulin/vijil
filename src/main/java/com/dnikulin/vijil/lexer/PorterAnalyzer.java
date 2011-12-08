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

package com.dnikulin.vijil.lexer;

import java.io.Reader;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

public class PorterAnalyzer extends ReusableAnalyzerBase {
    public static final Version VERSION = Version.LUCENE_35;

    public static final int MAX_TOKEN_LENGTH = 255;

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        // Build standard tokenizer with configured version.
        StandardTokenizer src = new StandardTokenizer(VERSION, reader);
        src.setMaxTokenLength(MAX_TOKEN_LENGTH);

        // Build token stream pipeline.
        TokenStream tok = new StandardFilter(VERSION, src);
        tok = new LowerCaseFilter(VERSION, tok);
        tok = new PorterStemFilter(tok);
        return new TokenStreamComponents(src, tok);
    }
}
