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
import java.util.TreeSet;

/**
 *
 * @author nazo
 */
public class HistoryTable extends TreeSet <History>{
    private int maxId = 0;
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

    public void toRSS(OutputStream os) {
    }
    public void toHTML(OutputStream os) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
            pw.println("<html lang=\"en\"><head>");
            pw.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>");
            pw.println("<meta http-equiv=\"Content-Language\" content=\"en\"/>");
            pw.println("<link rel=\"stylesheet\" href=\"css/list.css\" type=\"text/css\"/>");
            pw.println("<title>History</title></head><body>");
            pw.println("<h1>List of resistered RSSes</h1>");
            pw.println("<p><a href=\"index.html\">back to the map</a>, <a href=\"blog/\">go to the blog</a></p>");
            pw.println("<p>Timezone of timestamps are of UTC.</p>");
            pw.println("<table><tr><th>Date</th><th>Number of photos registered</th><th># of RSS</th><th># of new photos</th></tr>");
            boolean odd = true;
            for(History h: this){
                pw.print("<tr");
                if(odd){
                    pw.print(">");
                    odd = false;
                } else {
                    pw.print(" class=\"even\">");
                    odd = true;
                }
                h.toHTML(pw);
                pw.println("</tr>");
            }
            pw.println("</table>");
            pw.println("<p><a href=\"index.html\">back to the map</a>, <a href=\"blog/\">go to the blog</a></p></body></html>");
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
}
