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

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

enum WikiState {
    NONE,
    TITLE,
    TEXT
}

public class WikiParser extends DefaultHandler {
    public static final int minChars = 400;

    private WikiState state = WikiState.NONE;
    private String _title = "";
    private StringBuilder _text = new StringBuilder();

    public void haveText(String title, String text) {
        // Override this.
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (qName.equalsIgnoreCase("title")) {
            state = WikiState.TITLE;
            _title = "";
            _text.setLength(0);
        } else if (qName.equalsIgnoreCase("text")) {
            state = WikiState.TEXT;
            _text.setLength(0);
        } else {
            state = WikiState.NONE;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (state) {
            case TITLE:
                _title += new String(ch, start, length);
                break;

            case TEXT:
                _text.append(ch, start, length);
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("text")) {
            if ((_title.length() > 0) && (_text.length() >= minChars))
                haveText(_title, _text.toString());
            _title = "";
            _text.setLength(0);
        }
        state = WikiState.NONE;
    }
}
