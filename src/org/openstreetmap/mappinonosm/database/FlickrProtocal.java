/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openstreetmap.mappinonosm.database;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.people.PeopleInterface;
import com.aetrion.flickr.people.User;
import com.aetrion.flickr.photos.Exif;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.photos.SearchParameters;
import com.aetrion.flickr.tags.Tag;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import org.openstreetmap.mappinonosm.MapPINonOSM;
import org.xml.sax.SAXException;

/**
 *
 * @author nazo
 */
public class FlickrProtocal extends XML {
    private String secret;
    private String key;
    private Flickr f=null;
    /** Called only form XMLBase.getInstance();
     * 
     * @param u URI of it. The scheame is "flickr".
     */
    FlickrProtocal(URI u) {
        super(u);
        
    }
    /** Called only form XMLBase.
     * @param id integer id number 
     */
    FlickrProtocal(int id) {
        super(id);
    }

    @Override
    void read() {
        f = new Flickr(key);
        PeopleInterface peoplei= f.getPeopleInterface();
        PhotosInterface photosi=f.getPhotosInterface();
        try {
            uri = new URI("flickr:42992019@N02/tags/openstreetmap");
            PhotoList plist = null;
            SearchParameters sp = new SearchParameters();

            System.out.println("scheme: "+uri.getScheme());
            String []args=uri.getSchemeSpecificPart().split("/");

            for(int i = 0; i < args.length; i++){
                if(args[i].startsWith("tags")){
                    System.out.println("tags: " + args[i + 1]);
                    sp.setTags(new String[]{args[i + 1]});
                    break;
                } else if(args[i].startsWith("sets")){
                    System.out.println("sets: " + args[i + 1]);
                    break;
                } else {
                    System.out.println("uesrid: " + args[i]);
                    User u = peoplei.getInfo(args[i]);
                    System.out.println("username: " + u.getUsername());
                    sp.setUserId(u.getId());
                }
            }
            plist = photosi.search(sp, 5, 1);
            for(Object o: plist){
                Photo p = (Photo)o;
                System.out.println("id: " + p.getId());
                System.out.println("name: " + p.getTitle());
                System.out.println("link: " + p.getUrl());
                System.out.println("thumbnail: " + p.getThumbnailUrl());
                p = photosi.getPhoto(p.getId());
                System.out.println("original: " + p.getOriginalUrl());
                System.out.println("taken:" + p.getDateTaken());
                System.out.println("posted:" + p.getDatePosted());
                System.out.println("updated:" + p.getLastUpdate());
                for(Object o2: p.getTags()){
                    Tag t = (Tag)o2;
                    System.out.println("tag: " + t.getValue());
                }
                ArrayList<Exif> exifal = (ArrayList<Exif>)photosi.getExif(p.getId(), secret);
                for(Exif e: exifal){
                    System.out.println(e.getTag() + ": " + e.getRaw());
                }
            }
        } catch(URISyntaxException ex) {
            System.out.println("URI Syntax Exception: " + ex.getMessage());
        } catch(IOException ex) {
            System.out.println("URI Syntax Exception: " + ex.getMessage());
        } catch(SAXException ex) {
            System.out.println("URI Syntax Exception: " + ex.getMessage());
        } catch(FlickrException ex) {
            System.out.println("URI Syntax Exception: " + ex.getMessage());
        }
        
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
