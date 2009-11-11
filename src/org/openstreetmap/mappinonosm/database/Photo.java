/*
 * Copyright (c) 2009, Shun "Nazotoko" Watanabe <nazotoko@gmail.com>
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the OpenStreetMap <www.openstreetmap.org> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.openstreetmap.mappinonosm.database;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.jpeg.JpegSegmentReader;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.GpsDirectory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
/**
 * Information of a Photo
 * @author Shun "Nazotoko" Watanabe
 */
public class Photo {
    static private DecimalFormat df = new DecimalFormat("###.######");
    static private String base64 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_.";
    static private String base36str = "0123456789abcdefghijklmnopqrstuvwxyz";

    //    public static final int STATE_??? = 3;
    public static final int STATE_RED = 2;
    public static final int STATE_YELLOW = 1;
    public static final int STATE_BLUE = 0;

    /** 0 means that it is not set yet.
     *
     */
    private int id=0;

    /** URL of link. This is requied. It is used in int hashCode().
     * <dt>toJavaScript</dt><dd>li:'URL'</dd>
     * <dt>save</dt><dd>li:'URL'</dd>
     */
    private URL link=null;

    /** It shuld belong to a XML.
     * <dl>
     * <dt>toJavaScript</dt><dd>r:'URI'</dd>
     * <dt>save</dt><dd>r:XML id</dd>
     * </dl>
     */
    private XML xml=null;

    /** latitude.
     * 0 means the newPhoto doesn't has any geotag.
     * <dl>
     * <dt>toJavaScrip</dt><dd>la:###.######</dd>
     * <dt>save</dt><dd>la:double</dd>
     * </dl>
     * <a href="#setLat(double)">void setLat(double)</a>
     * <a href="#getLat(double)">double getLat()</a>
     */
    private double latitude=0;

    /** longitude.
     * 0 means the newPhoto doesn't has any geotag.
     * <dl>
     * <dt>tile</dt><dd>lo:###.######</dd>
     * <dt>save</dt><dd>lo:double</dd>
     * </dl>
     * <a href="#setLon(double)">void setLon(double)</a>
     * <a href="#getLon(double)">double getLon()</a>
     */
    private double longitude=0;

    /** state.
     * The values are select from STATE_*.
     * <dl>
     * <dt>tile</dt><dd>s:integer</dd>
     * <dt>save</dt><dd>s:integer</dd>
     * </dl>
     */
    private int state = STATE_YELLOW;

    /** direct URL to origial newPhoto
     * <dl>
     * <dt>tile</dt><dd>o:'URL'</dd>
     * <dt>save</dt><dd>o:'URL'</dd>
     * </dl>
     */
    private URL original=null;

    /** direct URL to thumbnale newPhoto
     * <dl>
     * <dt>tile</dt><dd>th:'URL'</dd>
     * <dt>save</dt><dd>th:'URL'</dd>
     * </dl>
     */
    private URL thumbnale=null;

    /**title text
     * <dl>
     * <dt>tile</dt><dd>ti:'String'</dd>
     * <dt>save</dt><dd>ti:'String'</dd>
     * </dl>
     */
    private String title=null;

    /** GPS altitude (meter)
     * <dl>
     * <dt>tile</dt><dd>al:double</dd>
     * <dt>save</dt><dd>al:double</dd>
     * </dl>
     */
    private float altitude=-1000;
    /** GPS direction (degree)
     * <dl>
     * <dt>tile</dt><dd>di:float</dd>
     * <dt>save</dt><dd>di:float</dd>
     * </dl>
     */
    private float direction=-1000;

    /**
     * EXIF focal length
     * (mm)
     * <dt>tile</dt><dd>fl:float</dd>
     * <dt>save</dt><dd>fl:float</dd>
     */
    private float focalLength=0;

    /** GPS speed (meter)
     * <dl>
     * <dt>tile</dt><dd>sp:float</dd>
     * <dt>save</dt><dd>sp:float</dd>
     * </dl>
     */
    private float speed=-1000;

