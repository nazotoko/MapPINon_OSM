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


import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author nazo
 */
abstract public class XML  implements Comparable<XML>{
    static private SimpleDateFormat htmlDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    static{
        htmlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    /**
     * prep. for event.
     * not recoded.
     * <a href="#setPhotoBase">setPhotoBase(PhotoTable pt)</a>
     */
    protected  PhotoTable photoTable;
    /** Registed date */
    protected Date registeredDate=null;
    /** Read date */
    protected Date readDate=null;
    /** Title of RSS */
    protected String title=null;
    /** URI of the RSS*/
    protected URI uri=null;
    /** URL of main link */
    protected URL link=null;
    
    /** id given by XMLTable */
    protected int id=0;
    /** Counter for geotagged photo */
    protected int counter=0;
    /** Counter for geotagged photo */
    protected int newCounter=0;

    /** for work*/
    protected Photo photo = null;

    /** public constractor
     * @param u 
     */
    protected XML(URI u) {
        uri=u;
        registeredDate=new Date();
    }
    /** Called form XMLBase only.
     * @param id integer id number 
     */
    protected XML(int id) {
        this.id = id;
    }
    /**
     * making instance from URI
     * @param uri the uri
     * @return Instance of subclasses. It returns null when the sutable scheam not found.
     */
    static public XML getInstance(URI uri) {
        if(uri.getScheme().equals("flickr")){
            return new FlickrProtocal(uri);
        } else if(uri.getScheme().equals("http")){
            return new RSS(uri);
        }
        return null;
    }

    /**
     * 
     * @return URI
     */
    public URI getURI() {
        return uri;
    }
    /**
     * XML mast have PhotoTable
     * @param photoTable PhotoTable
     */
    public void setPhotoBase(PhotoTable photoTable) {
        this.photoTable = photoTable;
    }

    void addCount() {
        counter++;
    }
    /**  only called from RSSBase */
    void setRecodedDate(Date date) {
        registeredDate=date;
    }
    /**  only called from RSSBase */
    void setReadDate(Date date) {
        readDate=date;
    }
    /**  only called from RSSBase */
    void setTitle(String title) {
        this.title=title;
    }
    public Date getRecodedDate() {
        return registeredDate;
    }
    public Date getReadDate() {
        return readDate;
    }
    public String getTitle() {
        return title;
    }
    public URL getLink() {
        return link;
    }
    public int getId() {
        return id;
    }
    /** called from RSSBase */
    void setId(int id) {
        this.id = id;
    }

    protected String entity(String input) {
        String ret=input.replaceAll("\n", "<br/>");
        ret=ret.replaceAll("\"", "&quot;");
        ret=ret.replaceAll("'", "&apos;");
        return ret;
    }

    protected void machineTags(String...  st){
        int in = 0;
        for(String s:st){
            if(s.startsWith("osm:")){
                in = 4;
                if(s.startsWith("node=", in)){
                    in += 5;
                    int node = Integer.parseInt(s.substring(in));
                    photo.addNode(node);
                } else if(s.startsWith("way=", in)){
                    in += 4;
                    int way = Integer.parseInt(s.substring(in));
                    photo.addWay(way);
                }
            }
            if(s.startsWith("mappin:")){
                in = 7;
                if(s.startsWith("at=", in)){
                    in += 3;
                    photo.getMappinAt(s.substring(in));
                }
            }
        }
    }

    abstract int read();

    /**
     * Make a instance of XML. It is selected from subclass of XML by the scheame.
     * @param line 1 line string
     * @param pb PhotoTable object. It is needed to make instance of XML object.
     * @return instance of XML. If it is invaled scheame, it returens null.
     */
    static public XML load(String line, PhotoTable pb){
        boolean end = true;
        int a, b, c, id;
        String key, value=null;
        XML ret = null;
        URI uri;
        a = line.indexOf(':');
        if (a < 0) {
            return null;
        }
        id = Integer.parseInt(line.substring(0, a));
        // System.err.println("Load-RSS ID: "+id);
        line = line.substring(line.indexOf('{', a + 1) + 1, line.lastIndexOf('}'));

        a = line.indexOf('"');
        c = line.indexOf('"', a + 1);
        try{
            uri = new URI(line.substring(a + 1, c));
            ret=getInstance(uri);
            ret.id=id;
            /** This don't add registering Date */
            c++;
            do {
                a = c + 1;
                b = line.indexOf(':', a + 1);
                if(b < 0){
                    break;
                }
                key = line.substring(a, b);
                if(line.charAt(b + 1) == '"'){
                    c = line.indexOf('"', b + 2);
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
                if(key.equals("recoded")){
                    ret.registeredDate = new Date(Long.parseLong(value) * 1000);
                } else if(key.equals("title")){
                    ret.title = value;
                } else if(key.equals("read")){
                    ret.readDate = new Date(Long.parseLong(value) * 1000);
                } else if(key.equals("li")){
                    ret.link = new URL(value);
                }
            } while (end);
        } catch (URISyntaxException ex) {
            System.out.println("Illigal URI: " + line.substring(a + 1, c));
        } catch (MalformedURLException ex){
            System.out.println("Illigal URL: " + value);
        }
        return ret;
    }

    /**
     * Called from XMLBase
     * @param pw PrintWriter given form XMLBase
     */
    public void save(PrintWriter pw) {
        pw.print(id + ":{url:\"" + uri +
                "\",recoded:" + registeredDate.getTime() / 1000);
        if(title != null){
            pw.print(",title:\"" + title + "\"");
        }
        if(readDate != null){
            pw.print(",read:" + readDate.getTime() / 1000);
        }
        if(link != null){
            pw.print(",li:\"" + link+"\"");
        }
        pw.print("}");
    }
    /** Out put to MapPIN'on OSM
     * @param ps This PrintStrem is usually made by RSSBase.
     * @return interger: number of photos having geotags
     */
    public int toHTML(PrintWriter ps){
        ps.println("<td class=\"number\">"+id+"</td>");
        ps.println("<td class=\"number\">"+counter+"</td>");
        ps.println("<td>");
        if(link!=null){
            ps.println("<a href=\""+link.toString()+"\">");
        }
        ps.println(title);
        if(link!=null){
            ps.println("</a>");
        }
        ps.println("</td>");
        ps.println("<td>"+uri.toString()+"</td>");
        ps.println("<td>"+htmlDateFormat.format(readDate)+"</td>");
        ps.println("<td>"+htmlDateFormat.format(registeredDate)+"</td>");
        return counter;
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        final XML other = (XML)obj;
        if(this.uri != null){
            return this.uri.equals(other.uri);
        }
        return false;
    }

    public int compareTo(XML other) {
        if(equals(other)){
            return 0;
        }
        return (other.id - id);// descending order
    }
}
