com.dnikulin.vijil.file (Java)
==============================

Hash.java
---------

Runs Java's built-in SHA-256 for arbitrary byte arrays, then generates a base-62 string of length 24.  Useful for giving objects unique-enough identifiers that sit well in URLs, XML and JSON.


TextPack.java
-------------

Contains simple functions to serialise TextModel arrays to and from byte arrays.  Directly references Google Snappy, and this should be replaced with an interface or injection.


com.dnikulin.vijil.index (Java)
===============================

MatchVisitor.java
-----------------

Interface representing a Visitor analogue for match results between TextModel objects.  The LinkSpanGraph is a powerful and practical MatchVisitor.  Use a Mindex or Stencils to perform searches that feed the visitor.


InvertMatchVisitor.java
-----------------------

Simple MatchVisitor that wraps another MatchVisitor, swapping the order of the two TextModel objects.  This is not necessarily for most reasonable MatchVisitor models, but use it if your custom visitor cares about the order.


MindexCore.java
---------------

Abstract base class containing most details common to Mindex and Stencils.  Implements a simple hash table with exponentially growing parallel array chains.


Mindex.java
-----------

Concrete class for indexing n-grams for a fixed n.  The same functionality may be replicated in Stencils (by having a single skipgram "Stencil" of size n and no skip) but with slightly lower efficiency.


Stencils.java
-------------

Concrete class for indexing skipgrams.  Vijil's concept of skipgrams is designed for ease of user understanding, so skipgrams are phrased as "m of n", such as "4 of 6", meaning that within 6 grams, 4 must match (optionally out of order).


StencilModel.java
-----------------

Concrete class that contains a "model" for skipgram stencils, indicating the different stencils that the index must store and search.


NullMatchVisitor.java
---------------------

Concrete class and singleton that ignores visited matches.


RadixSearch.java
----------------

Slow and possibly incorrect search algorithm conceptually based on Radix Sort.  This showed theoretical promise over Mindex techniques but to date has not been brought up to the same standard.


SlowSearch.java
---------------

Easily verified baseline search algorithm, perhaps useful to compare in unit testing.


Stencilin.java
--------------

Highly experimental parallel search using lockless linked lists.


TextModelTape.java
------------------

Combines several TextModel objects into a single linear tape, useful for creating multi-text radix sort or suffix arrays.



com.dnikulin.vijil.index (Scala)
================================

SearchPair.scala
----------------

High-level function to search between a pair of set of text models.


SearchSet.scala
---------------

High-level function to search within a set of text models.


SearchSplit.scala
-----------------

High-level function to search within a set of text models returning matches M1 in symbol domain, post-process with P the results to M2 in symbol domain, then separately normalise to character domain both M1 and M2 to MN1 and MN2, returning MN1 and MN2.

A concrete example is to perform a multi-text symbol search with SearchSet, and as post-processing, expand the symbol spans with ExpandSymbols, keeping both sets for display purposes.


StencilModels.scala
-------------------

Automatically generates StencilModel objects based on "m of n" definitions, and contains several such definitions for reasonable sizes.


com.dnikulin.vijil.lexer (Java)
===============================

PorterAnalyzer.java
-------------------

Creates a Lucene Analyzer similar to StandardAnalyzer, but adding Lucene's Porter stemmer as a filter.


com.dnikulin.vijil.lexer (Scala)
================================

TextLexer.scala
---------------

Trait capturing the function signature <code>(TextFile => TextModel)</code>.  It is strongly encouraged that all lexers act as pure functions.


CachedLexer.scala
-----------------

A caching wrapper for any TextLexer.


SimpleTextLexer.scala
---------------------

An abstract TextLexer that populates a TextModelBuilder.  Most lexers are built on SimpleTextLexer.


HashLexer.scala
---------------

A SimpleTextLexer that converts words to symbols using their <code>String.hashCode()</code>.  It uses <code>FindSpans.words()</code> to split words, and so may not be correct with respect to Unicode word separation --- try to use LuceneLexer for that.