    /** GPS track (degree)
     * <dl>
     * <dt>tile</dt><dd>tr:float</dd>
     * <dt>save</dt><dd>tr:float</dd>
     * </dl>
     */
    private float track=-1000;

    
    /** The date of last read.
     * <dl>
     * <dt>save</dt><dd>read:integer</dd>
     * </dl>
     */
    private Date readDate=null;

    /** The date of last updated. If it is late from readDate. The photo should
     * be update.
     * <dl>
     * <dt>save</dt><dd>updateDate:integer</dd>
     * </dl>
     */
    private Date updateDate=null;

    /** The date of last download of MapPIN'on OSM.
     * if this is not late from publishedDate. The photo must be
     * downloaded again.
     * <dl>
     * <dt>save</dt><dd>downloaded:integer</dd>
     * </dl>
     */
    private Date downloadedDate=null;

    /** the date of last uploaded
     * <dl>
     * <dt>save</dt><dd>published:integer</dd>
     * </dl>
     */
    private Date publishedDate=null;

    /** osm:way tag
     * <dl>
     * <dt>tile</dt><dd>w:[ingerger,..]</dd>
     * <dt>save</dt><dd>w:[ingerger,..]</dd>
     * </dl>
     */
    private ArrayList<Integer> way = null;

    /** osm:node tag
     * <dl>
     * <dt>tile</dt><dd>n:[ingerger,..]</dd>
     * <dt>save</dt><dd>n:[ingerger,..]</dd>
     * </dl>
     */
    private ArrayList<Integer> node = null;

    /** new flag for statistics */
    private boolean newPhoto = false;
    /** new flag for statistics */
    private boolean reread = false;
    /** new flag for statistics */
    private boolean deleted = false;

    /** Standard constractor */
    public Photo() {
    }

    /**
     * return hashCode of link.
     */
    @Override
    public int hashCode(){
        if(link != null){
            return link.hashCode();
        }
        return 0;
    }

    /**
     * Just comparing only link.
     * @return true if their links are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }
        if(link==null){
            return false;
        }
        final Photo other = (Photo)obj;
        return this.link.equals(other.link);
    }

    /** updating information by new information read from XML.
     * If the newPhoto is the same one, replace some information
     * of the old photo (this) by that of the new photo (newPhoto).
     * @param newPhoto newOne
     */
    public void upDate(Photo newPhoto) {
        title=newPhoto.title;
        readDate = newPhoto.readDate;
        if(newPhoto.updateDate != null) {
            updateDate = newPhoto.updateDate;
        }
        publishedDate = newPhoto.publishedDate;
        if(newPhoto.downloadedDate != null) {
            updateDate = newPhoto.downloadedDate;
        }

        thumbnale=newPhoto.thumbnale;
        original=newPhoto.original;
        node=newPhoto.node;
        way=newPhoto.way;

        if((state != STATE_BLUE || newPhoto.state == STATE_BLUE) && (newPhoto.latitude != 0 || newPhoto.longitude != 0)){
            latitude = newPhoto.latitude;
            longitude = newPhoto.longitude;
            if(newPhoto.state == STATE_BLUE){
                state = newPhoto.state;
            }
        }
    }



    /**
     * @return link
     */
    public URL getLink() {
        return link;
    }
    /** Getting ID
     * @return id
     */
    public int getId() {
        return id;
    }

    /** Getting the Latitude.
     * @return latitude
     */
    public double getLat() {
        return latitude;
    }

    /** Getting the Longitude.
     * @return longitude
     */
    public double getLon() {
        return longitude;
    }

    /**
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return xml
     */
    public XML getXML() {
        return xml;
    }


    /**
     * @return thumbnale
     */
    public URL getThumnale() {
        return thumbnale;
    }

    /**
     * @return original
     */
    public URL getOriginal() {
        return original;
    }
    /** getting altitude.
     * @return altitude
     */
    public float getAltitude() {
        return altitude;
    }
    /** getting direction.
     * @return direction
     */
    public float getDirection() {
        return direction;
    }
    /** getting speed.
     * @return speed
     */
    public float getSpeed() {
        return speed;
    }
    /** getting track.
     * @return track
     */
    public float getTrack() {
        return track;
    }

