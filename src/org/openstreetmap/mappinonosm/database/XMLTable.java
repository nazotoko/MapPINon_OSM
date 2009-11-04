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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import com.aetrion.flickr.Flickr;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
/**
 *
 * @author nazo
 */
public class XMLTable extends HashSet<XML>{
    private int maxId=0;
    private PhotoTable pb=null;
    private Flickr flickr=null;
    /** must set pb before load
     * @param pb Photobase object you are using.
     */
    public XMLTable(PhotoTable pb) {
        super();
        this.pb = pb;
    }
    /** prohibited */
    private XMLTable(){}
    /**
     * @param key Flickr's API key
     * @param secret Flickr's API secret key
     */
    public void setFlickrKeys(String key, String secret) {
        flickr=new Flickr(key);
        flickr.setSharedSecret(secret);
    }
    /** the RSS shuld have url
     * @param xml
     */
    @Override
    public boolean add(XML xml) {
        if (super.add(xml) == false) {
            return false;
        }
        if(xml instanceof FlickrProtocal){
            ((FlickrProtocal)xml).setFlickr(flickr);
        }
        xml.setPhotoBase(pb);
        if(xml.getId()==0){
            maxId++;
            xml.setId(maxId);
        } else if(maxId < xml.getId()){
            maxId=xml.getId();
        }
        return true;
    }

    /** get a XML entry by ID*/
    XML get(int id) {
        for(XML entry:this){
            if(entry.getId()==id){
                return entry;
            }
        }
        return null;
    }
    /**
     * 
     * @param is Inputsteam of savedparaeters.
     */
    public void load(InputStream is) {
        BufferedReader br=null;
        String line;
        if(pb==null){
            System.err.println("Cannot load any rss without PhotoBase set.");
            return;
        }
        try {
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            XML xml;
            while(true){
                if((line = br.readLine())==null){
                    break;
                }
                xml=XML.load(line,pb);
                add(xml);
            }
        } catch(UnsupportedEncodingException ex) {
            System.err.println("Program error in XMLTable.load()." + ex.getMessage());
        } catch (IOException ex){
            System.err.println("End?");
        }
    }

    /** Save informations into a local database
     * @param os OutputStream to be output. this method don't close the stream.
     */
    public void save(OutputStream os) {
        try{
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
            XML x;
            Iterator<XML> i = this.iterator();
            while(i.hasNext()){
                x = i.next();
                x.save(pw);
                pw.println(",");
            }
            pw.flush();
        } catch(UnsupportedEncodingException ex) {
            System.err.println("Program error in XMLTable.save()." + ex.getMessage());
        }
    }
    /**
     * read RSSes in the database
     * @return integer: number of new photos
     */
    public int read() {
        int newPhoto=0;
        for(XML x: this){
            newPhoto+=x.read();
        }
        return newPhoto;
    }
    public int toHTML(OutputStream os){
        PrintWriter pw = null;
        int numberOfPhotos=0;
        try {
            pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
            pw.println("<html lang=\"en\"><head>");
            pw.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>");
            pw.println("<meta http-equiv=\"Content-Language\" content=\"en\"/>");
            pw.println("<link rel=\"stylesheet\" href=\"css/list.css\" type=\"text/css\"/>");
            pw.println("<title>List of resistered RSSes</title></head><body>");
            pw.println("<h1>List of resistered RSSes</h1>");
            pw.println("<p><a href=\"index.html\">back to the map</a>, <a href=\"blog/\">go to the blog</a></p>");
            pw.println("<p>Timezone of timestamps are of UTC.</p>");
            pw.println("<table><tr><th>ID</th><th>Number of photos registered</th><th>Title</th><th>URI</th><th>Read date</th><th>Registed date</th></tr>");
            boolean odd = true;
            XML[] xmls = toArray(new XML[size()]);
            Arrays.sort(xmls);
            for(XML x: xmls){
                pw.print("<tr");
                if(odd){
                    pw.print(">");
                    odd = false;
                } else {
                    pw.print(" class=\"even\">");
                    odd = true;
                }
                numberOfPhotos+=x.toHTML(pw);
                pw.println("</tr>");
            }
            pw.println("</table>");
            pw.println("<p><a href=\"index.html\">back to the map</a>, <a href=\"blog/\">go to the blog</a></p></body></html>");
            pw.flush();
        } catch(UnsupportedEncodingException ex) {
            System.err.println("This system cannot supuuprt UTF-8.:"+ex.getMessage());
        }
        return numberOfPhotos;
    }
}
