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
import com.aetrion.flickr.photos.Extras;
import com.aetrion.flickr.photos.GeoData;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photosets.Photoset;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.photos.SearchParameters;
import com.aetrion.flickr.photosets.PhotosetsInterface;
import com.aetrion.flickr.tags.Tag;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import org.xml.sax.SAXException;

/**
 *
 * @author nazo
 */
public class FlickrProtocal extends XML {
    private String secret;
    private Flickr f=null;
    private PhotosInterface photosi;
    static private HashSet extra = new HashSet();
    static{
        extra.add(Extras.DATE_UPLOAD);
        extra.add(Extras.LAST_UPDATE);
        extra.add(Extras.URL_O);
//        extra.add(Extras.URL_T);
        extra.add(Extras.MACHINE_TAGS);
        extra.add(Extras.TAGS);
        extra.add(Extras.GEO);
    }

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

    private PhotoList serch(){
        String s = uri.getSchemeSpecificPart();
        SearchParameters sp = new SearchParameters();
        PeopleInterface peoplei= f.getPeopleInterface();

        System.out.println("Flickr's API: "+s);
        try {
            link = new URL("http://flic.kr/" + s);
        } catch(MalformedURLException ex) {
            System.out.println("This is invaild. ");
            return null;
        }
        String[] args = s.split("/");
        title="Flickr photos";
        try { //if the sercg condition is invalld
//            if(readDate!=null)sp.setMinUploadDate(readDate);
            readDate = new Date();
            for(int i = 0; i < args.length; i++){
                if(args[i].startsWith("tags")){
                    System.out.println("tags: " + args[i + 1]);
                    title+=" tagged &quot;"+args[i+1]+"&quot;.";
                    if(args[i+1].contains(":")){
                        sp.setMachineTags(new String[]{args[i + 1]});
                    }else {
                        sp.setTags(new String[]{args[i + 1]});
                    }
                    break;
                } else if(args[i].startsWith("sets")){
                    PhotosetsInterface photoSeti=f.getPhotosetsInterface();
                    Photoset photoset=photoSeti.getInfo(args[i + 1]);
                    System.out.println("set: " + photoset.getTitle());
                    //sp.????(args[i+1]);
                    title+=" from set &quot;"+photoset.getTitle()+"&quot;.";
                    return photoSeti.getPhotos(args[i + 1], extra, Flickr.PRIVACY_LEVEL_PUBLIC, 60, 1);// extra, Flickr.PRIVACY_LEVEL_PUBLIC
                } else {
                    System.out.println("uesrid: " + args[i]);
                    User u = peoplei.getInfo(args[i]);
                    System.out.println("username: " + u.getUsername());
                    title += " of user &quot;" + u.getUsername() + "&quot;, ";
                    sp.setUserId(args[i]);
                }
            }
            sp.setExtras(extra);
            return photosi.search(sp, 60, 1);
        } catch(IOException ex) {
            System.out.println("IO Exception: " + ex.getMessage());
        } catch(SAXException ex) {
            System.out.println("SAX XML Syntax Exception: " + ex.getMessage());
        } catch(FlickrException ex) {
            System.out.println("Flickr has some trable: " + ex.getMessage());
        }
        return new PhotoList();
    }

    @Override
    void read() {
        photosi = f.getPhotosInterface();
        PhotoList plist = serch();
        System.out.println("\tFound "+plist.size());
        /** analysing Photos */
        for(Object o: plist){
            Photo p = (Photo)o;
            photo = new org.openstreetmap.mappinonosm.database.Photo();
            photo.setXML(this);
//                System.out.println("id: " + p.getId());
            photo.setTitle(entity(p.getTitle()));
            System.out.println("\ttitle: " + p.getTitle());
            photo.setLink("http://flic.kr/" + p.getOwner().getId() + "/" + p.getId());
            System.out.println("\tlink: " + photo.getLink());
            photo.setThumbnale(p.getThumbnailUrl());
            System.out.println("\tthumbnail: " + p.getThumbnailUrl());
            try {
                photo.setOriginal(p.getOriginalUrl());
                System.out.println("\toriginal: " + p.getOriginalUrl());
            } catch(FlickrException ex) {
                System.out.println("\toriginal: not avalable.");
            }
//            System.out.println("\ttaken:" + p.getDateTaken());
            photo.setPublishedDate(p.getDatePosted());
            System.out.println("\tposted:" + p.getDatePosted());
            photo.setUpdateDate(p.getLastUpdate());
            System.out.println("\tupdated:" + p.getLastUpdate());
            for(Object o2: p.getTags()){
                Tag t = (Tag)o2;
                machineTags(t.getValue());
                System.out.println("\ttag: " + t.getValue());
            }
                /*** get georss information  ***/
            if(photo.getLat() == 0 && photo.getLon() == 0){
                GeoData g = p.getGeoData();
                if(g != null){
                    photo.setLat(g.getLatitude());
                    photo.setLon(g.getLongitude());
                    System.out.println("\tgeorss latlon: "+g.getLatitude()+", "+g.getLatitude());
                }
            }

            photo.setReadDate(new Date());
            if(photoTable.add(photo) == false){
                org.openstreetmap.mappinonosm.database.Photo oldPhoto = photoTable.get(photo);
                if(oldPhoto.getReadDate().compareTo(photo.getPublishedDate()) < 0){
                    photo.setId(oldPhoto.getId());
                    photoTable.remove(oldPhoto);
                    photoTable.add(photo);
                    setExifParameters(p.getId());
                    System.out.println("\tThe JPEG is replaced! photo ID: " + photo.getId());
                } else {
                    oldPhoto.upDate(photo);
                    System.out.println("\tphoto ID: " + oldPhoto.getId());
                }
            } else {
                // This means new photo.
                setExifParameters(p.getId());
                System.out.println("\tnew photo ID: " + photo.getId());
            }
        }// end of one photo
        System.out.println("Done: Flickr API.");
    }