    /** getting focalLength.
     * @return focalLength
     */
    public float getFocalLength() {
        return focalLength;
    }

    /**
     * @return readDate
     */
    public Date getReadDate() {
        return readDate;
    }

    /**
     * @return updateDate
     */
    public Date getUpdateDate() {
        return updateDate;
    }

    /**
     * @return publishedDate
     */
    public Date getPublishedDate() {
        return publishedDate;
    }

    /**
     * @return downloadedDate
     */
    public Date getDownloadedDate() {
        return downloadedDate;
    }

    /**
     * only called from PhotoTable, XMLTable
     * @param id
     */
    void setId(int id) {
        this.id=id;
    }

    /**
     * @param link the link to set
     */
    void setLink(URL link) {
        this.link = link;
    }
    /** Setting link.
     * This is only called from XML.
     */
    void setLink(String t) {
        try {
            setLink(new URL(t));
        } catch(MalformedURLException ex) {
            System.err.println("This is program bug. The inputed URL is strange: " + t);
        }
    }

    /** Setting Latitude if the state is not BLUE.
     * This is only called from XML.
     */
    void setLat(double lat) {
        if(state==STATE_BLUE){
            return;
        }
        latitude = lat;
    }

    /** Setting Longitude if the state is not BLUE.
     * This is only called from XML.
     */
    void setLon(double lon) {
        if(state==STATE_BLUE){
            return;
        }
        longitude = lon;
    }

    /** only called from XMLTable
     * @param xml the xml to set
     */
    void setXML(XML xml) {
        this.xml = xml;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }


    /**
     * @param thumbnale the thumbnale to set
     */
    public void setThumbnale(URL thumbnale) {
        this.thumbnale = thumbnale;
    }

    public void setThumbnale(String t) {
        try {
            setThumbnale(new URL(t));
        } catch(MalformedURLException ex) {
            System.err.println("This would be program bug. The inputed URL is strange: "+t);
        }
    }
    /**
     * @param original the original to set
     */
    void setOriginal(URL original) {
        this.original = original;
    }
    /** Setting orignal.
     * This is only called from XML.
     */
    void setOriginal(String t) {
        try {
            setOriginal(new URL(t));
        } catch(MalformedURLException ex) {
            System.err.println("This is program bug. The inputed URL is strange: "+t);
        }
    }

    /** only called from PhotoTable, XMLTable
     * @param date the date to set
     */
    void setReadDate(Date date) {
        readDate = date;
    }

    /**
     * @param date the date to set
     */
    void setUpdateDate(Date date) {
        updateDate = date;
    }

    /**
     * @param date the date publishing the photo
     */
    void setPublishedDate(Date date) {
        publishedDate = date;
    }

    void addNode(int id) {
        if(node==null){
            node=new ArrayList<Integer>();
        }
        node.add(id);
    }

    void addWay(int id) {
        if(way==null){
            way=new ArrayList<Integer>();
        }
        way.add(id);
    }

