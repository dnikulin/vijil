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

package com.dnikulin.vijil.parse;

import java.util.ArrayList;
import java.util.HashMap;

import com.dnikulin.vijil.model.Symbols;
import com.dnikulin.vijil.model.TextModelBuilder;
import com.dnikulin.vijil.model.TextModel;

public final class Words {
    /** True iff the dictionary will automatically create requested lemmas. */
    public  final boolean                  isFilling;

    private final TextModelBuilder         builder;
    private final ArrayList<Lemma>         lemmaList;
    private final HashMap<String, Lemma>   lemmas;

    private int nextSynset;

    public Words(boolean isFilling) {
        this.isFilling  = isFilling;
        this.builder    = new TextModelBuilder();
        this.lemmaList  = new ArrayList<Lemma>();
        this.lemmas     = new HashMap<String, Lemma>();
        this.nextSynset = 0;

        // Fill sentinel area with dummy lemmas.
        for (int code = 0; code <= Symbols.offset; code++)
            lemmaList.add(new Lemma(code));
    }

    public Words() {
        this(false); // Non-filling by default.
    }

    public synchronized Lemma startLemma() {
        final int   code  = lemmaList.size();
        final Lemma lemma = new Lemma(code);
        lemmaList.add(lemma);
        return lemma;
    }

    public synchronized int startSynset() {
        return ++nextSynset;
    }

    public synchronized void addWords(String[] words) {
        final Lemma lemma = startLemma();

        for (String word : words) {
            lemmas.put(word, lemma);
        }
    }

    public synchronized void addExtraWord(String word) {
        if (lemmas.containsKey(word))
            return;

        final Lemma lemma = startLemma();
        lemmas.put(word, lemma);
    }

    public synchronized void markStopword(String word) {
        final Lemma lemma  = lemmas.get(word);
        if (lemma != null)
            lemma.stopword = true;
    }

    public synchronized Lemma lemma(String word) {
        // Attempt to find existing lemma.
        Lemma lemma = lemmas.get(word);

        // Create lemma if in filling mode.
        if (isFilling && (lemma == null)) {
            lemma = startLemma();
            lemmas.put(word, lemma);
        }

        return lemma;
    }

    public synchronized Lemma lemma(int code) {
        if (code >= lemmaList.size())  return null;
        return lemmaList.get(code);
    }

    public synchronized TextModel parse(String data, String hash, byte[] meta) {
        final int length = data.length();

        boolean inWord = false;

        builder.reset();

        for (int i = 0, offset = 0; i <= length; i++) {
            assert (i >= offset);

            // Take character from text, or simulate whitespace.
            final char ch = (i < length) ? data.charAt(i) : ' ';

            if (Character.isLetter(ch) || Character.isDigit(ch)) {
                if (inWord == false) {
                    inWord = true;
                    offset = i;
                }
            } else {
                if (inWord == true) {
                    inWord = false;

                    // Calculate word length.
                    final int span = i - offset;

                    // Extract word.
                    final String word = data.substring(offset, i).toLowerCase();

                    // Find or create lemma.
                    final Lemma lemma = lemma(word);
                    if (lemma == null)
                        continue;

                    // Check if lemma is a stopword.
                    if (lemma.stopword == true)
                        continue;

                    // Record lemma symbol and span.
                    builder.add(lemma.symbol, offset, (byte) span);
                }
            }
        }

        return builder.finish(hash, meta);
    }
}