    private void setExifParameters(String photoID) {
        String s;
        int latRef=0;
        int lonRef=0;
        int altRef=0;
        int dirRef=0;
        int trackRef=0;
        float speedRef=0;
        ArrayList<Exif> exifal=null;
        try {
            exifal = (ArrayList<Exif>)photosi.getExif(photoID, secret);
        } catch(IOException ex) {
            System.out.println("EXIF not avalable:" + ex.getMessage());
            return;
        } catch(SAXException ex) {
            System.out.println("EXIF not avalable:" + ex.getMessage());
            return;
        } catch(FlickrException ex) {
            System.out.println("EXIF not avalable:" + ex.getMessage());
            return;
        }

        photo.setDownloadDate(new Date());
        for(Exif e: exifal){
            String tag = e.getTag();
            if(tag.equals("Software")&&e.getRaw().contains("Picasa")){
                photo.setRed();
            } else if (tag.equals("FocalLength")) {
                s=e.getRaw();
                photo.setFocalLength(Float.parseFloat(s.substring(0,s.indexOf(' '))));
                System.out.println("\tfocal length: " + photo.getFocalLength());
            } else if (tag.equals("GPSLatitudeRef")) {
                latRef=(e.getRaw().contains("N"))?1:-1;
            } else if (e.getTagspace().equals("GPS")&&tag.equals("GPSLatitude")) {
                double lat = 0;
                s = e.getRaw();
                System.out.println("\tlatClean: "+s);
                lat = Double.parseDouble(s.substring(0, s.indexOf("deg")-1));
                int i=s.indexOf("'");
                lat += Double.parseDouble(s.substring(s.indexOf("deg")+4, i))/60;
                int j = s.indexOf("\"");
                if(j > 0) {
                    s = s.substring(i + 2, j);
                } else {
                    s = s.substring(i + 2);
                }
                lat += Double.parseDouble(s) / 3600;
                if(latRef != 0){
                    lat = lat*latRef;
                    System.out.println("\tlat: " + lat);
                    photo.setEXIFLat(lat);
                }
            } else if (tag.equals("GPSLongitudeRef")) {
                lonRef=(e.getRaw().contains("E"))?1:-1;
            } else if (e.getTagspace().equals("GPS")&&tag.equals("GPSLongitude")) {
                double lon = 0;
                s = e.getRaw();
                System.out.println("\tlonClean: "+s);
                lon = Double.parseDouble(s.substring(0, s.indexOf("deg")-1));
                int i = s.indexOf("'");
                lon += Double.parseDouble(s.substring(s.indexOf("deg")+4, i))/60;
                int j = s.indexOf("\"");
                if(j > 0) {
                    s = s.substring(i + 2, j);
                } else {
                    s = s.substring(i + 2);
                }
                lon += Double.parseDouble(s) / 3600;
                if(lonRef != 0){
                    lon = lon * lonRef;
                    System.out.println("\tlon: " + lon);
                    photo.setEXIFLon(lon);
                }
            } else if (tag.equals("GPSSpeedRef")) {
                speedRef=(e.getRaw().contains("K"))?1:(e.getRaw().contains("M"))?1.609344F:1.852F;
            } else if (e.getTagspace().equals("GPS")&&tag.equals("GPSSpeed")) {
                if(speedRef!=0){
                    s=e.getRaw();
                    photo.setSpeed(Float.parseFloat(s.substring(0,s.indexOf(" ")))*speedRef);
                    System.out.println("\tspeed: " + photo.getSpeed());
                }
            } else if (tag.equals("GPSAltitudeRef")) {
                altRef=(e.getRaw().contains("Above Sea"))?1:-1;
            } else if (e.getTagspace().equals("GPS")&&tag.equals("GPSAltitude")) {
                if(altRef!=0){
                    s=e.getRaw();
                    photo.setAltitude(Float.parseFloat(s.substring(0,s.indexOf(" ")))*altRef);
                    System.out.println("\talt: " + photo.getAltitude());
                }
            } else if (tag.equals("GPSImgDirectionRef")) {
                dirRef=(e.getRaw().contains("True"))?1:-1;
            } else if (e.getTagspace().equals("GPS")&&tag.equals("GPSImgDirection")) {
                if(dirRef!=0){
                    photo.setDirection(Float.parseFloat(e.getRaw()));
                    System.out.println("\tdir: " + photo.getDirection());
                }
            } else if (tag.equals("GPSTrackRef")) {
                trackRef=(e.getRaw().contains("True"))?1:-1;
            } else if (e.getTagspace().equals("GPS")&&tag.equals("GPSTrack")) {
                if(trackRef!=0){
                    photo.setTrack(Float.parseFloat(e.getRaw()));
                    System.out.println("\ttrack: " + photo.getTrack());
                }
            }
        }
    }
}
