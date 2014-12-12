package tuning;

import entry.Base;
import entry.Config;
import evaluation.ComputeAP;
import indexing.LireIndexer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.slf4j.Logger;
import retrieval.LireSearcher;
import retrieval.MultiFeatureSearcher;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * Created by Epsirom on 14/12/11.
 */
public class GeneticTuning {
    private static Logger logger = Base.logger(GeneticTuning.class);
    public static ArrayList<String> qoi = new ArrayList<String>(55);
//    public static ArrayList<BufferedImage> roi = new ArrayList<BufferedImage>(55);
//    public static ArrayList<FeatureBuffer> roiFeatures = new ArrayList<FeatureBuffer>(55);
//    public static ArrayList<FeatureBuffer> datasetFeatures = new ArrayList<FeatureBuffer>(5062);
    public static ArrayList<ComputeAP> computeAPs = new ArrayList<ComputeAP>(55);
    public TreeSet<GeneticItem> pool = new TreeSet<GeneticItem>(Collections.reverseOrder());
    public int maxPoolSize = 10000;
    private int emSize = maxPoolSize / 10;
    private int emCount = maxPoolSize / 100;
    protected File bindedFile = null;
    protected int tuning_round = 0;
    public static Random random = new Random();

    public static DistanceManager dm;

    public static final float alter_rate = 0.1F;
    public static final float new_rate = 0.05F;

    static {
        qoi.add("gt/all_souls_1_query.txt");
        qoi.add("gt/all_souls_2_query.txt");
        qoi.add("gt/all_souls_3_query.txt");
        qoi.add("gt/all_souls_4_query.txt");
        qoi.add("gt/all_souls_5_query.txt");

        qoi.add("gt/ashmolean_1_query.txt");
        qoi.add("gt/ashmolean_2_query.txt");
        qoi.add("gt/ashmolean_3_query.txt");
        qoi.add("gt/ashmolean_4_query.txt");
        qoi.add("gt/ashmolean_5_query.txt");

        qoi.add("gt/balliol_1_query.txt");
        qoi.add("gt/balliol_2_query.txt");
        qoi.add("gt/balliol_3_query.txt");
        qoi.add("gt/balliol_4_query.txt");
        qoi.add("gt/balliol_5_query.txt");

        qoi.add("gt/bodleian_1_query.txt");
        qoi.add("gt/bodleian_2_query.txt");
        qoi.add("gt/bodleian_3_query.txt");
        qoi.add("gt/bodleian_4_query.txt");
        qoi.add("gt/bodleian_5_query.txt");

        qoi.add("gt/christ_church_1_query.txt");
        qoi.add("gt/christ_church_2_query.txt");
        qoi.add("gt/christ_church_3_query.txt");
        qoi.add("gt/christ_church_4_query.txt");
        qoi.add("gt/christ_church_5_query.txt");

        qoi.add("gt/cornmarket_1_query.txt");
        qoi.add("gt/cornmarket_2_query.txt");
        qoi.add("gt/cornmarket_3_query.txt");
        qoi.add("gt/cornmarket_4_query.txt");
        qoi.add("gt/cornmarket_5_query.txt");

        /////////////////////////////////////////////

        qoi.add("gt/hertford_1_query.txt");
        qoi.add("gt/hertford_2_query.txt");
        qoi.add("gt/hertford_3_query.txt");
        qoi.add("gt/hertford_4_query.txt");
        qoi.add("gt/hertford_5_query.txt");

        qoi.add("gt/keble_1_query.txt");
        qoi.add("gt/keble_2_query.txt");
        qoi.add("gt/keble_3_query.txt");
        qoi.add("gt/keble_4_query.txt");
        qoi.add("gt/keble_5_query.txt");

        qoi.add("gt/magdalen_1_query.txt");
        qoi.add("gt/magdalen_2_query.txt");
        qoi.add("gt/magdalen_3_query.txt");
        qoi.add("gt/magdalen_4_query.txt");
        qoi.add("gt/magdalen_5_query.txt");

        qoi.add("gt/pitt_rivers_1_query.txt");
        qoi.add("gt/pitt_rivers_2_query.txt");
        qoi.add("gt/pitt_rivers_3_query.txt");
        qoi.add("gt/pitt_rivers_4_query.txt");
        qoi.add("gt/pitt_rivers_5_query.txt");

        qoi.add("gt/radcliffe_camera_1_query.txt");
        qoi.add("gt/radcliffe_camera_2_query.txt");
        qoi.add("gt/radcliffe_camera_3_query.txt");
        qoi.add("gt/radcliffe_camera_4_query.txt");
        qoi.add("gt/radcliffe_camera_5_query.txt");
    }

