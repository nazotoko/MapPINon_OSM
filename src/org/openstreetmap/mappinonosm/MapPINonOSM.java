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

package org.openstreetmap.mappinonosm;

import java.io.FileInputStream;
import org.openstreetmap.mappinonosm.database.PhotoTable;
import org.openstreetmap.mappinonosm.database.Photo;
import org.openstreetmap.mappinonosm.database.Tile;
import org.openstreetmap.mappinonosm.database.XMLTable;
import org.openstreetmap.mappinonosm.database.TileTable;
import org.openstreetmap.mappinonosm.database.RSS;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author nazo
 */
public class MapPINonOSM {
    private PhotoTable pb;
    private XMLTable rb;

    private File rssList=new File("htdocs/rsslist.html");
    /** initiallizing */
    public MapPINonOSM() {
        InputStream is;
        pb = new PhotoTable();
        rb = new XMLTable(pb);
        try {
            rb.load(is=new GZIPInputStream(new FileInputStream("rss.json.gz")));
            is.close();
        } catch(FileNotFoundException ex) {
            System.out.println("RSS database file cannot be read.");
        } catch(IOException ex){
            System.out.println("RSS database file cannot be closed.");
        }
    }
    /** photoload */
    public void photoload() {
        InputStream is;
        try {
            pb.load(is = new GZIPInputStream(new FileInputStream("photo.json.gz")), rb);
            is.close();
        } catch(FileNotFoundException ex) {
            System.out.println("Cannot read the Photo base file.");
        } catch(IOException ex) {
            System.out.println("Cannot close the Photo base file.");
        }
    }

    /**
     * 
     * @param urls
     */
    public void addRSS(String... urls){
        /** First stage: adding new RSS */
        RSS r;
        for(String u: urls){
            try {
                r = new RSS(new URL(u));
                rb.add(r);
            } catch(MalformedURLException ex) {
                System.out.println("Ilreguler URL: " + u);
            }
        }
    }
    /** second: reading RSS */
    public void read() {
        rb.read();
    }

    /** */
    public void makeTiles(){
        /** third stage: making tiles from photo database*/
        TileTable tb =new TileTable();
        Tile t;
        for(Photo p: pb){
            int id = TileTable.getID(p.getLon(), p.getLat());
            if((t = tb.get(id)) == null){
                t = new Tile();
                tb.put(id, t);
            }
            t.add(p);
        }
        tb.save();
        try {
            rb.toHTML(new FileOutputStream(rssList));
        } catch(FileNotFoundException ex) {
            System.out.println("Error! Cannot make RSS list HTML.");
        }
    }
    /** save the parameters */
    public void save(){
        /** final stage: put out tiles **/
        OutputStream os;
        try {
            os = new GZIPOutputStream(new FileOutputStream("rss.json.gz"));
            rb.save(os);
            os.close();
        } catch(FileNotFoundException ex) {
            System.out.println("Cannot open rss.json.gz");
        } catch(IOException ex) {
            System.out.println("Cannot close rss.json.gz");
        }
        try {
            os=new GZIPOutputStream(new FileOutputStream("photo.json.gz"));
            pb.save(os);
            os.close();
        } catch(FileNotFoundException ex) {
            System.out.println("Cannot open photo.json.gz");
        } catch(IOException ex) {
            System.out.println("Cannot close photo.json.gz");
        }
        try {
            os=new GZIPOutputStream(new FileOutputStream("backup/photo-"+new SimpleDateFormat("MMddHHmmss").format(new Date())+".json.gz"));
            pb.save(os);
            os.close();
        } catch(FileNotFoundException ex) {
            System.out.println("Cannot open photo.json.gz");
        } catch(IOException ex) {
            System.out.println("Cannot close photo.json.gz");
        }
    }
    /**
     * show usage and exit
     */
    static public void usage() {
        System.err.println("Background server and database for \"MapPIN'on OSM\" (http://mappin.hp2.jp/).");
        System.err.println("Copyrighted by Nazotoko, 2009, licensed by a BSD-style license.");
        System.err.println("Usage:");
        System.err.println("$ java -cp lib/metafile.jar:MapPIN.jar mappin.Main (command) (args1) (args2) ..");
        System.err.println("");
        System.err.println("commands:");
        System.err.println("\tadd: add RSSes. args are URLs to the RSSes.");
        System.err.println("\tread: read the RSSes from the URLs and update databases");
        System.err.println("\ttile: create tiles and rss lists.");
//        System.err.println("\tupload: upload tiles and rss list. arg1:hostname arg2:username arg3:password");
        System.err.println("\tloop: read and tile.");
        System.exit(1);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length==0){
            usage();
        }
        System.err.println("=========== Loading data tables ===========");
        MapPINonOSM database = new MapPINonOSM();
        database.photoload();

        if(args[0].equals("add")){
            for(int i = 1; i < args.length; i++){
                database.addRSS(args[i]);
            }
            database.save();
            System.exit(0);
        }
        if(args[0].endsWith("read")||args[0].equals("loop")){
            System.err.println("=========== Starting to downloading RSSes and photo files ===========");
            database.read();
        }
        if(args[0].endsWith("tile")||args[0].equals("loop")){
            System.err.println("=========== Starting to make tiles ===========");
            database.makeTiles();
        }
        if(args[0].endsWith("upload")||args[0].equals("loop")){
        }
        System.err.println("=========== Saving data tables ===========");
        database.save();
    }
}