LuceneLexer.scala
-----------------

A SimpleTextLexer that uses a Lucene analyzer to visit tokens.  By default, the symbols are generated from <code>String.hashCode()</code> just like HashLexer, but using the more advanced analyzer system in Lucene.


LuceneEnglishLexer.scala
------------------------

A LuceneLexer bound to a PorterAnalyzer, slightly more useful for English.


WordListLexer.scala
-------------------

A TextLexer delegating to Words.  Partly for backwards compatibility in Factotum, and does not act as a pure function because Words is mutable.


com.dnikulin.vijil.model (Java)
===============================

Symbols.java
------------

Special symbol values, useful for tapes, though now obsoleted by TextModelTape itself.


TextModel.java
--------------

An immutable parallel array container representing the symbol sequence of a lexed text.  These form the data set used for matching, and contain information necessary to map back from the symbol domain to the character domain.


TextModelBuilder.java
---------------------

A safe builder for TextModel.  The builder is mutable but the returned TextModel objects are immutable.


com.dnikulin.vijil.parse (Java)
===============================

Words.java
----------

A dictionary model used for mapping from words to lemmas, with many words to one lemma.


Lemma.java
----------

A lemma in a dictionary model, stored by Words.


WikiParser.java
---------------

A SAX "Handler" that adapts Wikipedia SAX parsing to a (slightly) higher level interface.


com.dnikulin.vijil.parse (Scala)
================================

Differ.scala
------------

A large and high-level set of functions for computing "diffs" of sequences of spans of strings, including recursive multi-level diffs (e.g. paragraph to sentence to word).


FindSpans.scala
---------------

A collection of functions for finding spans within strings, either by splitting spans by patterns, or finding spans by patterns.  Supports finding paragraphs, sentences, words, with minor but useful variations for each.


ReadFactotumXML.scala
---------------------

Format parser for a primitive XML format used for many texts in Factotum.  Deprecated in favor of ReadTEI.


ReadTEI.scala
-------------

Format parser for TEI P4 and P5 texts, supporting several of the most common tags.  Extracts structure and content of a text, and also adapts some tags into XHTML rendering directives (see NodeSpan).


StringSpan.scala
----------------

Trait and concrete implementation of a Span that is specifically for strings.


StringToText.scala
------------------

Simple attempt at a plaintext parser that will generate a TextFile containing structure at the level of paragraphs.


WordCount.scala
---------------

Small framework for word counting, such as for tf-idf statistics or Bayesian inference.


WordFiles.scala
---------------

Trivial format parsers for trivial file formats, see accompanying <a href="https://github.com/dnikulin/vijil-data">vijil-data</a> repository.



com.dnikulin.vijil.render (Scala)
=================================

AntiRender.scala
----------------

"Un"-renders existing XHTML to produce rendering directives that (should) re-create it.  These can then be mixed with new rendering directives, which in many cases can produce new XHTML that renders correctly.  This is a difficult problem in general and so AntiRender is a first attempt that has shown some promise on simple XHTML.


GetOverlaps.scala
-----------------

Group together spans based on their overlaps with respect to other spans, highly useful in rendering.


NodeSpan.scala
--------------

A rendering directive, declaring that a certain span of the string should be rendered with a certain <code>(NodeSeq => NodeSeq)</code> transformation.  RenderString correctly combines these into a rendering tree that produces XHTML from many disjoint and orthogonal NodeSpans.


NodeWalk.scala
--------------

Simple functions that allow traversal and transformation of Node trees.


RenderNotes.scala
-----------------

Very simple adapter to produce NodeSpans from TextNotes.


RenderString.scala
------------------

Sophisticated recursive algorithm to render XHTML from separate NodeSpans that in general know nothing about each other.  NodeSpans will be rendered for even partial sub-spans so that overlaps are resolved, and where it matters, they may declare a "depth" so that they specifically become sub-spans of other spans of any lower "depth".

Factotum uses RenderString to achieve many rendering effects with very simple and modular code.



