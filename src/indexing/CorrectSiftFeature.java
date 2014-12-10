package indexing;

import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import net.semanticmetadata.lire.utils.SerializationUtils;

/**
 * Created by Epsirom on 14/12/10.
 */
public class CorrectSiftFeature {

    public static byte[] getByteArrayRepresentation(Feature f) {
        byte[] var1 = new byte[f.descriptor.length * 8 + 16];
        byte[] var2 = SerializationUtils.toBytes(f.scale);

        int var3;
        for(var3 = 0; var3 < 4; ++var3) {
            var1[var3] = var2[var3];
        }

        var2 = SerializationUtils.toBytes(f.orientation);

        for(var3 = 0; var3 < 4; ++var3) {
            var1[4 + var3] = var2[var3];
        }

        var2 = SerializationUtils.toBytes(f.location[0]);

        for(var3 = 0; var3 < 4; ++var3) {
            var1[8 + var3] = var2[var3];
        }

        var2 = SerializationUtils.toBytes(f.location[1]);

        for(var3 = 0; var3 < 4; ++var3) {
            var1[12 + var3] = var2[var3];
        }

        for(var3 = 16; var3 < var1.length; var3 += 8) {
            var2 = SerializationUtils.toBytes(f.descriptor[(var3 - 16) / 8]);

            for(int var4 = 0; var4 < 8; ++var4) {
                var1[var3 + var4] = var2[var4];
            }
        }

        return var1;
    }

    public static void setByteArrayRepresentation(Feature f, byte[] var1, int var2, int var3) {
        byte[] var4 = new byte[4];
        byte[] dbl = new byte[8];
        f.descriptor = new double[(var3 - 4 * 4) / 8];
        f.location = new float[2];
        System.arraycopy(var1, var2, var4, 0, 4);
        f.scale = SerializationUtils.toFloat(var4);
        System.arraycopy(var1, var2 + 4, var4, 0, 4);
        f.orientation = SerializationUtils.toFloat(var4);
        System.arraycopy(var1, var2 + 8, var4, 0, 4);
        f.location[0] = SerializationUtils.toFloat(var4);
        System.arraycopy(var1, var2 + 12, var4, 0, 4);
        f.location[1] = SerializationUtils.toFloat(var4);

        for(int var5 = 0; var5 < f.descriptor.length; ++var5) {
            System.arraycopy(var1, var2 + 16 + var5 * 8, dbl, 0, 8);
            f.descriptor[var5] = SerializationUtils.toDouble(dbl);
        }
    }
}
