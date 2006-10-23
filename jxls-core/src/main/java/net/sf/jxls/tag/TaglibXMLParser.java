package net.sf.jxls.tag;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.SetNextRule;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.io.IOException;
import java.util.Set;
import java.util.Iterator;

import net.sf.jxls.transformer.Configuration;

/**
 * @author Leonid Vysochyn
 */
public class TaglibXMLParser {

    public static final String TAGLIB_TAG = "taglib";
    public static final String TAG_TAG = "tag";
    public static final String ATTR_TAG = "attribute";
    public static final String ATTR_NAME_TAG = "name";
    public static final String ATTR_REQUIRED_TAG = "required";
    
    

    public TaglibXMLParser() {
    }

    public Taglib parseTaglibXMLFile(String filename){
        Taglib taglib = null;
        try {
            InputStream inputStream = loadTaglibDefinitionFile( filename );
            taglib = parseTaglibXMLStream( inputStream );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return taglib;
    }

    private InputStream loadTaglibDefinitionFile(String taglibFileName) {
        return getClass().getClassLoader().getResourceAsStream( taglibFileName );
    }
   
    private Taglib parseTaglibXMLStream(InputStream inputStream) throws IOException, SAXException {
        Digester digester = new Digester();
        digester.setValidating( false );
        digester.addObjectCreate( "", "");
        Set tagKeys = Taglib.getTagMap().keySet();
        for (Iterator iterator = tagKeys.iterator(); iterator.hasNext();) {
            String tagKey = (String) iterator.next();
            digester.addObjectCreate( TAGLIB_TAG, Taglib.class );
            digester.addSetNext(TAGLIB_TAG + "/" + TAG_TAG, "addTag", "net.sf.jxls.tag.Tag");
            digester.addSetNext( TAGLIB_TAG + "/" + TAG_TAG + "/" + ATTR_TAG, "addAttribute", "");
//            digester.addSetProperties( TAGLIB_TAG );
        }
        Taglib taglib = (Taglib) digester.parse( inputStream );
        return taglib;
    }

}