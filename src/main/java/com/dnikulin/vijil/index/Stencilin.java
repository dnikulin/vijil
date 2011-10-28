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

package com.dnikulin.vijil.index;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import com.dnikulin.vijil.model.TextModel;
import com.dnikulin.vijil.tools.Empty;
import com.dnikulin.vijil.tools.HashInts;

/** Software device for substring stencil matching within sets of texts.
 *
 * @author Dmitri Nikulin
 */
public class Stencilin {
    protected static final int         nbins   = (1 << 20);
    protected static final int         binmask = (nbins - 1);
    protected static final int         none    = -1;
    protected static final int         nchunk  = 16;
    protected static final int         maxlink = 1000;

    public    final int                nworkers;
    protected final Executor           workers;

    protected final AtomicIntegerArray heads;
    protected final AtomicInteger      cursor;
    protected final ArrayList<FutureTask<Object>> tasks;

    protected       TextModel []        texts;
    protected       StencilModel       model;
    protected       MatchVisitor       callback;
    protected       int                nlemmas;
    protected       int                nstencils;
    protected       int []             lemmas;
    protected       int []             itexts;
    protected       int []             ilemms;
    protected       int []             links;
    protected       int []             offsets;

    protected final Callable<Object> linker = new Callable<Object>() {
        @Override
        public Object call() throws Exception {
            makeLinks();
            return null;
        }
    };

    protected final Callable<Object> reader = new Callable<Object>() {
        @Override
        public Object call() throws Exception {
            readLinks();
            return null;
        }
    };

    public Stencilin() {
        this(16);
    }

    public Stencilin(int nworkers) {
        this.nworkers  = nworkers;

        this.workers   = Executors.newFixedThreadPool(nworkers);
        this.heads     = new AtomicIntegerArray(nbins);
        this.cursor    = new AtomicInteger(0);
        this.tasks     = new ArrayList<FutureTask<Object>>();

        this.texts     = null;
        this.model     = null;
        this.callback  = null;
        this.nlemmas   = 0;
        this.nstencils = 0;
        this.lemmas    = Empty.ints;
        this.itexts    = Empty.ints;
        this.ilemms    = Empty.ints;
        this.links     = Empty.ints;
        this.offsets   = Empty.ints;
    }

    public synchronized void search(TextModel[] texts, StencilModel model, MatchVisitor each) {
        this.texts     = texts;
        this.model     = model;
        this.nstencils = model.nstencils;
        this.callback  = each;

        long time1 = System.currentTimeMillis();

        resize(texts);
        allocate();
        fill(texts);

        long time2 = System.currentTimeMillis();

        // Reset all bin heads.
        for (int ibin = 0; ibin < nbins; ibin++)
            heads.set(ibin, none);

        // Reset lemma cursor.
        cursor.set(0);

        // Reset task list.
        tasks.clear();

        long time3 = System.currentTimeMillis();

        // Create futures to run linker on each worker thread.
        for (int i = 0; i < nworkers; i++) {
            FutureTask<Object> task = new FutureTask<Object>(linker);
            workers.execute(task);
            tasks.add(task);
        }

        // Wait for all linkers.
        for (FutureTask<Object> task : tasks)
            join(task);

        long time4 = System.currentTimeMillis();

        // Reset task list.
        tasks.clear();

        // Reset bin cursor.
        cursor.set(0);

        // Create futures to run reader on each worker thread.
        for (int i = 0; i < nworkers; i++) {
            FutureTask<Object> task = new FutureTask<Object>(reader);
            workers.execute(task);
            tasks.add(task);
        }

        // Wait for all readers.
        for (FutureTask<Object> task : tasks)
            join(task);

        long time5 = System.currentTimeMillis();

        // Reset task list.
        tasks.clear();

        // Release object references.
        this.texts    = null;
        this.model    = null;
        this.callback = null;

        System.out.println(String.format("%9d ms populating", (time2 - time1)));
        System.out.println(String.format("%9d ms linking",    (time4 - time3)));
        System.out.println(String.format("%9d ms reading",    (time5 - time4)));
    }

    private int resize(TextModel[] texts) {
        nlemmas = 0;
        for (TextModel text : texts)
            nlemmas += text.size;
        return nlemmas;
    }

    private void allocate() {
        if (nlemmas > lemmas.length) {
            lemmas  = new int[nlemmas];
            itexts  = new int[nlemmas];
            ilemms  = new int[nlemmas];
        }

        int nlinks = (nlemmas * nstencils);
        if (nlinks > links.length) {
            links   = new int[nlinks];
            offsets = new int[nlinks];
        }

        Arrays.fill(links,   none);
        Arrays.fill(offsets, none);
    }

    private void fill(TextModel[] texts) {
        for (int itext = 0, offset = 0; itext < texts.length; itext++) {
            TextModel text = texts[itext];
            for (int ilemma = 0; ilemma < text.size; ilemma++, offset++) {
                // Pre-hash lemma code.
                int k = text.symbol(ilemma);
                k *= 0xcc9e2d51;
                k  = Integer.rotateLeft(k, 15);
                k *= 0x1b873593;

                // Record pre-hash and offsets.
                lemmas [offset] = k;
                itexts [offset] = itext;
                ilemms [offset] = ilemma;
            }
        }
    }

