package evaluation;

import java.io.*;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Epsirom on 14/12/11.
 */
public class ComputeAP {
    public Set<String> pos_set = new TreeSet<String>();
    public Set<String> amb_set = new TreeSet<String>();

    public ComputeAP() {}

    public ComputeAP(String groundTruth) throws IOException {
        loadPosSetFromFile(getReaderByFile(groundTruth + "good.txt"));
        loadPosSetFromFile(getReaderByFile(groundTruth + "ok.txt"));
        loadAmbSetFromFile(getReaderByFile(groundTruth + "junk.txt"));
    }

    public void loadPosSetFromFile(BufferedReader in) throws IOException {
        String buf = in.readLine();
        while (buf != null) {
            pos_set.add(buf);
            buf = in.readLine();
        }
    }

    public void loadAmbSetFromFile(BufferedReader in) throws IOException {
        String buf = in.readLine();
        while (buf != null) {
            amb_set.add(buf);
            buf = in.readLine();
        }
    }

    public static BufferedReader getReaderByFile(String path) throws IOException {
        return new BufferedReader(new FileReader(path));
    }

    public static Set<String> getSetByFile(String path) throws IOException{
        BufferedReader in = getReaderByFile(path);
        Set<String> s = new TreeSet<String>();
        String buf = in.readLine();
        while (buf != null) {
            s.add(buf);
            buf = in.readLine();
        }
        return s;
    }

    public float computeAP(Set<String> ranked_list) {
        float old_recall = 0.0F, old_precision = 1.0F;
        float ap = 0.0F;
        int intersect_size = 0;
        float j = 1.0F;
        for (String s : ranked_list) {
            if (amb_set.contains(s)) {
                continue;
            }
            if (pos_set.contains(s)) {
                intersect_size++;
            }
            float recall = intersect_size / (float)pos_set.size();
            float precision = intersect_size / (j + 1.0F);

            ap += (recall - old_recall) * (old_precision + precision) / 2.0F;
            old_recall = recall;
            old_precision = precision;
            j += 1.0F;
        }
        return ap;
    }
}