com.dnikulin.vijil.result (Java)
================================

CountMatchVisitor.java
----------------------

A MatchVisitor that counts matches and optionally passes them through to another visitor.


LinkSpanGraph.java
------------------

A sophisticated data structure and set of algorithms to merge partial match results into complete match results, retaining links between parts and transitively combining linked matches into "match sets".

This is used heavily in Factotum.  Match results are displayed with links to whole or partial matches in other texts, and with colours and page grid links to the match set as a whole.


LinkSpan.java
-------------

A span that refers to other spans, used as an intermediate result in LinkSpanGraph.


LinkSpanSet.java
----------------

A set of LinkSpans.


SpanDomain.java
---------------

Enum describing the domain of a LinkSpan, such as symbol domain or character domain.


com.dnikulin.vijil.result (Scala)
=================================

ExpandSymbols.scala
-------------------

Utility to loosely expand existing symbol domain LinkSpans to ones of equal or greater length, based on the symbols available within some interval at linked LinkSpans.

Used by Factotum to grow a coloured (loose) highlight around coloured and bolded (strong) matches.


NormaliseSpans.scala
--------------------

Wraps LinkSpanGraph to translate symbol domain spans (in perhaps different symbol domains entirely, such as different lexed models for the same text string) to consistent character domain.  This is a critical step before results can be rendered in a user interface.


com.dnikulin.vijil.scripts (Scala)
==================================

Difftex.scala
-------------

Script to generate a difference report for "Commentaries on the Laws of England", with many concessions made for digitisation damage.


ReadXHTML.scala
---------------

Script to repair digitisation damage done to "Commentaries on the Laws of England", producing Factotum XML files for each of several editions.


com.dnikulin.vijil.store (Scala)
================================

ObjectStore.scala
-----------------

A key-value store for objects.


ByteStore.scala
---------------

An arbitrary key-value storage where the values are byte arrays.


BZip2ByteStore.scala
--------------------

A compressing ByteStore using BZip2 from Apache Commons Compress.


CacheStore.scala
----------------

A cache for any other ObjectStore.


DirectStore.scala
-----------------

An in-memory key-value store.


FileByteStore.scala
-------------------

A byte array store that stores files in a directory.


JsonStore.scala
---------------

A store that serialises and de-serialises objects using lift-json.  The architecture here is not ideal - there are ways in which using lift-json's own format architecture may be simpler (though somewhat slower due to its heavy use of reflection).


SnapByteStore.scala
-------------------

A compressing ByteStore using Google Snappy.


SnappyStack.scala
-----------------

A byte storage stack combining SnapByteStore and TokyoStore.


StoredSet.scala
---------------

A string "set" stored on an arbitrary byte array store (the stored objects are byte arrays of length 0).


TokyoStack.scala
----------------

An object storage stack combining CacheStore, JsonStore, SnapByteStore and TokyoStore.  This is used for stored texts and stored reports in Factotum.


TokyoStore.scala
----------------

Adapts <a href="http://code.google.com/p/tokyotyrant-java/">tokyotyrant-java</a> to act as a ByteStore.  This should be removed, because tokyotyrant-java appears to be unmaintained.


ZipByteStore.scala
------------------

A compressing ByteStore using GZIPOutputStream and GZIPInputStream.


com.dnikulin.vijil.text (Scala)
===============================

SpanPair.scala
--------------

A pair of TextSpans for reporting purposes.  Used in Factotum for in-memory and saved reports, so that matched pairs can be selected by the user for inclusion in a report.


Tag.scala
---------

An arbitrary "key string - value string" tag attached to TextFile or TextSpan.  This should be subsumed by the Rune system.


TextFile.scala
--------------

An entire hierarchical document containing string data, sections, tags, runes, etc.  This should be made to extend TextSpan, as it has a superset of attributes.


TextMap.scala
-------------

An injectable lookup system for texts, that may be useful for switching out text storage and lookup systems.


TextNote.scala
--------------

A footnote included with a text.  This should be subsumed by the Rune system.