    /** EXIF reader.
     * To run this method, before you have to set feild oringinal.
     */
    public void getEXIF() {
        if(original == null){
            System.err.println("\toriginal is null");
            return;
        }        /* getting EXIF information */

        try {
            URLConnection urlc = original.openConnection();
            int offset = 0, a = 0;
            int length = urlc.getContentLength();
            byte[] buf = new byte[length];
            InputStream is = urlc.getInputStream();
            while(length > 0){
                a = is.read(buf, offset, length);
                length -= a;
                offset += a;
            }
            is.close();
            System.out.println("\tThe original JPEG has been loaded on memory.");
            downloadedDate = new Date();

            Metadata metadata;
            Directory directory = null;
            double lat = 0, lon = 0;
            float f;
            String s;

            JpegSegmentReader segmentReader = new JpegSegmentReader(buf);
            byte[] exifSegment = segmentReader.readSegment(JpegSegmentReader.SEGMENT_APP1);
            byte[] iptcSegment = segmentReader.readSegment(JpegSegmentReader.SEGMENT_APPD);
            metadata = new Metadata();
            new ExifReader(exifSegment).extract(metadata);
            new IptcReader(iptcSegment).extract(metadata);

            directory = metadata.getDirectory(ExifDirectory.class);
            if((s = directory.getString(ExifDirectory.TAG_SOFTWARE)) != null && s.contains("Picasa")){
                state = STATE_RED;
            }
            if(directory.containsTag(ExifDirectory.TAG_FOCAL_LENGTH)){
                try {
                    focalLength = directory.getFloat(ExifDirectory.TAG_FOCAL_LENGTH);
                } catch(MetadataException ex) {
                    System.err.println("Error in EXIF focal length");
                }
                System.out.println("\tfocal length: " + focalLength);
            }
            
            directory = metadata.getDirectory(GpsDirectory.class);

            if(directory.containsTag(GpsDirectory.TAG_GPS_LATITUDE)){
                try {
                    Rational[] rats = directory.getRationalArray(GpsDirectory.TAG_GPS_LATITUDE);
                    lat = rats[0].doubleValue();
                    lat += rats[1].doubleValue() / 60;
                    lat += rats[2].doubleValue() / 3600;
                    if(directory.getString(GpsDirectory.TAG_GPS_LATITUDE_REF).equals("S")){
                        lat = -lat;
                    }
                    System.out.println("\tlat: " + lat);
                } catch(MetadataException ex) {
                    System.out.println("Program error in Photo.getEXIF. " + ex.getMessage());
                }

            }

            if(directory.containsTag(GpsDirectory.TAG_GPS_LONGITUDE)){
                try {
                    Rational[] rats = directory.getRationalArray(GpsDirectory.TAG_GPS_LONGITUDE);
                    lon = rats[0].doubleValue();
                    lon += rats[1].doubleValue() / 60;
                    lon += rats[2].doubleValue() / 3600;
                    if(directory.getString(GpsDirectory.TAG_GPS_LONGITUDE_REF).equals("W")){
                        lon = -lon;
                    }
                    System.out.println("\tlon: " + lon);
                } catch(MetadataException ex) {
                    System.out.println("Program error in Photo.getEXIF. " + ex.getMessage());
                }
            }

            if(directory.containsTag(GpsDirectory.TAG_GPS_ALTITUDE)){
                try {
                    altitude = directory.getFloat(GpsDirectory.TAG_GPS_ALTITUDE);
                    if(directory.getInt(GpsDirectory.TAG_GPS_ALTITUDE_REF) != 0){
                        altitude = -altitude;
                    }
                    System.out.println("\taltitude: " + altitude);
                } catch(MetadataException ex) {
                    System.out.println("EXIF error! at altitude");
                }
            }

            if(directory.containsTag(GpsDirectory.TAG_GPS_IMG_DIRECTION)){
                try {
                    direction = directory.getFloat(GpsDirectory.TAG_GPS_IMG_DIRECTION);
                    System.out.println("\tdirection: " + direction);
                } catch(MetadataException ex) {
                    System.out.println("EXIF error! at Direction");
                }
            }

            if(directory.containsTag(GpsDirectory.TAG_GPS_SPEED)){
                try {
                    s=directory.getString(GpsDirectory.TAG_GPS_SPEED_REF);
                    if (s.startsWith("K")) {
                    speed = directory.getFloat(GpsDirectory.TAG_GPS_SPEED);
                    } else if (s.startsWith("M")) {
                        speed = directory.getFloat(GpsDirectory.TAG_GPS_SPEED) * 1.609344F;
                    } else if (s.startsWith("N")) {
                        speed = directory.getFloat(GpsDirectory.TAG_GPS_SPEED) * 1.852F;
                    }
                } catch(MetadataException ex) {
                    System.out.println("EXIF error! at Speed");
                }
            }

            if(directory.containsTag(GpsDirectory.TAG_GPS_TRACK)){
                try {
                    track = directory.getFloat(GpsDirectory.TAG_GPS_TRACK);
                    System.out.println("\ttrack: " + s);
                } catch(MetadataException ex) {
                    System.out.println("EXIF error! at Speed");
                }
            }

            if(state != STATE_RED && (lat != 0 || lon != 0)){
                this.latitude = lat;
                this.longitude = lon;
                state = STATE_BLUE;
            }
        } catch(IOException ex) {
            System.out.println("\tURL cannot open: " + ex.getMessage());
        } catch(JpegProcessingException ex) {
            System.out.println("\tNo EXIF");
        }
    }

