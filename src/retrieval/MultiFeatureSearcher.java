package retrieval;

import entry.Base;
import entry.Config;
import indexing.LireIndexer;
import indexing.MetadataBuilder;
import indexing.SiftKdTreeIndexer;
import net.semanticmetadata.lire.AbstractImageSearcher;
import net.semanticmetadata.lire.ImageDuplicates;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.imageanalysis.*;
import net.semanticmetadata.lire.imageanalysis.joint.JointHistogram;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import net.semanticmetadata.lire.impl.SimpleImageSearchHits;
import net.semanticmetadata.lire.impl.SimpleResult;
import net.sf.javaml.core.kdtree.KDTree;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import tuning.FeatureBuffer;
import tuning.FeatureBufferResult;

import javax.imageio.ImageIO;
import javax.print.Doc;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by Epsirom on 14/12/11.
 */
public class MultiFeatureSearcher extends AbstractImageSearcher {
    private Logger logger = Base.logger(MultiFeatureSearcher.class);
    protected int maxHits = 10;
    protected HashMap<String, Float> featureWeights = new HashMap<String, Float>();
    protected TreeSet<SimpleResult> docs = new TreeSet<SimpleResult>();
    protected float maxDistance = -1.0F;
    public static HashMap<String, Class<?>> featureMap = new HashMap<String, Class<?>>();

    static {
        featureMap.put("featureCEDD", CEDD.class);
        featureMap.put("featureAutoColorCorrelogram", AutoColorCorrelogram.class);
        featureMap.put("descriptorEdgeHistogram", EdgeHistogram.class);
        featureMap.put("descriptorScalableColor", ScalableColor.class);
        featureMap.put("featureFCTH", FCTH.class);
        featureMap.put("featureJCD", JCD.class);
        featureMap.put("featureColorHistogram", SimpleColorHistogram.class);
        featureMap.put("featureTAMURA", Tamura.class);
        featureMap.put("featureGabor", Gabor.class);
        featureMap.put("featureJpegCoeffs", JpegCoefficientHistogram.class);
        featureMap.put("featureJointHist", JointHistogram.class);
        featureMap.put("featOpHist", OpponentHistogram.class);
        featureMap.put("featLumLay", LuminanceLayout.class);
        featureMap.put("featPHOG", PHOG.class);
    }

    public MultiFeatureSearcher(int max_hits) {
        this.maxHits = max_hits;
    }

    public MultiFeatureSearcher(int max_hits, HashMap<String, Float> feature_weights) {
        this.maxHits = max_hits;
        this.featureWeights = feature_weights;
    }

    public void setFeatureWeight(String name, float weight) {
        featureWeights.put(name, weight);
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        Document doc = getDocumentByImage(image);
        return this.search(doc, reader);
    }

    public static Document getDocumentByImage(BufferedImage image) throws IOException {
        MetadataBuilder builder = new MetadataBuilder();
        File outImg = new File("tmp/query_image.jpg");
        ImageIO.write(image, "jpg", outImg);
        return builder.createDocument(image, outImg.getAbsolutePath());
    }

    public static Map<String, LireFeature> getSimpleFeatures(Document doc) throws Exception {
        HashMap<String, LireFeature> features = new HashMap<String, LireFeature>();
        List<IndexableField> fields = doc.getFields();
        for (IndexableField field : fields) {
            String fieldName = field.name();
            if (featureMap.containsKey(fieldName)) {
                BytesRef ref = doc.getField(fieldName).binaryValue();
                if (ref != null && ref.length > 0) {
                    LireFeature f = (LireFeature)featureMap.get(fieldName).newInstance();
                    f.setByteArrayRepresentation(ref.bytes, ref.offset, ref.length);
                    features.put(fieldName, f);
                }
            }
        }
        return features;
    }

    public static KDTree getSiftFeatureTree(Document doc) {
        SiftSearcher searcher = new SiftSearcher(10, "featureSift");
        List<Feature> features = searcher.getSiftFeatures(doc);
        return SiftKdTreeIndexer.buildTreeByFeatures(features);
    }

    public static List<Feature> getSiftFeatures(Document doc) {
        SiftSearcher searcher = new SiftSearcher(10, "featureSift");
        return searcher.getSiftFeatures(doc);
    }

    public static Set<String> search(FeatureBuffer buf, Map<String, Float> weights) throws Exception {
        TreeSet<FeatureBufferResult> docs = new TreeSet<FeatureBufferResult>();
        float maxDistance = -1.0F;
        IndexReader reader = LireIndexer.getReader();
        int numDocs = reader.numDocs();
        for (int i = 0; i < numDocs; ++i) {
            System.out.println(i);
            Document doc = reader.document(i);
            FeatureBuffer buf2 = new FeatureBuffer(doc);
            float dist = getDistance(buf, buf2, weights);
            if (maxDistance < 0) {
                maxDistance = dist;
            }
            if (docs.size() < Config.num_of_results) {
                docs.add(new FeatureBufferResult(dist, buf2, i));
                if (dist > maxDistance) {
                    maxDistance = dist;
                }
            } else if (dist < maxDistance) {
                docs.remove(docs.last());
                docs.add(new FeatureBufferResult(dist, buf2, i));
                maxDistance = docs.last().dist;
            }
        }
        TreeSet<String> result = new TreeSet<String>();
        for (FeatureBufferResult r : docs) {
            result.add(r.buffer.tag);
        }
        return result;
    }

