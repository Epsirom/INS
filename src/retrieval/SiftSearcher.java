package retrieval;

import entry.Base;
import indexing.CorrectSiftFeature;
import net.semanticmetadata.lire.AbstractImageSearcher;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageDuplicates;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.imageanalysis.sift.Extractor;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import net.semanticmetadata.lire.impl.SimpleImageSearchHits;
import net.semanticmetadata.lire.impl.SimpleResult;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Epsirom on 14/12/10.
 */
public class SiftSearcher extends AbstractImageSearcher {
    private Logger logger = Base.logger(SiftSearcher.class);
    protected TreeSet<SimpleResult> docs = new TreeSet<SimpleResult>();
    private int maxHits = 10;
    private float maxDistance = -1.0F;
    private String fieldName = "featureSift";
    private Extractor extractor = new Extractor();

    public SiftSearcher(int max_hits, String field_name) {
        this.maxHits = max_hits;
        this.fieldName = field_name;
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) {
        try {
            this.logger.info("Start searching by sift features...");
            List<Feature> features = this.extractor.computeSiftFeatures(image);
            System.out.println(
                    this.getDistance(
                            features,
                            this.extractor.computeSiftFeatures(ImageIO.read(new FileInputStream("dataset/00008.jpg")))
                    )
            );
            return this.search(features, reader);
        } catch (Exception e) {
            this.logger.error("Search by sift failed: {}", e.toString());
        }
        return null;
    }

    public ImageSearchHits search(List<Feature> features, IndexReader reader) {
        try {
            this.docs.clear();
            this.maxDistance = -1.0F;
            int numDocs = reader.numDocs();
            for (int i = 0; i < numDocs; ++i) {
                Document doc = reader.document(i);
                float dist = this.getDistance(features, this.getSiftFeatures(doc));
                System.out.println("Document " + doc.getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue() + ", distance " + dist);
                if (this.maxDistance < 0) {
                    this.maxDistance = dist;
                }
                if (this.docs.size() < this.maxHits) {
                    this.docs.add(new SimpleResult(dist, doc, i));
                    if (dist > this.maxDistance) {
                        this.maxDistance = dist;
                    }
                } else if (dist < this.maxDistance) {
                    this.docs.remove(this.docs.last());
                    this.docs.add(new SimpleResult(dist, doc, i));
                    this.maxDistance = this.docs.last().getDistance();
                }
            }
        } catch (Exception e) {

        }
        SimpleImageSearchHits hits = new SimpleImageSearchHits(this.docs, this.maxDistance);
        return hits;
    }

    protected float getDistance(List<Feature> f1, List<Feature> f2) {
        int matchCount = 0;
        //float sum_dist = 0;
        for (Feature f : f1) {
            float dist = this.getDistance(f, f2);
            if (dist > 0) {
                ++matchCount;
                //sum_dist += dist;
            }
        }
        //return sum_dist;
        return f1.size() - matchCount;
    }

    protected float getDistance(Feature f, List<Feature> fs) {
        float minDist = -1.0F, min2Dist = -1.0F;
        for (Feature ff : fs) {
            float dist = f.getDistance(ff);
            //float dist = this.getDistance(f, ff);
            if (minDist < 0 || dist < minDist) {
                min2Dist = minDist;
                minDist = dist;
            } else if (min2Dist < 0 || dist < min2Dist) {
                min2Dist = dist;
            }
        }
        float ratio = 0.8F;
        //ratio *= ratio;
        if (minDist < min2Dist * ratio) {
            return minDist;
        } else {
            return -1.0F;
        }
    }

    protected float getDistance(Feature f1, Feature f2) {
        double[] d1 = f1.descriptor;
        double[] d2 = f2.descriptor;
        double dist = 0;
        for (int i = 0; i < d1.length; ++i) {
            double delta = d1[i] * f1.scale - d2[i] * f2.scale;
            dist += (delta * delta);
        }
        return (float)dist;
    }

    public List<Feature> getSiftFeatures(Document doc) {
        IndexableField[] fields = doc.getFields(this.fieldName);
        LinkedList<Feature> features = new LinkedList<Feature>();
        for (IndexableField field : fields) {
            Feature f = new Feature();
            BytesRef ref = field.binaryValue();
            CorrectSiftFeature.setByteArrayRepresentation(f, ref.bytes, ref.offset, ref.length);
            //f.setByteArrayRepresentation(ref.bytes, ref.offset, ref.length);
            features.add(f);
        }
        return features;
    }

    public ImageSearchHits search(Document document, IndexReader reader) {
        return this.search(this.getSiftFeatures(document), reader);
    }

    public ImageDuplicates findDuplicates(IndexReader reader) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

}
