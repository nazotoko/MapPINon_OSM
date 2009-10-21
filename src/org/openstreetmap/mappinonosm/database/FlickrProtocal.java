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
import java.util.ArrayList;
import java.util.Date;
import org.xml.sax.SAXException;

/**
 *
 * @author nazo
 */
public class FlickrProtocal extends XML {
    private String secret;
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

    /** Called only form XMLBase.
     * @param id integer id number 
     */
    void setFlickr(Flickr flickr,String secret) {
        this.f=flickr;
        this.secret=secret;
    }
    
    @Override
    void read() {
        PeopleInterface peoplei= f.getPeopleInterface();
        PhotosInterface photosi = f.getPhotosInterface();
        try {
            PhotoList plist = null;
            SearchParameters sp = new SearchParameters();

            String[] args = uri.getSchemeSpecificPart().split("/");
            for(int i = 0; i < args.length; i++){
                if(args[i].startsWith("tags")){
                    System.out.println("tags: " + args[i + 1]);
                    sp.setTags(new String[]{args[i + 1]});
                    break;
                } else if(args[i].startsWith("sets")){
                    System.out.println("sets: " + args[i + 1]);
                    //sp.????(args[i+1]);
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
                Photo p = photosi.getPhoto(((Photo)o).getId());
                photo = new org.openstreetmap.mappinonosm.database.Photo();
                photo.setXML(this);
//                System.out.println("id: " + p.getId());
                photo.setTitle(p.getTitle());
                System.out.println("\ttitle: " + p.getTitle());
                photo.setLink(p.getUrl());
                System.out.println("\tlink: " + p.getUrl());
                photo.setThumnale(p.getThumbnailUrl());
                System.out.println("\tthumbnail: " + p.getThumbnailUrl());
                photo.setOriginal(p.getOriginalUrl());
                System.out.println("\toriginal: " + p.getOriginalUrl());
                System.out.println("\ttaken:" + p.getDateTaken());
                System.out.println("\tposted:" + p.getDatePosted());
                photo.setUpdateDate(p.getLastUpdate());
                System.out.println("\tupdated:" + p.getLastUpdate());
                for(Object o2: p.getTags()){
                    Tag t = (Tag)o2;
                    machineTags(t.getValue());
                    System.out.println("tag: " + t.getValue());
                }
                ArrayList<Exif> exifal = (ArrayList<Exif>)photosi.getExif(p.getId(), secret);
                for(Exif e: exifal){
                    System.out.println(e.getTag() + ": " + e.getClean());
                }
                photo.setReadDate(new Date());
                if(pb.add(photo) == false){
                    org.openstreetmap.mappinonosm.database.Photo oldPhoto = pb.get(photo);
                    if(oldPhoto.getReadDate().compareTo(photo.getUpdateDate()) < 0){
                        photo.setId(oldPhoto.getId());
                        pb.remove(oldPhoto);
                        pb.add(photo);
                        photo.getEXIF();
                        System.out.println("\tThe JPEG is replaced! photo ID: " + photo.getId());
                    } else {
                        oldPhoto.upDate(photo);
                        System.out.println("\tphoto ID: " + oldPhoto.getId());
                    }
                } else {// This means new photo.
                    photo.getEXIF();
                    System.out.println("\tnew photo ID: " + photo.getId());
                }
                photo = null;
            }
        } catch(IOException ex) {
            System.out.println("IO Exception: " + ex.getMessage());
        } catch(SAXException ex) {
            System.out.println("SAX XML Syntax Exception: " + ex.getMessage());
        } catch(FlickrException ex) {
            System.out.println("Flickr has some trable: " + ex.getMessage());
        }
    }
}
