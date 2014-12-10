package indexing;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcReader;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.SiftDocumentBuilder;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;


/**
 * Created by Epsirom on 14/12/9.
 */
public class MetadataBuilder extends ChainedDocumentBuilder {
    public MetadataBuilder() {
        super();
        //addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
        //addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
        //addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
        //addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
        //addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
        //addBuilder(DocumentBuilderFactory.getScalableColorBuilder());
        addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
        //addBuilder(DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder());
        //addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
        //addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
        //addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
        //addBuilder(DocumentBuilderFactory.getJpegCoefficientHistogramDocumentBuilder());
        //addBuilder(DocumentBuilderFactory.getOpponentHistogramDocumentBuilder());
        //addBuilder(DocumentBuilderFactory.getJointHistogramDocumentBuilder());
        //addBuilder(DocumentBuilderFactory.getLuminanceLayoutDocumentBuilder());
        //addBuilder(DocumentBuilderFactory.getPHOGDocumentBuilder());
        //addBuilder(new SurfDocumentBuilder());
        addBuilder(new CorrectedSiftDocumentBuilder());
    }

    @Override
    public Document createDocument(BufferedImage bufferedImage, String s) throws FileNotFoundException {
        Document d = super.createDocument(ImageUtils.createWorkingCopy(bufferedImage), s);
        // extract available metadata:
        Metadata metadata = new Metadata();
        try {
            new ExifReader(new FileInputStream(s)).extract(metadata);
            new IptcReader(new FileInputStream(s)).extract(metadata);
            // add metadata to document:
            Iterator i = metadata.getDirectoryIterator();
            while (i.hasNext()) {
                Directory dir = (Directory) i.next();
                String prefix = dir.getName();
                Iterator ti = dir.getTagIterator();
                while (ti.hasNext()) {
                    Tag tag = (Tag) ti.next();
                    // System.out.println(prefix+"-"+tag.getTagName()+" -> " + dir.getString(tag.getTagType()));
                    // add to document:
                    d.add(new TextField(prefix + "-" + tag.getTagName(), dir.getString(tag.getTagType()), Field.Store.YES));
                }
            }
        } catch (JpegProcessingException e) {
            System.err.println("Error reading EXIF & IPTC metadata from image file.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return d;
    }

    @Override
    public Field[] createDescriptorFields(BufferedImage image) {
        return super.createDescriptorFields(image);
    }
}
