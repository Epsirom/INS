package tuning;

import entry.Config;
import indexing.LireIndexer;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import retrieval.LireSearcher;
import retrieval.MultiFeatureSearcher;
import retrieval.SiftSearcher;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Epsirom on 14/12/12.
 */
public class DistanceManager {
    public HashMap<String, HashMap<String, HashMap<String, Float>>> distance = new HashMap<String, HashMap<String, HashMap<String, Float>>>();

    public void setDistance(String query, String dataTag, String featureName, float dist) {
        if (!distance.containsKey(query)) {
            distance.put(query, new HashMap<String, HashMap<String, Float>>());
        }
        if (!distance.get(query).containsKey(dataTag)) {
            distance.get(query).put(dataTag, new HashMap<String, Float>());
        }
        distance.get(query).get(dataTag).put(featureName, dist);
    }

    public float getDistance(String query, String dataTag, String featureName) {
        if (!distance.containsKey(query)) {
            return -1.0F;
        } else if (!distance.get(query).containsKey(dataTag)) {
            return -1.0F;
        } else if (!distance.get(query).get(dataTag).containsKey(featureName)) {
            return -1.0F;
        } else {
            return distance.get(query).get(dataTag).get(featureName);
        }
    }

    public boolean autoComplete() {
        boolean needSave = false;
        for (String query : GeneticTuning.qoi) {
            System.out.println("Auto complete query: " + query);
            FeatureBuffer buffer = GeneticTuning.getROI(query);
            try {
                IndexReader reader = LireIndexer.getReader();
                int numDocs = reader.numDocs();
                for (int i = 0; i < numDocs; ++i) {
//                    System.out.print(i + " ");
                    Document doc = reader.document(i);
                    String dataTag = LireSearcher.outputFormat(doc.getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
//                    System.out.println("Data tag: " + dataTag);
                    for (String featureName : GeneticItem.featureNames) {
                        if (getDistance(query, dataTag, featureName) < 0.0F) {
                            FeatureBuffer buf = new FeatureBuffer(doc);
                            if (!featureName.equals("featureSift") && !buf.simpleFeatures.containsKey(featureName)) {
                                continue;
                            } else if (featureName.equals("featureSift") && buf.siftFeature.size() == 0) {
                                continue;
                            }
                            for (String fName : GeneticItem.featureNames) {
//                                System.out.println("Feature: " + fName);
                                float dist = -1.0F;
                                if (fName.equals("featureSift") && buffer.siftFeature.size() > 0 && buf.siftFeature.size() > 0) {
                                    dist = SiftSearcher.getDistance(buffer.siftFeature, buf.siftFeature);
                                } else if (buffer.simpleFeatures.containsKey(fName) && buf.simpleFeatures.containsKey(fName)) {
                                    dist = buffer.simpleFeatures.get(fName).getDistance(buf.simpleFeatures.get(fName));
                                }
                                if (dist >= 0) {
                                    setDistance(query, dataTag, fName, dist);
                                    needSave = true;
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return needSave;
    }

    public void save(File f) throws Exception {
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
        for (String query : distance.keySet()) {
            for (String dataTag : distance.get(query).keySet()) {
                for (String featureName : distance.get(query).get(dataTag).keySet()) {
                    out.write(query + " " + dataTag + " " + featureName + " " + distance.get(query).get(dataTag).get(featureName));
                    out.newLine();
                }
            }
        }
        out.close();
    }

    public static DistanceManager getDM(File f) {
        DistanceManager dm = new DistanceManager();
        try {
            BufferedReader in = new BufferedReader(new FileReader(f));
            String buf = in.readLine();
            while (buf != null) {
                Scanner scanner = new Scanner(buf);
                String query = scanner.next();
                String dataTag = scanner.next();
                String featureName = scanner.next();
                float dist = scanner.nextFloat();
                dm.setDistance(query, dataTag, featureName, dist);
                buf = in.readLine();
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!Config.ignore_auto_complete) {
            try {
                if (dm.autoComplete()) {
                    dm.save(f);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dm;
    }
}
