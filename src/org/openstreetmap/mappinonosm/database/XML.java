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

import java.io.PrintStream;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.xml.sax.ext.DefaultHandler2;

/**
 *
 * @author nazo
 */
abstract public class XML extends DefaultHandler2 implements Comparable<XML>{
    static private SimpleDateFormat htmlDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    static{
        htmlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    /** for event */
    protected  PhotoTable pb;
    /** for recode date */
    protected Date recodeDate=null;
    /** for read date */
    protected Date readDate=null;
    /** for title */
    protected String title=null;
    /** URL of the RSS*/
    protected URI uri;
    /** id given by RSSBase */
    protected int id=0;
    protected int counter=0;

    /** public constractor
     * @param u 
     */
    XML(URI u) {
        uri=u;
        recodeDate=new Date();
    }
    /** Called form XMLBase only.
     * @param id integer id number 
     */
    XML(int id) {
        this.id = id;
    }
    /**
     * making instance from URI
     * @param uri the uri
     * @return Instance of subclasses. It returns null when the sutable scheam not found.
     */
    static public XML getInstance(URI uri){
        if(uri.getScheme().equals("flickr")){
            return new FlickrProtocal(uri);
        } else if(uri.getScheme().equals("http")){
            return new RSS(uri);
        }
        return null;
    }

    /**
     * 
     * @return
     */
    public URI getURL() {
        return uri;
    }
    public void setPhotoBase(PhotoTable pb) {
        this.pb=pb;
    }

    void addCount() {
        counter++;
    }
    /**  only called from RSSBase */
    void setRecodedDate(Date date) {
        recodeDate=date;
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
        return recodeDate;
    }
    public Date getReadDate() {
        return readDate;
    }
    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }
    /** called from RSSBase */
    void setId(int id) {
        this.id = id;
    }
    abstract void read();
    /**
     * Called from XMLBase
     * @param line 1 line string
     * @param rb parent datatable
     */
    void load(String line, PhotoTable pb){
        boolean end = true;
        int a, b, c;
        String key, value;

        this.pb=pb;
        a = line.indexOf('"');
        c = line.indexOf('"', a + 1);
        try{
            uri = new URI(line.substring(a + 1, c));
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
                    recodeDate = new Date(Long.parseLong(value) * 1000);
                } else if(key.equals("title")){
                    title = value;
                } else if(key.equals("read")){
                    readDate = new Date(Long.parseLong(value) * 1000);
                }
            } while(end);
        } catch(URISyntaxException ex) {
            System.out.println("Illigal URI: " +line.substring(a + 1, c));
        }
    }
    /**
     * Called from XMLBase
     * @param ps PrintStream given form XMLBase
     */
    public void save(PrintStream ps) {
        ps.print(id + ":{url:\"" + uri +
                "\",recoded:" + recodeDate.getTime() / 1000);
        if(title != null){
            ps.print(",title:\"" + title + "\"");
        }
        if(readDate != null){
            ps.print(",read:" + readDate.getTime() / 1000);
        }
        ps.print("}");
    }
    /** Out put to MapPIN'on OSM
     * @param ps This PrintStrem is usually made by RSSBase.
     */
    public void toHTML(PrintStream ps){
        ps.println("<td class=\"number\">"+id+"</td>");
        ps.println("<td class=\"number\">"+counter+"</td>");
        ps.println("<td><a href=\""+uri.toString()+"\">"+title+"</a></td>");
        ps.println("<td>"+htmlDateFormat.format(readDate)+"</td>");
        ps.println("<td>"+htmlDateFormat.format(recodeDate)+"</td>");
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
