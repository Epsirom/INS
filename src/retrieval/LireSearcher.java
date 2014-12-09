package retrieval;

import entry.Config;
import indexing.LireIndexer;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import org.apache.lucene.index.IndexReader;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Epsirom on 14/12/9.
 */
public class LireSearcher {
    private static Map<String, ImageSearcher> searcher_map;

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
    }

    public static ImageSearcher getSearcher(String name) {
        return searcher_map.get(name);
    }

    public static ImageSearchHits searchImage(String path, ImageSearcher searcher) throws IOException {
        IndexReader reader = LireIndexer.getReader();
        return searcher.search(ImageIO.read(new FileInputStream(path)), reader);
    }

    public static ImageSearchHits searchImage(String path, String searcher) throws IOException {
        return searchImage(path, getSearcher(searcher));
    }
}
