package tuning;

/**
 * Created by Epsirom on 14/12/12.
 */
public class DistStringResult implements Comparable<DistStringResult> {

    public float dist = 0.0F;
    public String name;

    public DistStringResult(float dist, String name) {
        this.dist = dist;
        this.name = name;
    }

    @Override
    public int compareTo(DistStringResult o) {
        if (this.dist > o.dist) {
            return 1;
        } else if (this.dist < o.dist) {
            return -1;
        }
        return 0;
    }
}
