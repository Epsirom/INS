package indexing;

import entry.Base;
import entry.Config;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.indexing.parallel.ParallelIndexer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by Epsirom on 14/12/9.
 */
public class LireIndexer {
    private static Logger logger = Base.logger(LireIndexer.class);

    public static void index() throws IOException {
        index(Config.data_path);
    }

    public static void index(String data_path) throws IOException {
        index(8, data_path);
    }

    public static void index(int threads_count, String data_path) throws IOException {
        final String index_path = Config.index_path;
        ParallelIndexer pin = new ParallelIndexer(threads_count, index_path, data_path) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
                builder.addBuilder(new MetadataBuilder());
            }
        };
        Thread t = new Thread(pin);
        logger.info("Start indexing...");
        t.start();
        //while (!pin.hasEnded()) {
            // DO NOTHING
        //}
        try {
            t.join();
            logger.info("Indexing finished.");
        } catch (InterruptedException e) {
            logger.warn("Indexing interrupted.");
        }
    }

    public static IndexReader getReader() throws IOException {
        final String index_path = Config.index_path;
        return DirectoryReader.open(FSDirectory.open(new File(index_path)));
    }
}