    /**
     * Decoder of A special location code.
     * @param code String of the mappin:at code.
     */
    public void getMappinAt(String code) {
        System.out.println("\tmappin:at=" + code);
        if(code.length()<11){
            System.out.println("Too Short.");
            return;
        }
        int mod;
        int total=0;
        int base=1;
        double lat,lon=0;
        int c;
        for(int i=0;i<11;i++){
            if((mod=base36str.indexOf(code.charAt(i)))<0){
                return;
            }
            if(i!=5){
                total += mod * base;
                base *= 36;
            } else {
                c=(mod%6);
                total += c*base;
                lon=total*1e-6D-180D;
                total = mod-c;
                base=36;
            }
        }
        lat = (total / 6) *1e-6D- 90D;
        latitude = lat;
        longitude = lon;
        System.out.println("\tlatitude=" + latitude);
        System.out.println("\tlongitude=" + longitude);
        state = STATE_BLUE;
    }

    /** Output JavaScript code. It will be called from Tile.
     * @param pw PrintWriter given by tile. It is used for the output.
     */
    boolean toJavaScript(PrintWriter pw) {
        if(deleted){
            return false;
        } else if(latitude == 0 && longitude == 0){
            return false;
        }
        pw.print(id+":{la:" + df.format(latitude) +
                ",lo:" + df.format(longitude) +
                ",s:" + state +
                ",li:'" + link + "'" +
                ",r:'" + xml.getLink() + "'");
        if(title != null){
            pw.print(",ti:'" + title + "'");
        }
        if(original != null){
            pw.print(",o:'" + original + "'");
        }
        if(thumbnale != null){
            pw.print(",th:'" + thumbnale + "'");
        }
        if(altitude != -1000){
            pw.print(",al:" + altitude );
        }
        if(direction != -1000){
            pw.print(",di:" + direction );
        }
        if(speed != -1000){
            pw.print(",sp:" + speed );
        }
        if(track!= -1000){
            pw.print(",tr:" + track );
        }
        if(focalLength!=0){
            pw.print(",av:" + Math.round(Math.atan(18/focalLength)*180/Math.PI));
        }
        if(node != null){
            pw.print(",n:" + node.toString());
        }
        if(way != null){
            pw.print(",w:" + way.toString());
        }
        pw.print("}");
        xml.addCount();
        return true;
    }
    
    /** only called from PhotoTable
     * @param pw PrintWriter given from PhotoTable
     */
    boolean save(PrintWriter pw){
        if(deleted){
            return false;
        }
        pw.print(id + ":{la:" + latitude +
                ",lo:" + longitude +
                ",li:'" + link + "'" +
                ",r:" + xml.getId() +
                ",s:" + state);
        if (title != null) {
            pw.print(",ti:'" + title + "'");
        }
        if (original != null) {
            pw.print(",o:'" + original + "'");
        }
        if (thumbnale != null) {
            pw.print(",th:'" + thumbnale + "'");
        }
        if(node != null){
            pw.print(",n:[");
            Integer i;
            Iterator<Integer> ii=node.iterator();
            while(true){
                i = ii.next();
                pw.print(i);
                if(ii.hasNext()){
                    pw.print(",");
                } else{
                    break;
                }
            }
            pw.print("]");
        }
        if(way != null){
            pw.print(",w:[");
            Integer i;
            Iterator<Integer> ii=way.iterator();
            while(true){
                i = ii.next();
                pw.print(i);
                if(ii.hasNext()){
                    pw.print(",");
                } else{
                    break;
                }
            }
            pw.print("]");
        }
        if (altitude != -1000) {
            pw.print(",al:" + altitude);
        }
        if (direction != -1000) {
            pw.print(",di:" + direction);
        }
        if (speed != -1000) {
            pw.print(",sp:" + speed);
        }
        if (track != -1000) {
            pw.print(",tr:" + track);
        }
        if(focalLength!=0){
            pw.print(",fl:" + focalLength);
        }
        if (readDate != null) {
            pw.print(",readDate:" + readDate.getTime()/1000 );
        }
        if (updateDate != null) {
            pw.print(",updateDate:" + updateDate.getTime()/1000 );
        }
        if (downloadedDate != null) {
            pw.print(",downloaded:" + downloadedDate.getTime()/1000 );
        }
        if (publishedDate != null) {
            pw.print(",published:" + publishedDate.getTime()/1000 );
        }
        pw.print("}");
        return true;
    }

