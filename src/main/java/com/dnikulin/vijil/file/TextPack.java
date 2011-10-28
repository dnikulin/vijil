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

package com.dnikulin.vijil.file;

import java.io.*;

import com.google.common.io.Files;

import org.xerial.snappy.Snappy;
import org.xerial.snappy.SnappyException;

import com.dnikulin.vijil.model.TextModel;
import com.dnikulin.vijil.tools.Empty;

public final class TextPack {
    public static byte[] write(TextModel[] texts, boolean full) throws IOException, SnappyException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        out.writeInt(texts.length);
        out.writeByte(full ? 1 : 0);

        for (TextModel text : texts) {
            out.writeUTF(text.hash);

            final byte[] meta = text.meta();
            out.writeInt(meta.length);
            out.write(meta);

            out.writeInt(text.size);
            for (int i = 0; i < text.size; i++)
                out.writeInt(text.symbol(i));

            if (full) {
                for (int i = 0; i < text.size; i++)
                    out.writeInt(text.offset(i));
                for (int i = 0; i < text.size; i++)
                    out.writeByte((byte) text.length(i));
            }
        }

        out.flush();
        byte[] plain = bytes.toByteArray();
        return Snappy.compress(plain);
    }

    public static TextModel[] read(byte[] bytes) throws IOException, SnappyException {
        // Basic sanity checks.

        if (bytes == null)
            return TextModel.none;

        if (bytes.length < 100)
            return TextModel.none;

        if (Snappy.isValidCompressedBuffer(bytes, 0, bytes.length) == false)
            return TextModel.none;

        // Full in-memory decompression and decoding.

        byte[] plain = Snappy.uncompress(bytes);

        ByteArrayInputStream stream = new ByteArrayInputStream(plain);
        DataInputStream in = new DataInputStream(stream);

        final int ntexts = in.readInt();
        final boolean full = in.readByte() > 0;

        TextModel[] texts = new TextModel[ntexts];
        for (int ntext = 0; ntext < ntexts; ntext++) {
            final String hash = in.readUTF();

            // Start with empty sentinel arrays.
            // Replaced for any non-empty stored data.
            int  [] lemmas  = Empty.ints;
            int  [] offsets = Empty.ints;
            byte [] lengths = Empty.bytes;
            byte [] meta    = Empty.bytes;

            final int nmeta = in.readInt();
            if (nmeta > 0) {
                meta = new byte[nmeta];
                in.readFully(meta);
            }

            final int nentries = in.readInt();
            if (nentries > 0) {
                lemmas = new int[nentries];
                for (int i = 0; i < nentries; i++)
                    lemmas[i] = in.readInt();

                if (full) {
                    offsets = new int[nentries];
                    for (int i = 0; i < nentries; i++)
                        offsets[i] = in.readInt();

                    lengths = new byte[nentries];
                    for (int i = 0; i < nentries; i++)
                        lengths[i] = in.readByte();
                }
            }

            texts[ntext] = new TextModel(hash, lemmas, offsets, lengths, meta);
        }

        in.close();

        return texts;
    }

    public static TextModel[] read(File file) throws IOException, SnappyException {
        byte[] bytes = Files.toByteArray(file);
        return read(bytes);
    }

    private TextPack() {}
}