    public static FeatureBuffer getROI(String q) {
        try {
            BufferedImage img = LireSearcher.getQueryImageByFile(q);
            return new FeatureBuffer(MultiFeatureSearcher.getDocumentByImage(img));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void initialize() {
        logger.info("Loading distance manager...");
        dm = DistanceManager.getDM(new File(Config.distance_file));
        for (String q : qoi) {
            try {
                computeAPs.add(new ComputeAP(q.substring(0, q.length() - "query.txt".length())));
            } catch (Exception e) {
                computeAPs.add(null);
                e.printStackTrace();
            }
        }
        /*
        logger.info("Loading queries...");
        int counter = 0;
        for (String q : qoi) {
            System.out.println(++counter);
            try {
                BufferedImage img = LireSearcher.getQueryImageByFile(q);
                roi.add(img);
                roiFeatures.add(new FeatureBuffer(MultiFeatureSearcher.getDocumentByImage(img)));
            } catch (Exception e) {
                roi.add(null);
                e.printStackTrace();
            }
            try {
                computeAPs.add(new ComputeAP(q.substring(0, q.length() - "query.txt".length())));
            } catch (Exception e) {
                computeAPs.add(null);
                e.printStackTrace();
            }
        }
        /*
        logger.info("Loading dataset...");
        try {
            IndexReader reader = LireIndexer.getReader();
            int numDocs = reader.numDocs();
            for (int i = 0; i < numDocs; ++i) {
                System.out.println(i);
                Document doc = reader.document(i);
                datasetFeatures.add(new FeatureBuffer(doc));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        logger.info("Genetic tuning initialized.");
    }

    public GeneticTuning(String path) {
        logger.info("Create tuning environment...");
        this.bindedFile = new File(path);
        initPool();
        savePool();
        logger.info("Tuning environment created.");
    }

    protected void initPool() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(this.bindedFile));
            String buf = in.readLine();
            while (buf != null) {
                Scanner scanner = new Scanner(buf);
                scanner.nextFloat();
                scanner.nextFloat();
                scanner.nextFloat();
                scanner.nextFloat();
                pool.add(new GeneticItem(scanner));
                buf = in.readLine();
            }
            in.close();
            if (pool.size() == 0) {
                resetPool();
            }
        } catch (Exception e) {
            resetPool();
            e.printStackTrace();
        }
    }

    protected void resetPool() {
        for (int i = 0; i < GeneticItem.featureNames.size(); ++i) {
            System.out.println("Item " + i);
            pool.add(new GeneticItem(i));
        }
    }

    public void savePool() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(this.bindedFile));
            for (GeneticItem item : pool) {
                out.write(item.toString());
                out.newLine();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GeneticItem getPoolItem(int k) {
        Iterator<GeneticItem> it = pool.iterator();
        int i = 0;
        GeneticItem current = it.next();
        while (it.hasNext() && i < k) {
            current = it.next();
            ++i;
        }
        if (current == null) {
            System.out.print("oops");
        }
        return current;
    }

    public void addItemToPool(GeneticItem newItem) {
        if (pool.size() < maxPoolSize || newItem.compareTo(pool.last()) > 0) {
            pool.add(newItem);
            while (pool.size() > maxPoolSize) {
                pool.remove(pool.last());
            }
        }
    }

    public void eliminatePool() {
        if (pool.size() < maxPoolSize - emCount) {
            return;
        }
        int i = 0;
        int dst = pool.size() - (maxPoolSize - emCount);
        while (i < dst) {
            ++i;
            pool.remove(pool.last());
        }
    }

    public GeneticItem randomItem() {
        return getPoolItem(random.nextInt(pool.size()));
    }

    public void runPool() {
        ++this.tuning_round;
        if (this.tuning_round % emSize == 0) {
            logger.info("Start tuning round {} ...", this.tuning_round);
        }
        double r = Math.random();
        if (r < new_rate) {
            GeneticItem newItem = GeneticItem.randomItem();
            addItemToPool(newItem);
        } else if (r < new_rate + alter_rate) {
            GeneticItem newItem = randomItem().randomAlter();
            addItemToPool(newItem);
        } else {
            GeneticItem newItem = randomItem().giveBirth(randomItem());
            addItemToPool(newItem);
        }
        if (this.tuning_round % emSize == 0) {
            savePool();
            eliminatePool();
            logger.info("Tuning round {} finished, current best: {}", this.tuning_round, pool.first().getEP());
        }
    }
}
