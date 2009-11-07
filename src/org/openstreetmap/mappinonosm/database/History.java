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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author nazo
 */
public class History implements Comparable<History> {
    static private SimpleDateFormat htmlDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    static private SimpleDateFormat rssDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",Locale.UK);
    static private SimpleDateFormat fileDateFormat = new SimpleDateFormat("MMddHHmmss");
    static {
        rssDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        htmlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        fileDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /** the date of last updated */
    protected Date date=null;
    /** number of added photo*/
    protected int numOfNewPhotos=0;
    /** number of all photo*/
    protected int numOfPhoto=0;
    /** number of RSS*/
    protected int numOfRSS=0;
    /** number of photo removed */
    protected int numOfRemoved = 0;
    /** number of photo reread */
    protected int numOfReread = 0;
    /** ID */
    protected int id=0;

    /**
     * Make a instance of History. It is selected from subclass of XML by the scheame.
     * @param line 1 line string
     * @return instance of XML. If it is invaled scheame, it returens null.
     */
    static public History load(String line){
        boolean end = true;
        int a, b, c, id;
        String key, value=null;
        History ret = new History();

        a = line.indexOf(':');
        if (a < 0) {
            return null;
        }
        id = Integer.parseInt(line.substring(0, a));
        line = line.substring(line.indexOf('{', a + 1) + 1, line.lastIndexOf('}'));

        ret.id = id;
        a=0;c=0;
        do {
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
            if(key.equals("date")){
                ret.date = new Date(Long.parseLong(value) * 1000);
            } else if(key.equals("numOfRSS")){
                ret.numOfRSS = Integer.parseInt(value);
            } else if(key.equals("numOfPhoto")){
                ret.numOfPhoto = Integer.parseInt(value);
            } else if(key.equals("numOfNewPhoto")){
                ret.numOfNewPhotos = Integer.parseInt(value);
            } else if(key.equals("numOfRemoved")){
                ret.numOfRemoved = Integer.parseInt(value);
            } else if(key.equals("numOfReread")){
                ret.numOfReread = Integer.parseInt(value);
            }
            a = c + 1;
        } while(end);
        return ret;
    }

    public History() {
        date=new Date();
    }

    @Override
    public int hashCode() {
        return date.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        final History other = (History)obj;
        if(date != null){
            return date.equals(other.date);
        }
        return false;
    }

    public int compareTo(History other) {
        if(equals(other)){
            return 0;
        }
        return other.date.compareTo(date);// descending order
    }

    public int getId() {
        return id;
    }
    public Date getDate(){
        return date;
    }
    public void setId(int id) {
        this.id=id;
    }

    public void setDate() {
        this.date=new Date();
    }

    public void setNumOfPhoto(int n) {
        this.numOfPhoto=n;
    }

    public void setNumOfRSS(int n) {
        this.numOfRSS=n;
    }

    public void setNumOfNewPhoto(int n) {
        this.numOfNewPhotos=n;
    }

    public void setNumOfRemoved(int n) {
        this.numOfRemoved=n;
    }

    public void setNumOfReread(int n) {
        this.numOfReread=n;
    }
    void toRSS(PrintWriter pw,String base) {
        pw.println("<item>");
        pw.println("<title>" + htmlDateFormat.format(date) + "</title>");
        pw.println("<link>" + base + getBackupFileName() + "</link>");
        pw.println("<description><![CDATA[");
        pw.print("RSS: "+numOfRSS+", ");
        pw.print("Photo: " + numOfPhoto + ", ");
        pw.print("New Photo: " + numOfNewPhotos + ", ");
        pw.print("Removed Photo: " + numOfRemoved + ", ");
        pw.print("Updated Photo:" + numOfReread);
        pw.println("]]></description>");
        pw.println("<pubDate>" + rssDateFormat.format(date) + "</pubDate>");
        pw.println("</item>");
    }

    void save(PrintWriter pw) {
        pw.print(id+":{");
        pw.print("date:"+(date.getTime()/1000)+",");
        pw.print("numOfRSS:"+numOfRSS+",");
        pw.print("numOfPhoto:"+numOfPhoto+",");
        pw.print("numOfNewPhoto:"+numOfNewPhotos+",");
        pw.print("numOfRemoved:"+numOfRemoved+",");
        pw.print("numOfReread:"+numOfReread+",");
        pw.print("}");
    }

    public String getBackupFileName() {
        return "photo-" + fileDateFormat.format(date) + ".json.gz";
    }

    void toHTML(PrintWriter pw,String base){
        pw.print("<td>"+htmlDateFormat.format(date)+"</td>");
        String backup = getBackupFileName();
        pw.print("<td><a href=\""+base+backup+"\">"+backup+"</a></td>");
        pw.print("<td class=\"number\">"+numOfPhoto+"</td>");
        pw.print("<td class=\"number\">"+numOfRSS+"</td>");
        pw.print("<td class=\"number\">"+numOfNewPhotos+"</td>");
        pw.print("<td class=\"number\">"+numOfRemoved+"</td>");
        pw.print("<td class=\"number\">"+numOfReread+"</td>");
    }
}