    /** only called from PhotoTable
     * @param line 1 line string
     * @param xt XMLTable to specify XML by ID number.
     * @return new <code>Photo</code> instance. It returns <code>null</code>, when the line don't has id number.
     */
    static Photo load(String line, XMLTable xt) throws
            StringIndexOutOfBoundsException, NumberFormatException {
        boolean end=true;
        int a,b,c;
        String key,value;
        Photo ret=new Photo();
        a = line.indexOf(':');
        if(a < 0){
            return null;
        }
        ret.id = Integer.parseInt(line.substring(0, a));
        line = line.substring(line.indexOf('{', a + 1) + 1, line.lastIndexOf('}'));
        a = 0;
        do {
            b = line.indexOf(':', a + 1);
            if(b < 0){
                break;
            }
            key = line.substring(a, b);
            if(line.charAt(b + 1) == '\''){
                c = line.indexOf('\'', b + 2);
                value = line.substring(b + 2, c);
                c++;
            } else if(line.charAt(b + 1) == '['){
                c = line.indexOf(']', b + 2);
                value = line.substring(b + 2, c);
                c++;
            } else {
                c = line.indexOf(',', b + 1);
                if(c < 0){
                    end = false;
                    value = line.substring(b + 1);
                } else {
                    value = line.substring(b + 1, c);
                }
            }
            if(key.equals("readDate")){
                ret.readDate = new Date(Long.parseLong(value) * 1000);
            } else if(key.equals("updateDate")){
                ret.updateDate = new Date(Long.parseLong(value) * 1000);
            } else if(key.equals("downloaded")){
                ret.downloadedDate = new Date(Long.parseLong(value) * 1000);
            } else if(key.equals("published")){
                ret.publishedDate = new Date(Long.parseLong(value) * 1000);
            } else if(key.equals("la")){
                ret.latitude = Double.parseDouble(value);
            } else if(key.equals("lo")){
                ret.longitude = Double.parseDouble(value);
            } else if(key.equals("al")){
                ret.altitude = Float.parseFloat(value);
            } else if(key.equals("di")){
                ret.direction = Float.parseFloat(value);
            } else if(key.equals("sp")){
                ret.speed = Float.parseFloat(value);
            } else if(key.equals("tr")){
                ret.track = Float.parseFloat(value);
            } else if(key.equals("fl")){
                ret.focalLength = Float.parseFloat(value);
            } else if(key.equals("r")){
                ret.xml = xt.get(Integer.parseInt(value));
                if(ret.xml == null){
                    ret.deleted = true;
                    System.out.println("removing photo id: "+ret.id);
                }
            } else if(key.equals("li")){
                try {
                    ret.link = new URL(value);
                } catch(MalformedURLException ex) {
                    System.err.println("Illigal URL?");
                }
            } else if(key.equals("o")){
                try {
                    ret.original = new URL(value);
                } catch(MalformedURLException ex) {
                    System.err.println("Illigal URL?");
                }
            } else if(key.equals("th")){
                try {
                    ret.thumbnale = new URL(value);
                } catch(MalformedURLException ex) {
                    System.err.println("Illigal URL?");
                }
            } else if(key.equals("n")){
                ret.node = new ArrayList<Integer>();
                int s=0,e;
                while((e=value.indexOf(",", s))>0){
                    ret.node.add(Integer.parseInt(value.substring(s,e)));
                    s = e+1;
                }
                ret.node.add(Integer.parseInt(value.substring(s)));
            } else if(key.equals("w")){
                ret.way = new ArrayList<Integer>();
                int s=0,e;
                while((e=value.indexOf(",", s))>0){
                    ret.way.add(Integer.parseInt(value.substring(s,e)));
                    s = e + 1;
                }
                ret.way.add(Integer.parseInt(value.substring(s)));
            } else if(key.equals("ti")){
                ret.title=value;
            } else if(key.equals("s")){
                ret.state=Integer.parseInt(value);
            }
            a = c + 1;
        } while(end);
        return ret;
    }

