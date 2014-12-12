package tuning;

import entry.Config;
import evaluation.ComputeAP;
import evaluation.FeatureEvaluation;
import retrieval.MultiFeatureSearcher;

import java.util.*;

/**
 * Created by Epsirom on 14/12/11.
 */
public class GeneticItem implements Comparable<GeneticItem> {
    public HashMap<String, Float> featureWeights = new HashMap<String, Float>();
    public FeatureEvaluation evaluation = null;
    public static ArrayList<String> featureNames = new ArrayList<String>(15);
    public static Random random = new Random();

    static {
        featureNames.add("featureSift");
        featureNames.add("featureCEDD");
        featureNames.add("featureAutoColorCorrelogram");
        featureNames.add("descriptorEdgeHistogram");
        featureNames.add("descriptorScalableColor");
        featureNames.add("featureFCTH");
        featureNames.add("featureJCD");
        featureNames.add("featureColorHistogram");
        featureNames.add("featureTAMURA");
        featureNames.add("featureGabor");
        featureNames.add("featureJpegCoeffs");
        featureNames.add("featureJointHist");
        featureNames.add("featOpHist");
        featureNames.add("featLumLay");
        featureNames.add("featPHOG");
    }

    public GeneticItem(Map<String, Float> weights) {
        for (String key : weights.keySet()) {
            featureWeights.put(key, weights.get(key));
        }
    }

    public GeneticItem(Scanner scanner) {
        if (!scanner.hasNext()) {
            evaluate();
            return;
        }
        featureWeights.put("featureSift", scanner.nextFloat());
        for (String key : featureNames) {
            if (!scanner.hasNext()) {
                evaluate();
                return;
            }
            featureWeights.put(key, scanner.nextFloat());
        }
        evaluate();
    }

    public GeneticItem(int id) {
        featureWeights.put(featureNames.get(id), 1.0F);
        evaluate();
    }

    public String toString() {
        String tmp = evaluation.avgAP + " " + evaluation.midAP + " " + evaluation.maxAP + " " + evaluation.minAP;
        tmp += (" " + (featureWeights.containsKey("featureSift") ? featureWeights.get("featureSift") : 0.0F));
        for (String key : featureNames) {
            tmp += (" " + (featureWeights.containsKey(key) ? featureWeights.get(key) : 0.0F));
        }
        return tmp;
    }

    public Set<String> search(String query) {
        TreeSet<DistStringResult> docs = new TreeSet<DistStringResult>();
        DistanceManager dm = GeneticTuning.dm;
        float maxDistance = -1.0F;
        int maxHits = Config.num_of_results;
        for (String dataTag : dm.distance.get(query).keySet()) {
            float dist = 0.0F;
            for (String featureName : dm.distance.get(query).get(dataTag).keySet()) {
                if (featureWeights.containsKey(featureName)) {
                    dist += dm.getDistance(query, dataTag, featureName) * featureWeights.get(featureName);
                }
            }
            if (maxDistance < 0) {
                maxDistance = dist;
            }
            if (docs.size() < maxHits) {
                docs.add(new DistStringResult(dist, dataTag));
                if (dist > maxDistance) {
                    maxDistance = dist;
                }
            } else if (dist < maxDistance) {
                docs.remove(docs.last());
                docs.add(new DistStringResult(dist, dataTag));
                maxDistance = docs.last().dist;
            }
        }
        TreeSet<String> result = new TreeSet<String>();
        for (DistStringResult doc : docs) {
            result.add(doc.name);
        }
        return result;
    }

    public void evaluate() {
        ArrayList<Float> aps = new ArrayList<Float>(GeneticTuning.qoi.size());
        int numROIs = GeneticTuning.qoi.size();
        for (int i = 0; i < numROIs; ++i) {
            //System.out.println("ROI " + i);
            ComputeAP ap = GeneticTuning.computeAPs.get(i);
            aps.add(ap.computeAP(search(GeneticTuning.qoi.get(i))));
            /*
            FeatureBuffer roi = GeneticTuning.roiFeatures.get(i);
            try {
                Set<String> results = MultiFeatureSearcher.search(roi, featureWeights);
                //Set<String> results = MultiFeatureSearcher.search(roi, GeneticTuning.datasetFeatures, featureWeights);
                aps.add(ap.computeAP(results));
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }
        this.evaluation = new FeatureEvaluation(aps);
    }

    public GeneticItem randomAlter() {
        String featureName = featureNames.get(random.nextInt(featureNames.size()));
        float oldVal = getWeight(featureName);
        GeneticItem item = new GeneticItem(featureWeights);
        int flag = random.nextInt(4);
        switch (flag) {
            case 0:
                oldVal += 1.0F;
                break;
            case 1:
                oldVal -= 1.0F;
                break;
            case 2:
                oldVal *= 1.1F;
                break;
            case 3:
                oldVal /= 1.1F;
                break;
        }
        item.featureWeights.put(featureName, oldVal);
        item.evaluate();
        return item;
    }

    public float getWeight(String key) {
        if (featureWeights.containsKey(key)) {
            return featureWeights.get(key);
        } else {
            return 0.0F;
        }
    }

    public GeneticItem giveBirth(GeneticItem o) {
        HashMap<String, Float> childWeights = new HashMap<String, Float>();
        for (String key : featureNames) {
            float ratio = (float)Math.random();
            float w = this.getWeight(key) * ratio + o.getWeight(key) * (1 - ratio);
            if (w != 0) {
                childWeights.put(key, w);
            }
        }
        GeneticItem item = new GeneticItem(childWeights);
        item.evaluate();
        return item;
    }

    @Override
    public int compareTo(GeneticItem o) {
        if (o.evaluation.avgAP > this.evaluation.avgAP) {
            return -1;
        } else if (o.evaluation.avgAP < this.evaluation.avgAP) {
            return 1;
        } else {
            return 0;
        }
    }
}