    private int hash(int[] data) {
        int h = 0xbcaa747;

        for (int i = 0; i < model.size; i++) {
            h ^= data[i];
            h  = Integer.rotateLeft(h, 13);
            h  = ((h * 5) + 0xe6546b64);
        }

        return HashInts.fmix(h);
    }

    protected void makeLinks() {
        // Take final references to arrays,
        // helping the JVM JIT considerably.
        final int          mynlemmas = nlemmas;
        final StencilModel mymodel   = model;
        final int      []  mylinks   = links;
        final int      []  myoffsets = offsets;

        // Produce a TextBody from the current data.
        final TextModel text = new TextModel("stencilin", lemmas);

        // Prepare stencil sample buffer.
        final int [] buffer = new int[model.size];

        while (true) {
            final int ilemm0 = cursor.getAndAdd(nchunk);
            if (ilemm0 >= mynlemmas)
                return;

            final int ilemm1 = Math.min(ilemm0 + nchunk, mynlemmas);

            for (int ilemm = ilemm0; ilemm < ilemm1; ilemm++) {
                final int ilink0 = ilemm * nstencils;

                for (int istencil = 0; istencil < nstencils; istencil++) {
                    if ((ilemm + mymodel.width(istencil)) > mynlemmas)
                        continue;

                    // Sample lemmas using stencil.
                    mymodel.sample(buffer, text, ilemm, istencil);

                    // Hash buffered data.
                    final int hash = hash(buffer);

                    // Calculate bin index.
                    final int ibin = hash & binmask;

                    // Index of "new" link, based only on lemma and stencil indices.
                    final int ilinkN  = ilink0 + istencil;

                    // Index of "old" link, swapped atomically.
                    final int ilinkP  = heads.getAndSet(ibin, ilinkN);

                    // Populate link memory.
                    mylinks  [ilinkN] = ilinkP;
                    myoffsets[ilinkN] = ilemm;
                }
            }
        }
    }

    protected void readLinks() {
        // Take final references to arrays,
        // helping the JVM JIT considerably.
        final TextModel [] mytexts   = texts;
        final int      [] myitexts  = itexts;
        final int      [] myilemms  = ilemms;
        final int      [] mylinks   = links;
        final int      [] myoffsets = offsets;
        final MatchVisitor each     = callback;
        final StencilModel mymodel  = model;

        final int [] mtexts = new int[maxlink];
        final int [] mlemms = new int[maxlink];
        final int [] mstens = new int[maxlink];

        while (true) {
            final int ibin0 = cursor.getAndAdd(nchunk);
            if (ibin0 >= nbins)
                return;

            final int ibin1 = Math.min(ibin0 + nchunk, nbins);
            for (int ibin = ibin0; ibin < ibin1; ibin++) {
                int ilink = heads.get(ibin);
                int count = 0;

                while ((count < maxlink) && (ilink != none)) {
                    // Take lemma offset from chain.
                    final int ioffset = myoffsets[ilink];

                    // Buffer position.
                    mtexts[count] = myitexts[ioffset];
                    mlemms[count] = myilemms[ioffset];
                    mstens[count] = (ioffset % nstencils);
                    count++;

                    // Update cursor to next link.
                    ilink = mylinks[ilink];
                }

                // Ignore underly or overly matched bins.
                if ((count < 2) || (count >= maxlink))
                    continue;

                for (int i1 = 0; i1 < count; i1++) {
                    final int      itext1 = mtexts[i1];
                    final int      ilemm1 = mlemms[i1];
                    final int      isten1 = mstens[i1];

                    final TextModel text1  = mytexts[itext1];
                    final int      nlemm1 = mymodel.width(isten1);

                    // Check bounds within text1.
                    if ((ilemm1 + nlemm1) > text1.size)
                        continue;

                    // Take first and last lemma of text1.
                    final int      lemm1F = text1.symbol(ilemm1);
                    final int      lemm1L = text1.symbol(ilemm1 + nlemm1 - 1);

                    for (int i2 = i1 + 1; i2 < count; i2++) {
                        final int      itext2 = mtexts[i2];

                        // Don't match within the same text.
                        if (itext1 == itext2)
                            continue;

                        final TextModel text2  = mytexts[itext2];
                        final int      ilemm2 = mlemms[i2];
                        final int      isten2 = mstens[i2];
                        final int      nlemm2 = mymodel.width(isten2);

                        // Check bounds within text2.
                        if ((ilemm2 + nlemm2) > text2.size)
                            continue;

                        // Take first and last lemma of text2.
                        final int      lemm2F = text2.symbol(ilemm2);
                        final int      lemm2L = text2.symbol(ilemm2 + nlemm2 - 1);

                        if ((lemm1F == lemm2F) && (lemm1L == lemm2L)) {
                            each.matched(text1, text2, ilemm1, ilemm2, nlemm1, nlemm2);
                            each.matched(text2, text1, ilemm2, ilemm1, nlemm2, nlemm1);
                        }
                    }
                }
            }
        }
    }

    private static void join(FutureTask<Object> task) {
        while (true) {
            try {
                task.get();
                return;
            } catch (ExecutionException ex) {
                ex.printStackTrace();
                return;
            } catch (InterruptedException ex) {
                // Continue to wait.
            }
        }
    }
}