    /** Specially called from FlickrProtocal.getExifParameters()
     * @param state state
     */
    void setRed() {
        this.state=STATE_RED;
}

    void setEXIFLat(double lat) {
        if(state!=STATE_RED){
            this.latitude = lat;
            state=STATE_BLUE;
        } else if(this.latitude==0){
            this.latitude = lat;
        }
    }

    void setEXIFLon(double lon) {
        if(state!=STATE_RED){
            this.longitude = lon;
            state=STATE_BLUE;
        } else if(this.longitude==0){
            this.longitude = lon;
        }
    }

    void setAltitude(float alt){
        this.altitude=alt;
    }

    void setDirection(float dir){
        this.direction=dir;
    }

    void setTrack(float track){
        this.track=track;
    }

    void setFocalLength(float fl) {
        focalLength=fl;
    }

    void setSpeed(float s) {
        speed=s;
    }

    void setDownloadDate(Date date) {
        downloadedDate = date;
    }

    /**
     * @param newPhoto the newPhoto to set
     */
    public void setNewPhoto(boolean newPhoto) {
        this.newPhoto = newPhoto;
    }

    /**
     * @return the reread
     */
    public boolean isReread() {
        return reread;
    }

    /**
     * @return the reread
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * @param reread the reread to set
     */
    public void setReread(boolean reread) {
        this.reread = reread;
    }
    /** Today's new photos. It is also 
     *
     * @param pw PrintWriter for writing Today's new photos
     * @param root URL of the base of site
     * @return true if the photo is
     */
    Boolean toRSS(PrintWriter pw, URL root) {
        if(deleted){
            return false;
        } else if(newPhoto && (latitude != 0 || longitude != 0)){
            pw.print("<item><title>" + title + "</title>");
            pw.print("<pubDate>" + new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.UK).format(readDate) + "</pubDate>");
            pw.print("<description><![CDATA[");
            pw.print("<img src=\"" + thumbnale + "\" /><br/>");
            String code = "";
            int i;
            int digit=((int)((longitude+180)*1000000));
            int mod;
            for(i=0;i<4;i++){
                mod=digit % 64;
                code +=base64.charAt(mod);
                digit /= 64;
            }
            mod=digit;
            digit=(int)((latitude+90.0)*1000000.0);
            code += base64.charAt((digit % 2) * 32 + mod);
            digit /= 2;
            for(i=0;i<4;i++){
                mod = (int)(digit & 0x3f);
                code += base64.charAt(mod);
                digit /= 64;
            }
            digit = (id * 16) + digit;
            while(digit > 0){
                mod = (int)(digit % 64);
                code += base64.charAt(mod);
                digit /= 64;
            }
            pw.print("<a href=\"http://mappin.hp2.jp/s?" + code+ "\">lat=" + df.format(latitude) + ", lon=" + df.format(longitude) +"</a><br/>");
            pw.print("<a href=\"" + link + "\">link</a><br/>");
            pw.print("]]></description>");
            pw.print("<link>http://mappin.hp2.jp/s?" + code+ "</link>");
            pw.print("<georss:point>"+latitude+" "+longitude+"</georss:point>");
            pw.println("</item>");
            return true;
        } else {
            return false;
        }
    }
}
