package retrieval;

import entry.Base;
import entry.Config;
import evaluation.ComputeAP;
import indexing.LireIndexer;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import org.apache.lucene.index.IndexReader;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Epsirom on 14/12/9.
 */
public class LireSearcher {
    private static Logger logger = Base.logger(LireSearcher.class);
    private static Map<String, ImageSearcher> searcher_map = new HashMap<String, ImageSearcher>();

    static {
        int n = Config.num_of_results;
        searcher_map.put("acc", ImageSearcherFactory.createAutoColorCorrelogramImageSearcher(n));
        searcher_map.put("edge_histogram", ImageSearcherFactory.createEdgeHistogramImageSearcher(n));
        searcher_map.put("scalable_color", ImageSearcherFactory.createScalableColorImageSearcher(n));
        searcher_map.put("cedd", ImageSearcherFactory.createCEDDImageSearcher(n));
        searcher_map.put("fcth", ImageSearcherFactory.createFCTHImageSearcher(n));
        searcher_map.put("jcd", ImageSearcherFactory.createJCDImageSearcher(n));
        searcher_map.put("color_histogram", ImageSearcherFactory.createColorHistogramImageSearcher(n));
        searcher_map.put("tamura", ImageSearcherFactory.createTamuraImageSearcher(n));
        searcher_map.put("gabor", ImageSearcherFactory.createGaborImageSearcher(n));
        searcher_map.put("jch", ImageSearcherFactory.createJpegCoefficientHistogramImageSearcher(n));
        searcher_map.put("joint_histogram", ImageSearcherFactory.createJointHistogramImageSearcher(n));
        searcher_map.put("opp_histogram", ImageSearcherFactory.createOpponentHistogramSearcher(n));
        searcher_map.put("lum_layout", ImageSearcherFactory.createLuminanceLayoutImageSearcher(n));
        searcher_map.put("phog", ImageSearcherFactory.createPHOGImageSearcher(n));
        /*searcher_map.put("sift", new GenericFastImageSearcher(
                n,
                net.semanticmetadata.lire.imageanalysis.sift.Feature.class,
                "featureSift")
        );*/
        searcher_map.put("sift", new SiftSearcher(n, "featureSift"));
    }

    public static ImageSearcher getSearcher(String name) throws Exception {
        if (!searcher_map.containsKey(name)) {
            throw new Exception("Searcher not found.");
        }
        return searcher_map.get(name);
    }

    public static ImageSearchHits searchImage(BufferedImage img, String searcher_name) throws Exception {
        return searchImage(img, getSearcher(searcher_name));
    }

    public static ImageSearchHits searchImage(BufferedImage img, ImageSearcher searcher) throws IOException {
        logger.info("Start searching...");
        IndexReader reader = LireIndexer.getReader();
        ImageSearchHits result = searcher.search(img, reader);
        logger.info("Searching finished.");
        return result;
    }

    public static ImageSearchHits searchImage(String path, ImageSearcher searcher) throws IOException {
        return searchImage(ImageIO.read(new FileInputStream(path)), searcher);
    }

    public static ImageSearchHits searchImage(String path, String searcher) throws Exception {
        return searchImage(path, getSearcher(searcher));
    }

    public static String outputFormat(String str) {
        String fname = Paths.get(str).getFileName().toString();
        int pos = fname.lastIndexOf(".");
        if (pos > 0) {
            return fname.substring(0, pos);
        }
        return fname;
    }

    public static void printResult(ImageSearchHits hits) {
        if (Config.result_file != null) {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(Config.result_file));
                for (int i = 0; i < hits.length(); ++i) {
                    out.write(outputFormat(hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue()));
                    out.newLine();
                }
                out.close();
            } catch (Exception e) {
                logger.error("Output result file {} failed.", Config.result_file);
                e.printStackTrace();
            }
            if (Config.query_file != null && Config.compute_ap) {
                String groundTruth = Config.query_file;
                groundTruth = groundTruth.substring(0, groundTruth.length() - "query.txt".length());
                try {
                    ComputeAP ap = new ComputeAP(groundTruth);
                    logger.info("AP: {}", ap.computeAP(ComputeAP.getSetByFile(Config.result_file)));
                } catch (Exception e) {
                    logger.error("Compute AP with ground truth {} failed.", groundTruth);
                    e.printStackTrace();
                }
            }
        } else {
            for (int i = 0; i < hits.length(); ++i) {
                System.out.println(outputFormat(hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue()));
            }
        }
    }

    public static void searchByConfig() throws Exception {
        String searcher_name = "edge_histogram";
        if (Config.query_file != null) {
            try {
                BufferedReader in = new BufferedReader(new FileReader(Config.query_file));
                String str = in.readLine();
                in.close();
                Scanner s = new Scanner(str);
                String filename = s.next();
                float left = s.nextFloat();
                float top = s.nextFloat();
                float width = s.nextFloat();
                float height = s.nextFloat();
                BufferedImage image = ImageIO.read(new FileInputStream(Config.data_path + "/" + filename + ".jpg"));
                if (left + width > image.getWidth()) {
                    width = image.getWidth() - left;
                }
                if (top + height > image.getHeight()) {
                    height = image.getHeight() - top;
                }
                printResult(searchImage(image.getSubimage((int)left, (int)top, (int)width, (int)height), searcher_name));
            } catch (Exception e) {
                logger.error("Load query file failed: {}", e.toString());
                e.printStackTrace();
            }
        } else if (Config.query_image != null) {
            printResult(searchImage(Config.query_image, searcher_name));
        } else {
            throw new Exception("Query not found!");
        }
    }
}
