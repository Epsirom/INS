package retrieval;

import entry.Base;
import entry.Config;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    public static void printResult(ImageSearchHits hits) {
        for (int i = 0; i < hits.length(); ++i) {
            System.out.println(hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue() + " " + Float.toString(hits.score(i)));
        }
    }

    public static void searchByConfig() throws Exception {
        if (Config.query_file != null) {

        } else if (Config.query_image != null) {
            printResult(searchImage(Config.query_image, "sift"));
        } else {
            throw new Exception("Query not found!");
        }
    }
}
