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
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.GpsDirectory;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcReader;
import java.io.InputStream;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import javax.print.attribute.standard.MediaSize.Other;
/**
 *
 * @author nazo
 */
public class Photo {
    static DecimalFormat df = new DecimalFormat("###.######");
    /** 0 means not set yet */
    protected int id=0;
    /** none null */
    protected XML xml=null;
    /** none 0 */
    protected double latitude=0;
    /** none 0 */
    protected double longitude=0;

        /** 2:red, 1:yellow, 0:green */
//    public static final int STATE_??? = 3;
    public static final int STATE_RED = 2;
    public static final int STATE_YELLOW = 1;
    public static final int STATE_BLUE = 0;
    protected int state = STATE_YELLOW;

    /** URL of link */
    protected URL link=null;
    /** direct URL to origial photo */
    protected URL original=null;
    /** direct URL to thumnale photo */
    protected URL thumnale=null;
    /** title text */
    protected String title=null;
    /** GPS alatitude (meter) */
    protected float alatitude=0;
    /** GPS direction (degree) */
    protected float direction=-1000;
    /** GPS speed (meter) */
    protected float speed=0;

    
    /** the date of last updated */
    protected Date readDate=null;
    /** the date of last updated */
    protected Date updateDate=null;
    /** osm:way tag */
    protected ArrayList<Integer> way = null;
    /** osm:node tag */
    protected ArrayList<Integer> node = null;

    public Photo() {
    }
    /** only called from PhotoBase */
    Photo(int id){
        this.id=id;
    }

    @Override
    public int hashCode(){
        return link.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }
        final Photo other = (Photo)obj;
        if(this.link.hashCode() != other.link.hashCode()){
            return false;
        }
        return true;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the link
     */
    public URL getLink() {
        return link;
    }

    /**
     * @param link the link to set
     */
    public void setLink(URL link) {
        this.link = link;
    }

    /**
     * @return the xml
     */
    public XML getXML() {
        return xml;
    }

    /**
     * @param xml the xml to set
     */
    public void setXML(XML xml) {
        this.xml = xml;
    }

    /**
     * @return the thumnale
     */
    public URL getThumnale() {
        return thumnale;
    }

    /**
     * @param thumnale the thumnale to set
     */
    public void setThumnale(URL thumnale) {
        this.thumnale = thumnale;
    }