    public static Set<String> search(FeatureBuffer buf, List<FeatureBuffer> bufs, Map<String, Float> weights) {
        TreeSet<FeatureBufferResult> docs = new TreeSet<FeatureBufferResult>();
        float maxDistance = -1.0F;
        int numBufs = bufs.size();
        for (int i = 0; i < numBufs; ++i) {
            FeatureBuffer buf2 = bufs.get(i);
            float dist = getDistance(buf, buf2, weights);
            if (maxDistance < 0) {
                maxDistance = dist;
            }
            if (docs.size() < Config.num_of_results) {
                docs.add(new FeatureBufferResult(dist, buf2, i));
                if (dist > maxDistance) {
                    maxDistance = dist;
                }
            } else if (dist < maxDistance) {
                docs.remove(docs.last());
                docs.add(new FeatureBufferResult(dist, buf2, i));
                maxDistance = docs.last().dist;
            }
        }
        TreeSet<String> result = new TreeSet<String>();
        for (FeatureBufferResult r : docs) {
            result.add(r.buffer.tag);
        }
        return result;
    }

    public static float getDistance(FeatureBuffer buf1, FeatureBuffer buf2, Map<String, Float> weights) {
        float dist = 0.0F;
        for (String key : buf1.simpleFeatures.keySet()) {
            if (weights.containsKey(key)) {
                float w = weights.get(key);
                if (w != 0.0F) {
                    dist += buf1.simpleFeatures.get(key).getDistance(buf2.simpleFeatures.get(key)) * w;
                }
            }
        }
        if (weights.containsKey("featureSift") && weights.get("featureSift") != 0.0F) {
            dist += SiftSearcher.getDistance(buf1.siftFeature, buf2.siftFeature) * weights.get("featureSift");
            //dist += SiftSearcher.getDistanceByKDTree(buf1.siftFeature, buf2.siftTree) * weights.get("featureSift");
        }
        return dist;
    }

    public ImageSearchHits search(Document doc, IndexReader reader) {
        try {
            this.docs.clear();
            this.maxDistance = -1.0F;
            int numDocs = reader.numDocs();
            for (int i = 0; i < numDocs; ++i) {
                Document d = reader.document(i);
                float dist = this.getDistance(doc, d);
                if (this.maxDistance < 0) {
                    this.maxDistance = dist;
                }
                if (this.docs.size() < this.maxHits) {
                    this.docs.add(new SimpleResult(dist, d, i));
                    if (dist > this.maxDistance) {
                        this.maxDistance = dist;
                    }
                } else if (dist < this.maxDistance) {
                    this.docs.remove(this.docs.last());
                    this.docs.add(new SimpleResult(dist, d, i));
                    this.maxDistance = this.docs.last().getDistance();
                }
            }
        } catch (Exception e) {
            logger.error("Search error: {}", e.toString());
            e.printStackTrace();
        }
        SimpleImageSearchHits hits = new SimpleImageSearchHits(this.docs, this.maxDistance);
        return hits;
    }

    public float getDistance(Document doc1, Document doc2) throws Exception {
        float dist = 0.0F;
        List<IndexableField> fields = doc1.getFields();
        for (IndexableField field : fields) {
            String fieldName = field.name();
            if (featureMap.containsKey(fieldName) && featureWeights.containsKey(fieldName)) {
                BytesRef ref1 = doc1.getField(fieldName).binaryValue();
                BytesRef ref2 = doc2.getField(fieldName).binaryValue();
                if (ref1 != null && ref1.length > 0 && ref2 != null && ref2.length > 0) {
                    LireFeature f1 = (LireFeature)featureMap.get(fieldName).newInstance();
                    LireFeature f2 = (LireFeature)featureMap.get(fieldName).newInstance();
                    f1.setByteArrayRepresentation(ref1.bytes, ref1.offset, ref1.length);
                    f2.setByteArrayRepresentation(ref2.bytes, ref2.offset, ref2.length);
                    dist += f1.getDistance(f2) * featureWeights.get(fieldName);
//                    System.out.println(fieldName + ": " + f1.getDistance(f2) * featureWeights.get(fieldName));
                }
            }
        }
        dist += this.getSiftDistance(doc1, doc2);
        return dist;
    }

    public float getSiftDistance(Document doc1, Document doc2) {
        if (featureWeights.containsKey("featureSift") &&
                doc1.getFields("featureSift").length > 0 &&
                doc2.getFields("featureSift").length > 0) {
            SiftSearcher siftSearcher = new SiftSearcher(this.maxHits, "featureSift");
            return SiftSearcher.getDistance(siftSearcher.getSiftFeatures(doc1), siftSearcher.getSiftFeatures(doc2))
                    * featureWeights.get("featureSift");
        } else {
            return 0.0F;
        }
    }

    public ImageDuplicates findDuplicates(IndexReader reader) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
