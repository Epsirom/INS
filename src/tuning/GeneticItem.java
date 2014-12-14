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

    public GeneticItem() {

    }

    public GeneticItem(Map<String, Float> weights) {
        for (String key : weights.keySet()) {
            featureWeights.put(key, weights.get(key));
        }
    }

    public GeneticItem(Scanner scanner) {
        setByScanner(scanner);
        evaluate();
    }

    public void setByScanner(Scanner scanner) {
        if (!scanner.hasNext()) {
            evaluate();
            return;
        }
        //featureWeights.put("featureSift", scanner.nextFloat());
        for (String key : featureNames) {
            if (!scanner.hasNext()) {
                evaluate();
                return;
            }
            featureWeights.put(key, scanner.nextFloat());
        }
    }

    public GeneticItem(int id) {
        featureWeights.put(featureNames.get(id), 1.0F);
        evaluate();
    }

    public static GeneticItem randomItem() {
        GeneticItem item = new GeneticItem();
        for (String featureName : featureNames) {
            item.featureWeights.put(featureName, (float)Math.random());
        }
        item.evaluate();
        return item;
    }

    public String toString() {
        String tmp = evaluation.avgAP + " " + evaluation.midAP + " " + evaluation.maxAP + " " + evaluation.minAP;
        //tmp += (" " + (featureWeights.containsKey("featureSift") ? featureWeights.get("featureSift") : 0.0F));
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
//                    if (dataTag.equals("00895")) {
//                        System.out.println(featureName + ":" + dm.getDistance(query, dataTag, featureName) + "*" + featureWeights.get(featureName) + "=" + dm.getDistance(query, dataTag, featureName) * featureWeights.get(featureName));
//                    }
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

    public static void fullTest() {
        String[] params = new String[] {
                "0.916739 -0.19634552 0.011117695 0.10252904 0.0 0.2860832 0.0018024673 0.0 0.0 0.0 0.0 0.0 6.0861363E-5 -0.25363192 0.0011360993",
                "2.887865 0.008350551 0.02028375 0.4617432 0.0018666949 0.16933735 0.0051918235 0.008206222 0.0 0.0042483206 0.101685114 0.016859192 0.009118049 -0.34180298 0.026379004",
                "1.7501068 -0.5038963 0.0097277425 0.038396254 0.029685218 0.27105823 0.13551159 0.0016981012 0.0 0.015474225 0.06058233 0.010360099 0.0065006074 -0.06913181 0.0123246545",
                "1.2120671 0.007272746 0.0022112536 0.10050783 1.7042719E-4 0.037125666 0.0029745733 5.5094148E-5 6.236415E-6 0.0015577332 0.053962447 0.0055411067 6.5759166E-5 0.12562963 0.0048611863",
                "2.3728526 0.017083421 0.05788247 0.17465495 0.013691798 0.017775401 -0.007904547 0.012115723 7.722818E-6 0.014155126 0.018507997 -0.009679886 -0.048411757 0.15738899 0.023479227",
                "1.2590231 0.24824844 0.0029439535 0.10364805 0.001926594 0.021168191 0.037353627 0.0 0.0 0.0160876 -0.068394385 -9.276392E-4 -0.021278104 0.23223805 0.02068044",
                "0.7828202 0.034721248 0.0 0.05375815 0.0 0.031381965 0.0 0.0 0.0 0.014024047 0.0 0.0 0.008270069 -0.29918662 0.0"
        };
        for (String p : params) {
            testItem(p);
        }
    }

    public static void autoTest() {
        double[] tmp = new double[] {
                1.2120671, 0/*0.007272746*/, 0/*0.0022112536*/, 0.10050783, 0/*1.7042719E-4*/,
                0.037125666, 0/*0.0029745733*/, 0/*5.5094148E-5*/, 6.236415E-6, 0/*0.0015577332*/,
                0.053962447, 0.0055411067, 0/*6.5759166E-5*/, 0.12562963, 0.0048611863
        };
        ArrayList<Float> w = new ArrayList<Float>();
        for (double d : tmp) {
            w.add((float)d);
        }
        testItem(w);
    }

    public static void testItem(String s) {
        Scanner scanner = new Scanner(s);
        GeneticItem item = new GeneticItem();
        item.setByScanner(scanner);
        item.testAndShow();
    }

    public static void testItem(ArrayList<Float> weights) {
        int len = Math.min(weights.size(), featureNames.size());
        HashMap<String, Float> wmap = new HashMap<String, Float>();
        for (int i = 0; i < len; ++i) {
            wmap.put(featureNames.get(i), weights.get(i));
        }
        GeneticItem item = new GeneticItem(wmap);
        item.testAndShow();
    }

    public void testAndShow() {
        System.out.print("=====================================");
        GeneticItem item = this;
        ArrayList<Float> aps = item.getAPs();
        int len = GeneticTuning.qoi.size();
        for (int i = 0; i < len; ++i) {
            System.out.println(GeneticTuning.qoi.get(i) + ": " + aps.get(i));
        }
        item.evaluation = new FeatureEvaluation(aps);
        System.out.println("Average: " + item.evaluation.avgAP);
        System.out.println("Middle: " + item.evaluation.midAP);
        System.out.println("Max: " + item.evaluation.maxAP);
        System.out.println("Min: " + item.evaluation.minAP);
    }

    public ArrayList<Float> getAPs() {
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
        return aps;
    }

    public void evaluate() {
        this.evaluation = new FeatureEvaluation(this.getAPs());
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

    public float getEP() {
        if (Config.evaluate_param == null) {
            return this.evaluation.avgAP;
        } else if (Config.evaluate_param.equals("min")) {
            return this.evaluation.minAP;
        } else if (Config.evaluate_param.equals("max")) {
            return this.evaluation.maxAP;
        } else if (Config.evaluate_param.equals("mid")) {
            return this.evaluation.midAP;
        } else {
            return this.evaluation.avgAP;
        }
    }

    @Override
    public int compareTo(GeneticItem o) {
        float ep = this.getEP(), o_ep = o.getEP();
        if (ep > o_ep) {
            return 1;
        } else if (ep < o_ep) {
            return -1;
        } else {
            return 0;
        }
    }
}