TextPage.scala
--------------

Contains the PageBreak rune and algorithms for generating and interpreting page breaks, such as finding subsets of pages touched by matched spans.


TextSpan.scala
--------------

A StringSpan with extra information related to TextFile, such as hierarchical structure and tags.


com.dnikulin.vijil.tools (Java)
===============================

CleanString.java
----------------

Contains several regular expressions and replacement tables that help normalise strings towards forms that are easier to display and parse.  Fixes whitespace (including non-standard character codes that often render to whitespace and so are accidentally used in some files), fancy quotation marks and dashes, several accented letters and diphthongs.  These functions are very useful for developing pre-processing scripts for files in ad-hoc formats, or files in standard formats that were themselves converted faithfully from ad-hoc formats (at least in terms of structure, "word processed" files are effectively ad-hoc and only very slightly more structured than plaintext).


Empty.java
----------

Contains instances of empty (length 0) arrays for all Java primitive types, Objects and Strings.  It was discovered that these are also defined in Apache Commons Lang, so Empty.java contains references to those instances with shorter names.

Because there is nothing mutable in an empty array, these instances are considered immutable singletons and very useful as non-null default values for members of other objects, consistent with functional programming styles (compare to <code>Nil</code> and <code>None</code> in Scala's collection library).

There is also a set of pure functions that will copy arrays but replace null and empty arrays with these empty singletons.


HashInts.java
-------------

Implements a hash that is incompatible with, but offers hashing qualities similar to, <a href="http://sites.google.com/site/murmurhash/">Murmur 3</a>.  There is a unit test that verifies extremely balanced hash collisions, by testing at the tipping point of the birthday paradox.  This hash function has proven extremely valuable in hashing n-grams and skipgrams of integers, and so is used by some of the indexing and search algorithms featured in Vijil.


Integers.java
-------------

Contains a class representing a variable-size array of unboxed integers (contrast to a fixed-size Java array of unboxed integers, or a variable-sized array of boxed integers) with simple utility methods for in-place sorting, removing duplicates, and binary search.


com.dnikulin.vijil.tools (Scala)
================================

ArrSeq.scala
------------

Contains utilities for working with ArraySeq and IndexedSeq instances, closing small gaps in the Scala collection library.

ArraySeq has many useful qualities as an IndexedSeq and more generally a Seq and an Iterable.  It is dense in memory and thus indexable (unlike List), does not require a ClassManifest or runtime reflection (though it does leave checked casts at runtime), and by being simple and shallow in its indirections, it imposes very little overhead on the Java just-in-time compiler and garbage collector.

The usual ArraySeq.empty actually creates a new instance on each invocation, which is extremely wasteful in runtime, memory and garbage collection overhead.  ArrSeq has an immutable <code>emptySeq</code> singleton that is an ArraySeq[Nothing] of size 0.  Because this is also an IndexedSeq[Nothing], and IndexedSeq[T] is covariant with respect to T, it can be the value for essentially any iterable sequence.  This is useful as the default value for IndexedSeq members, as a zero for sequence folding, etc.

There are also object-level <code>apply()</code> functions that construct ArraySeq instances for 1, 2, 3 and 4 items.  As part of Scala's well-factored collection library, ArraySeq cleanly inherits such constructor-functions already.  However, these operate by creating a builder, adding elements, and completing the array from the builder, all of which is far heavier than necessary for constructing small arrays.

Finally there is a simple <code>convert()</code> function that will consume any IterableOnce and produce an ArraySeq.  This may later be complemented with an implicit conversion that gives all collections a .toArraySeq method.


AutoLock.scala
--------------

Contains a simple wrapper for a standard Java read-write lock, allowing the read and write locks to enclose function blocks.  The use of AutoLock is generally discouraged, as higher-level synchronisation concepts are available through actors and software transactional memory libraries, while it remains difficult to verify that locking is used correctly even with medium-level abstractions like function blocks.


Deadline.scala
--------------

Enables a code block to be executed with guarantees, either after a certain elapsed period (the ``deadline''), or an earlier explicit invocation.  This abstracts over several Java concurrency primitives and creates its own private work thread.  It is not used in the current Factotum feature set, but may be very useful for more advanced interactive subsystems. <b>Vestigial.</b>


Histogram.scala
---------------

Trivial function to compute a histogram for an arbitrary collection of objects that are hashable. <b>Vestigial.</b>


MinCostEdit.scala and CostModel.scala
-------------------------------------

Contains a trait for unit and pairwise cost modeling, and a dynamic programming minimum-cost edit calculator using these traits.  Several reusable cost models are provided, and unit tests are included to both verify and illustrate the operation of the edit calculator.  This has proven very useful in advanced inexact ``diff'' systems.


ObjectCache.scala
-----------------

Wraps an arbitrary pure function with a single argument with a cache, similar to memoization.  Uses Google Guava's ``computing map'' facility, though this will be retired in favour of a more specialised caching system in future versions of Guava.


TimeString.scala
----------------

Cleanly formats standard time strings, useful for creating text and report tags at the time of creation. <b>Vestigial.</b>


TryOrNone.scala
---------------

For a function that returns an Option, this will invoke that function but trap and translate exceptions to None.  TryTraced (in the same file) does the same thing, but prints the exception before discarding it, helping debugging.  It is not unreasonable to want to ignore exceptions, as they may occur due to rejected input in libraries such as lift-json.

See also Lift's tryo() helper, which does something similar but more awkward.  tryo() will take any function and wrap the result in Some or None, but the only way to return None is to throw an exception, i.e.\ the function given cannot return None (it it tries to, the result will actually be Some(None) which then has to be flattened out to None again).


WorkerPool.scala
----------------

Contains a class implementing a background worker thread pool, to which tasks may be scheduled, including parallel processing tasks.  This is far less valuable now that Parallel Collections are part of the Scala standard library, though in some circumstances WorkerPool may be more efficient or result in simpler code.


XmlTools.scala
--------------

Contains a very small set of functions that wrap ugly XML handling behind simpler function names and signatures.  The most important offering is a <code>readNode()</code> function that will adapt a very safe and fast SAX parser configuration to parse from a byte array, with slow/exploitable features like DTD validation disabled.  Exceptions are absorbed to return None.  This is useful as an input step to get the XML DOM before a more confident processing algorithm interprets the structure and content.


com.dnikulin.vijil.traits (Scala)
=================================

HasData.scala
-------------

Specifies that an object ``has data'' of a given type T.  This is mostly used for StringSpan and therefore TextSpan.


HasHash.scala and KeyedByHash.scala
-----------------------------------

Specifies that an object ``has a hash'' which is a plaintext string.  If it is also KeyedByHash, then the Hash is used for toString(), equals(), hashCode() and compare().


HasMarks.scala
--------------

Specifies that an object ``has marks'' which are rendering directives (NodeSpan instances).  This may be replaced with the Rune model later.


HasTexts.scala
--------------

Specifies that an object ``has texts''.  It is not required to list its texts, only to respond to queries for a text with a certain hash (and as a convenience, for more advanced strings that also specify a page number or span position).


Rune.scala
----------

The rune trait represents an arbitrary, optional marking for an object - in Vijil the host object would be a text or text span.  Runes may be added and interpreted arbitrarily, allowing freestyle extension of the information associated with a text.  Scala pattern matching is a convenient way to extract appropriate runes and ignore all others.

The rune metaphor is used to suggest that runes are meaningful to a specific sub-system and completely opaque to all others.


Span.scala
----------

An integer interval over some data.  See StringSpan and TextSpan.


ToJson.scala
------------

Has ToJson trait (for instances) and FromJson trait (for companion objects or specialised parsers).  A simple way to translate objects to and from JSON without using lift-json's "format" architecture.


ToNode.scala
------------

Has ToNode trait (for instances) and FromNode trait (for companion objects or specialised parsers).  A simple way to translate objects to and from XML.


