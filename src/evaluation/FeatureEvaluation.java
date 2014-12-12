package evaluation;

import java.util.Collections;
import java.util.List;

/**
 * Created by Epsirom on 14/12/11.
 */
public class FeatureEvaluation {
    public float maxAP = 0.0F;
    public float minAP = 0.0F;
    public float midAP = 0.0F;
    public float avgAP = 0.0F;

    public FeatureEvaluation(List<Float> aps) {
        Collections.sort(aps);
        this.maxAP = aps.get(aps.size() - 1);
        this.minAP = aps.get(0);
        if (aps.size() % 2 == 0) {
            this.midAP = (aps.get(aps.size() / 2 - 1) + aps.get(aps.size() / 2)) / 2;
        } else {
            this.midAP = aps.get(aps.size() / 2);
        }
        this.avgAP = 0.0F;
        for (float ap : aps) {
            this.avgAP += ap;
        }
        this.avgAP /= aps.size();
    }
}
