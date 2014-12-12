package tuning;

/**
 * Created by Epsirom on 14/12/12.
 */
public class FeatureBufferResult implements Comparable<FeatureBufferResult> {
    public float dist = 0.0F;
    public FeatureBuffer buffer = null;
    public int index = 0;

    public FeatureBufferResult(float dist, FeatureBuffer buffer, int index) {
        this.index = index;
        this.dist = dist;
        this.buffer = buffer;
    }

    @Override
    public int compareTo(FeatureBufferResult o) {
        if (this.dist > o.dist) {
            return 1;
        } else if (this.dist < o.dist) {
            return -1;
        }
        return 0;
    }
}
