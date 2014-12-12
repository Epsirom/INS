package tuning;

import indexing.SiftKdTreeIndexer;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import net.sf.javaml.core.kdtree.KDTree;
import org.apache.lucene.document.Document;
import retrieval.LireSearcher;
import retrieval.MultiFeatureSearcher;
import retrieval.SiftSearcher;

import java.util.List;
import java.util.Map;

/**
 * Created by Epsirom on 14/12/11.
 */
public class FeatureBuffer {

    public Map<String, LireFeature> simpleFeatures;
    //public KDTree siftTree;
    public List<Feature> siftFeature;
    public String tag;

    public FeatureBuffer(Document doc) throws Exception {
        this.tag = LireSearcher.outputFormat(doc.getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        this.simpleFeatures = MultiFeatureSearcher.getSimpleFeatures(doc);
        SiftSearcher searcher = new SiftSearcher(10, "featureSift");
        this.siftFeature = searcher.getSiftFeatures(doc);
        //this.siftTree = SiftKdTreeIndexer.buildTreeByFeatures(this.siftFeature);
    }
}
