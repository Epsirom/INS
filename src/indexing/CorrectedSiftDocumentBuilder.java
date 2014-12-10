package indexing;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.sift.Extractor;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Epsirom on 14/12/10.
 */
public class CorrectedSiftDocumentBuilder extends AbstractDocumentBuilder {
    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Extractor extractor = new Extractor();

    public Field[] createDescriptorFields(BufferedImage var1) {
        Field[] var2 = null;

        try {
            List var3 = this.extractor.computeSiftFeatures(var1);
            var2 = new Field[var3.size()];
            int var4 = 0;

            for(Iterator var5 = var3.iterator(); var5.hasNext(); ++var4) {
                Feature var6 = (Feature)var5.next();
                var2[var4] = new StoredField("featureSift", CorrectSiftFeature.getByteArrayRepresentation(var6));
            }
        } catch (IOException var7) {
            this.logger.severe(var7.getMessage());
        }

        return var2;
    }

    public Document createDocument(BufferedImage var1, String var2) {
        Document var3 = null;

        try {
            List var4 = this.extractor.computeSiftFeatures(var1);
            var3 = new Document();
            Iterator var5 = var4.iterator();

            while(var5.hasNext()) {
                Feature var6 = (Feature)var5.next();
                var3.add(new StoredField("featureSift", CorrectSiftFeature.getByteArrayRepresentation(var6)));
            }

            if(var2 != null) {
                var3.add(new StringField("descriptorImageIdentifier", var2, Field.Store.YES));
            }
        } catch (IOException var7) {
            this.logger.severe(var7.getMessage());
        }

        return var3;
    }
}