    public void setThumnale(String t) {
        try {
            setThumnale(new URL(t));
        } catch(MalformedURLException ex) {
            Logger.getLogger(Photo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the original
     */
    public URL getOriginal() {
        return original;
    }

    /**
     * @param original the original to set
     */
    public void setOriginal(URL original) {
        this.original = original;
    }

    /**
     * @return the date
     */
    public Date getReadDate() {
        return readDate;
    }

    /**
     * @param date the date to set
     */
    public void setReadDate(Date date) {
        readDate = date;
    }

    /**
     * @return the date
     */
    public Date getUpdateDate() {
        return updateDate;
    }

    /**
     * @param date the date to set
     */
    public void setUpdateDate(Date date) {
        updateDate = date;
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
    /** updating information by new information gotten from RSS. */
    void upDate(Photo photo) {
        title=photo.title;
        readDate=photo.readDate;
        thumnale=photo.thumnale;
        original=photo.original;
        node=photo.node;
        way=photo.way;

        if( (state != STATE_BLUE || photo.state==STATE_BLUE) && (photo.latitude!=0||photo.longitude!=0)){
            latitude=photo.latitude;
            longitude=photo.longitude;
            if( photo.state==STATE_BLUE){
                state = photo.state;
            }
        }
    }

    /**
     * only called from PhotoBase, XMLTable
     * @param id 
     */
    void setId(int id) {
        this.id=id;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    public double getLat() {
        return latitude;
    }

    public void setLat(double lat) {
        if(state==STATE_BLUE){
            return;
        }
        latitude = lat;
    }

    public double getLon() {
        return longitude;
    }

    public void setLon(double lon) {
        if(state==STATE_BLUE){
            return;
        }
        longitude = lon;
    }

    void setLink(String textBuffer) {
        try {
            setLink(new URL(textBuffer));
        } catch(MalformedURLException ex) {
            Logger.getLogger(Photo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void setOriginal(String value) {
        try {
            setOriginal(new URL(value));
        } catch(MalformedURLException ex) {
            Logger.getLogger(Photo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
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
            System.out.println("\tThe original JPEG has been read.");

            Metadata metadata;
            Directory directory = null;
            double lat = 0, lon = 0;
            String s;
            String[] spaced;
            String[] digit;

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
            directory = metadata.getDirectory(GpsDirectory.class);
            if(directory.getTagCount() > 0){
                System.out.println("\tGet EXIF GPS tags!");

                if((s = directory.getString(GpsDirectory.TAG_GPS_LATITUDE)) != null){
                    System.out.println("\tlat_dev: " + s);
                    spaced = s.split(" ");
                    digit = spaced[0].split("/");
                    lat = Double.parseDouble(digit[0]) / Double.parseDouble(digit[1]);
                    digit = spaced[1].split("/");
                    lat += Double.parseDouble(digit[0]) / Double.parseDouble(digit[1]) / 60;
                    digit = spaced[2].split("/");
                    lat += Double.parseDouble(digit[0]) / Double.parseDouble(digit[1]) / 3600;
                    if(directory.getString(GpsDirectory.TAG_GPS_LATITUDE_REF).equals("S")){
                        lat = -lat;
                    }
                }

                if((s = directory.getString(GpsDirectory.TAG_GPS_LONGITUDE)) != null){
                    System.out.println("\tlon_dev: " + s);
                    spaced = s.split(" ");
                    digit = spaced[0].split("/");
                    lon = Double.parseDouble(digit[0]) / Double.parseDouble(digit[1]);
                    digit = spaced[1].split("/");
                    lon += Double.parseDouble(digit[0]) / Double.parseDouble(digit[1]) / 60;
                    digit = spaced[2].split("/");
                    lon += Double.parseDouble(digit[0]) / Double.parseDouble(digit[1]) / 3600;
                    if(directory.getString(GpsDirectory.TAG_GPS_LONGITUDE_REF).equals("W")){
                        lon = -lon;
                    }
                }
//            System.out.println("Alatitude" + directory.getString(GpsDirectory.TAG_GPS_ALTITUDE));
//            System.out.println("Alatitude_REF" + directory.getString(GpsDirectory.TAG_GPS_ALTITUDE_REF));
                if(state != STATE_RED && (lat != 0 || lon != 0)){
                    this.latitude = lat;
                    this.longitude = lon;
                    state = STATE_BLUE;
                }
            }/* end of GPS tag reading */
        } catch(IOException ex) {
            System.out.println("\tURL cannot open: " + ex.getMessage());
        } catch(JpegProcessingException ex) {
            System.out.println("\tNo EXIF");
        }
    }
    public void getMappinAt(String code) {
        String base36str = "0123456789abcdefghijklmnopqrstuvwxyz";
        System.out.println("\tmappin:at=" + code);
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

    /** Output JavaScript code. It will be called from a marker tile.
     * @param ps PrintStream for the output.
     */
    void toJavaScript(PrintStream ps) {
        ps.print(id+":{la:" + df.format(latitude) +
                ",lo:" + df.format(longitude) +
                ",r:'" + xml.getURL() + "'" +
                ",s:" + state);
        if(title != null){
            ps.print(",ti:'" + title + "'");
        }
        if(link != null){
            ps.print(",li:'" + link + "'");
        }
        if(original != null){
            ps.print(",o:'" + original + "'");
        }
        if(thumnale != null){
            ps.print(",th:'" + thumnale + "'");
        }
        if(direction == -1000){
            ps.print(",dir:'" + direction + "'");
        }
        if(node != null){
            ps.print(",n:" + node.toString());
        }
        if(way != null){
            ps.print(",w:" + way.toString());
        }
        ps.print("}");
        xml.addCount();
    }
    
    /** only called from Photobase */
    void save(PrintStream ps){
        ps.print(id+":{la:" + latitude +
                ",lo:" + longitude +
                ",r:" + xml.getId() +
                ",s:" + state);
        if (title != null) {
            ps.print(",ti:'" + title + "'");
        }
        if (link != null) {
            ps.print(",li:'" + link + "'");
        }
        if (original != null) {
            ps.print(",o:'" + original + "'");
        }
        if (thumnale != null) {
            ps.print(",th:'" + thumnale + "'");
        }
        if(node != null){
            ps.print(",n:[");
            Integer i;
            Iterator<Integer> ii=node.iterator();
            while(true){
                i = ii.next();
                ps.print(i);
                if(ii.hasNext()){
                    ps.print(",");
                } else{
                    break;
                }
            }
            ps.print("]");
        }
        if(way != null){
            ps.print(",w:[");
            Integer i;
            Iterator<Integer> ii=way.iterator();
            while(true){
                i = ii.next();
                ps.print(i);
                if(ii.hasNext()){
                    ps.print(",");
                } else{
                    break;
                }
            }
            ps.print("]");
        }
        if (readDate != null) {
            ps.print(",readDate:" + readDate.getTime()/1000 );
        }
        if (updateDate != null) {
            ps.print(",updateDate:" + updateDate.getTime()/1000 );
        }
        ps.print("}");
    }

    /** only called from Photobase
     * @param line 1 line string
     * @param rb parent datatable
     */
    void load(String line,XMLTable rb){
        boolean end=true;
        int a,b,c;
        String key,value;
        a=0;
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
                readDate=new Date(Long.parseLong(value)*1000);
            } else if(key.equals("updateDate")){
                updateDate=new Date(Long.parseLong(value)*1000);
            } else if(key.equals("la")){
                latitude=Double.parseDouble(value);
            } else if(key.equals("lo")){
                longitude=Double.parseDouble(value);
            } else if(key.equals("r")){
                xml=rb.get(Integer.parseInt(value));
            } else if(key.equals("li")){
                try {
                    link = new URL(value);
                } catch(MalformedURLException ex) {
                    System.err.println("Illigal URL?");
                }
            } else if(key.equals("o")){
                try {
                    original = new URL(value);
                } catch(MalformedURLException ex) {
                    System.err.println("Illigal URL?");
                }
            } else if(key.equals("th")){
                try {
                    thumnale = new URL(value);
                } catch(MalformedURLException ex) {
                    System.err.println("Illigal URL?");
                }
            } else if(key.equals("n")){
                node = new ArrayList<Integer>();
                int s=0,e;
                while((e=value.indexOf(",", s))>0){
                    node.add(Integer.parseInt(value.substring(s,e)));
                    s = e+1;
                }
                node.add(Integer.parseInt(value.substring(s)));
            } else if(key.equals("w")){
                way = new ArrayList<Integer>();
                int s=0,e;
                while((e=value.indexOf(",", s))>0){
                    way.add(Integer.parseInt(value.substring(s,e)));
                    s = e + 1;
                }
                way.add(Integer.parseInt(value.substring(s)));
            } else if(key.equals("ti")){
                title=new String(value);
            } else if(key.equals("s")){
                state=Integer.parseInt(value);
            }
            a = c + 1;
        } while(end);
    }
}
