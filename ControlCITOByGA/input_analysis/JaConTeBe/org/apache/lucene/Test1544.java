package org.apache.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.LuceneTestCase240;
import org.apache.lucene.util._TestUtil293;

import edu.illinois.jacontebe.Helpers;
import edu.illinois.jacontebe.framework.Reporter;

/**
 * 
 * Bug URL:https://issues.apache.org/jira/browse/LUCENE-1544 
 * This is a wait-notify deadlock.
 * Reproduce environment: lucene 2.4.0, Java 1.6.0_33
 * 
 * @author Doug Sale
 * @collector Ziyi Lin
 *
 */
public class Test1544 extends LuceneTestCase240 {

    private String hotIndexPath;
    private String coldIndexPath;

    // not important
    private int hotDocCount = 0;

    // as long as cold doc count is greater than
    // IndexWriter.getMaxBufferedDocs(), then
    // IndexWriter.AddIndexes(IndexReader[])
    // will optimize prior to adding indexes
    // and the optimize will deadlock
    private int coldDocCount = 12;

    public void setUp() throws Exception {
        super.setUp();

        // String tmpPath = System.getProperty("tempDir", "");
        String tmpPath = System.getProperty("user.dir");
        coldIndexPath = new java.io.File(tmpPath, "TestDoug2-cold")
                .getCanonicalPath();
        hotIndexPath = new java.io.File(tmpPath, "TestDoug2-hot")
                .getCanonicalPath();

        // in case test thread is killed and tearDown() was never called
        if (new java.io.File(coldIndexPath).exists())
            _TestUtil293.rmDir(coldIndexPath);
        if (new java.io.File(hotIndexPath).exists())
            _TestUtil293.rmDir(hotIndexPath);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        _TestUtil293.rmDir(coldIndexPath);
        _TestUtil293.rmDir(hotIndexPath);
    }

    public void testAddIndexesByIndexReader() throws Exception {
       
        int timeout = 30;
        Reporter.reportStart("lucene1544", timeout, "deadlock");
        Helpers.startWaitingMonitor(timeout);
        createIndex(coldIndexPath, 100000, coldDocCount);
        createIndex(hotIndexPath, 200000, hotDocCount);

        IndexWriter writer = getWriter(coldIndexPath);
        writer.setInfoStream(System.out);

        IndexReader[] readers = new IndexReader[] { IndexReader
                .open(hotIndexPath) };

        try {
            writer.addIndexes(readers);
        } finally {
            writer.close();
            readers[0].close();
        }

        _TestUtil293.checkIndex(FSDirectory.getDirectory(coldIndexPath));

        IndexReader reader = IndexReader.open(coldIndexPath);
        assertEquals("Index should contain " + (coldDocCount + hotDocCount)
                + "{0} documents.", coldDocCount + hotDocCount,
                reader.numDocs());
        reader.close();
        Reporter.reportEnd(false);
    }

    private IndexWriter getWriter(String directory) throws Exception {
        // Note that the deadlock only occurs when autocommit == true
        // (deprecated constructor)
        IndexWriter writer = new IndexWriter(directory, new StandardAnalyzer());
        writer.setMaxBufferedDocs(coldDocCount - 1);
        return writer;
    }

    private void createIndex(String path, int baseId, int count)
            throws Exception {
        IndexWriter writer = getWriter(path);
        for (int i = 1; i <= count; i++)
            writer.addDocument(createDocument(baseId + i));
        writer.close();
    }

    private Document createDocument(int id) {
        Document doc = new Document();
        doc.add(new Field("field", id + "", Field.Store.YES,
                Field.Index.ANALYZED, Field.TermVector.NO));
        return doc;
    }
}