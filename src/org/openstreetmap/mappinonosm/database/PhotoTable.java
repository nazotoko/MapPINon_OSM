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
import java.util.HashSet;
import java.util.Locale;

/**
 *
 * @author nazo
 */
public class PhotoTable extends HashSet<Photo> {
    private int max=0 ;
            
    /**
     * constractor 
     */
    public PhotoTable(){
    }
    /**
     * 
     * @param p
     * @return boolean
     */
    @Override
    public boolean add(Photo p) {
        if(super.add(p)==false){
            return false;
        }
        if(p.getId()==0){
            max++;
            p.setId(max);
        } else if(max < p.getId()){
            max = p.getId();
        }
        return true;
    }
    /** disused?
     *
     * @param os OutputStream
     */
    public void toJavaScript(OutputStream os) {
        try{
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
            pw.println("AJAXI({");
            for(Photo p: this){
                if(p.toJavaScript(pw)){
                    pw.println(",");
                }
            }
            pw.println("});");
            pw.flush();
        } catch(UnsupportedEncodingException ex) {
            System.out.println("Program error in PhotoTable.save()." + ex.getMessage());
        }
    }
    /** save Photo datatable to OutPutStream
     *@param os the OutputStrem
     */
    public void save(OutputStream os){
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
            for(Photo p: this){
                if(p.save(pw)){
                    pw.println(",");
                }
            }
            pw.flush();
        } catch(UnsupportedEncodingException ex){
            System.out.println("Program error in PhotoTable.save()." + ex.getMessage());
        }
    }
    /**
     * @param is ImputStream oject. The input has photo database text.
     * @param xt XMLTable object.
     */
    public void load(InputStream is, XMLTable xt) {
        Photo p;
        String line;
        int line_number = 0;
        int id=0;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int a;
            while(true){
                if((line = br.readLine())==null){
                    break;
                }
                line_number++;
                p=Photo.load(line,xt);
                add(p);
            }
        } catch(UnsupportedEncodingException ex) {
            System.out.println("Syntax error in the input stream. Check it is written in UTF-8 code.");
        } catch(StringIndexOutOfBoundsException ex) {
            System.out.println("Format illigal at line "+line_number+" ID:"+id+".");
        } catch(NumberFormatException ex){
            System.out.println("ID number is strange at line "+line_number+".");
        } catch (IOException ex){
            System.out.println("The input stream cannot be read.");
        }
    }

    /**
     * Getting the conflict Photo object (which has the same link) with photo.
     * @param photo
     * @return return the conflicted old Photo with photo.
     */
    public Photo get(Photo photo) {
        int hash=photo.hashCode();
        for(Photo entry:this){
            if(entry.hashCode()==hash){
                return entry;
            }
        }
        return null;
    }
    /** makeing RSS, Today's new photo.
     *
     * @param os
     * @param root
     * @param histroy
     */
    public void toRSS(OutputStream os, URL root, History histroy) {
        int numberOfNewPhoto = 0;
        int numberOfReread = 0;
        int numberOfRemoved = 0;
        try{
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<rss version=\"2.0\" xmlns:georss=\"http://www.georss.org/georss\">");
            pw.println("<channel>");
            pw.println("<title>Today's New photos of MapPIN'on OSM</title>");
            pw.println("<lastBuildDate>" + new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.UK).format(histroy.getDate()) + "</lastBuildDate>");
            pw.println("<link>"+root+"</link>");
            for(Photo p: this){
                if(p.toRSS(pw,root)){
                    numberOfNewPhoto++;
                }
                if(p.isReread()){
                    numberOfReread++;
                }
                if(p.isDeleted()){
                    numberOfRemoved++;
                }
            }
            pw.println("</channel>");
            pw.println("</rss>");
            pw.flush();
        } catch(UnsupportedEncodingException ex) {
        }
        histroy.setNumOfNewPhoto(numberOfNewPhoto);
        histroy.setNumOfReread(numberOfReread);
        histroy.setNumOfRemoved(numberOfRemoved);
    }
}
