package indexing;

import entry.Config;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import net.semanticmetadata.lire.indexing.LireCustomCodec;
import net.semanticmetadata.lire.utils.LuceneUtils;
import net.sf.javaml.core.kdtree.KDTree;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import retrieval.SiftSearcher;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Epsirom on 14/12/11.
 */
public class SiftKdTreeIndexer {

    public static void reindexSiftFeature(String featureName, String newName) throws IOException {
        IndexReader reader = LireIndexer.getReader();
        IndexWriterConfig config = new IndexWriterConfig(LuceneUtils.LUCENE_VERSION, new StandardAnalyzer(LuceneUtils.LUCENE_VERSION));
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        config.setCodec(new LireCustomCodec());
        IndexWriter writer = new IndexWriter(FSDirectory.open(new File(Config.index_path)), config);
        SiftSearcher searcher = new SiftSearcher(Config.num_of_results, featureName);
        int numDocs = reader.numDocs();
        for (int i = 0; i < numDocs; ++i) {
            Document doc = reader.document(i);
            List<Feature> features = searcher.getSiftFeatures(doc);

        }
    }

    public static KDTree buildTreeByFeatures(List<Feature> features) {
        KDTree tree = new KDTree(features.get(0).descriptor.length);
//        int numFeatures = features.size();
        for (Feature f : features) {
            tree.insert(f.descriptor, f);
        }
//        for (int i = 0; i < numFeatures; ++i) {
//            tree.insert(features.get(i).descriptor, i);
//        }
        return tree;
    }
}
