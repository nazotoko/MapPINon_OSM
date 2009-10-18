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

import org.openstreetmap.mappinonosm.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author nazo
 */
public class XMLTable extends HashSet<XML>{
    private int maxId=0;
    private PhotoTable pb=null;
    /** must set pb before load
     * @param pb Photobase object you are using.
     */
    public XMLTable(PhotoTable pb) {
        super();
        this.pb = pb;
    }
    /** prohibited */
    private XMLTable(){}

    /** the RSS shuld have url
     * @param rss 
     */
    @Override
    public boolean add(XML rss) {
        if (super.add(rss) == false) {
            return false;
        }
        rss.setPhotoBase(pb);
        if(rss.getId()==0){
            maxId++;
            rss.setId(maxId);
        } else if(maxId < rss.getId()){
            maxId=rss.getId();
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
            XML rss;
            int id;
            int a;
            while(true){
                if((line = br.readLine())==null){
                    break;
                }
                a = line.indexOf(':');
                if(a<0){
                    continue;
                }
                id=Integer.parseInt(line.substring(0, a));
 //               System.err.println("Load-RSS ID: "+id);
                line = line.substring(line.indexOf('{', a + 1)+1,line.lastIndexOf('}'));
                rss = new RSS(id);
                rss.load(line,pb);
                add(rss);
            }
        } catch(UnsupportedEncodingException ex) {
            System.err.println("Syntax");
        } catch (IOException ex){
            System.err.println("End?");
        }
    }

    /** Save informations into a local database
     * @param os OutputStream to be output. this method don't close the stream.
     */
    public void save(OutputStream os) {
        PrintStream ps = new PrintStream(os);
        XML r;
        Iterator <XML> i= this.iterator();
        while(i.hasNext()){
            r = i.next();
            r.save(ps);
            ps.println(",");
        }
    }
    /**
     * read RSSes in the database
     */
    public void read() {
        XMLReader parser;
        for(XML r: this){
            try {
                parser = XMLReaderFactory.createXMLReader();
                parser.setContentHandler(r);
                parser.parse(r.getURL().toString());
            } catch(SAXException ex) {
                Logger.getLogger(MapPINonOSM.class.getName()).log(Level.SEVERE, null, ex);
            } catch(IOException ex) {
                Logger.getLogger(MapPINonOSM.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void toHTML(OutputStream os){
        PrintStream ps = new PrintStream(os);
        ps.println("<html lang=\"en\"><head>");
        ps.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>");
        ps.println("<meta http-equiv=\"Content-Language\" content=\"en\"/>");
        ps.println("<link rel=\"stylesheet\" href=\"css/list.css\" type=\"text/css\"/>");
        ps.println("<title>List of resistered RSSes</title></head><body>");
        ps.println("<h1>List of resistered RSSes</h1>");
        ps.println("<p><a href=\"index.html\">back to the map</a>, <a href=\"blog/\">go to the blog</a></p>");
        ps.println("<p>Timezone of timestamps are of UTC.</p>");
        ps.println("<table><tr><th>id</th><th>number of photos registered</th><th>title</th><th>read date</th><th>registed date</th></tr>");

        boolean odd = true;
        XML [] xmls=toArray(new XML[size()]);
        Arrays.sort(xmls);
        for(XML r: xmls){
            ps.print("<tr");
            
            if(odd){
                ps.print(">");
                odd = false;
            } else {
                ps.print(" class=\"even\">");
                odd = true;
            }
            r.toHTML(ps);
            ps.println("</tr>");
        }
        ps.println("</table>");
        ps.println("<p><a href=\"index.html\">back to the map</a>, <a href=\"blog/\">go to the blog</a></p></body></html>");
    }
}
