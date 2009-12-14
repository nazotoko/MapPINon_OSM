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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Iterator;

/**
 *
 * @author nazo
 */
public class HistoryTable extends TreeSet <History>{
    private int maxId = 0;
    private URL root=null;
    private String backupDir=null;
    private String historyRSS=null;
    private String historyList=null;
    /** the History shuld have date
     * @param his
     */
    @Override
    public boolean add(History his) {
        if (super.add(his) == false) {
            return false;
        }
        if(his.getId()==0){
            maxId++;
            his.setId(maxId);
        } else if(maxId < his.getId()){
            maxId=his.getId();
        }
        return true;
    }
    /**
     * do it before
     * @param u
     */
    public void setRoot(URL u) {
        root=u;
    }
    /**
     * do it before
     * @param dir
     */
    public void setBackupDir(String dir) {
        backupDir=dir;
    }
    /**
     * do it before
     * @param file
     */
    public void setHistoryList(String file) {
        historyList=file;
    }
    /**
     * do it before
     * @param file
     */
    public void setHistoryRSS(String file) {
        historyRSS=file;
    }

    public void toRSS(OutputStream os) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<rss version=\"2.0\">");
            pw.println("<channel>");
            pw.println("<title>Updating history of MapPIN'on OSM</title>");
//            pw.println("<pubDate>" + new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.UK).format(new Date()) + "</pubDate>");
            pw.println("<lastBuildDate>" + new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.UK).format(new Date()) + "</lastBuildDate>");
            pw.println("<link>"+getRoot()+historyList+"</link>");
            pw.println("<language>en</language>");
            History h;
            int i=0;
            Iterator <History> it=iterator();
            while(it.hasNext() && i < 14){
                h = it.next();
                h.toRSS(pw, getRoot() + backupDir);
                i++;
            }
            pw.println("</channel>");
            pw.println("</rss>");
            pw.flush();
        } catch(UnsupportedEncodingException ex) {
            System.err.println("This system cannot supuuprt UTF-8.:"+ex.getMessage());
        }
    }
    public void toHTML(OutputStream os) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
            pw.println("<html lang=\"en\"><head>");
            pw.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>");
            pw.println("<meta http-equiv=\"Content-Language\" content=\"en\"/>");
            pw.println("<link rel=\"alternate\" type=\"application/rss+xml\" title=\"History RSS Feed\" href=\""+getRoot()+historyRSS+"\" />");
            pw.println("<link rel=\"stylesheet\" href=\"../css/list.css\" type=\"text/css\"/>");
            pw.println("<title>Updating history of MapPIN'on OSM</title></head><body>");
            pw.println("<h1>Updating history of MapPIN'on OSM</h1>");
            pw.println("<p><a href=\"/index.html\">back to the map</a>, <a href=\"/blog/\">go to the blog</a></p>");
            pw.println("<p>These backup dataTable are licensed by owners of all Photographs.</p>");
            pw.println("<table><tr>");
            pw.print("<th>Date (UTC)</th>");
            pw.print("<th>All photo table file</th>");
            pw.print("<th># of photos registered</th>");
            pw.print("<th># of RSS</th>");
            pw.print("<th># of new photos</th>");
            pw.print("<th># of photos removed</th>");
            pw.print("<th># of photos reread</th>");
            pw.println("</tr>");
            boolean odd = true;
            History h;
            int i=0;
            Iterator <History> it=iterator();
            while(it.hasNext() && i < 14){
                h = it.next();
                pw.print("<tr");
                if(odd){
                    pw.print(">");
                    odd = false;
                } else {
                    pw.print(" class=\"even\">");
                    odd = true;
                }
                h.toHTML(pw, getRoot() + backupDir);
                pw.println("</tr>");
                i++;
            }
            pw.println("</table>");
            pw.println("<p><a href=\"/index.html\">back to the map</a>, <a href=\"/blog/\">go to the blog</a></p></body></html>");
            pw.flush();
        } catch(UnsupportedEncodingException ex) {
            System.err.println("This system cannot supuuprt UTF-8.:"+ex.getMessage());
        }
    }

    public void load(InputStream is) {
        BufferedReader br=null;
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            History his;
            while(true){
                if((line = br.readLine())==null){
                    break;
                }
                his=History.load(line);
                add(his);
            }
        } catch(UnsupportedEncodingException ex) {
            System.err.println("Program error in XMLTable.load()." + ex.getMessage());
        } catch (IOException ex){
            System.err.println("End?");
        }
    }

    public void save(OutputStream os) {
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
            for(History h: this){
                h.save(pw);
                pw.println(",");
            }
            pw.flush();
        } catch(UnsupportedEncodingException ex) {
            System.out.println("Output Stream cannot open. at "+HistoryTable.class.getName());
        }
    }

    /**
     * @return the root
     */
    public URL getRoot() {
        return root;
    }
}
