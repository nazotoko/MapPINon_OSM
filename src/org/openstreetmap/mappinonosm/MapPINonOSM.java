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

import com.aetrion.flickr.photos.PhotosInterface;
import java.io.BufferedReader;
import java.io.FileInputStream;
import org.openstreetmap.mappinonosm.database.PhotoTable;
import org.openstreetmap.mappinonosm.database.Photo;
import org.openstreetmap.mappinonosm.database.Tile;
import org.openstreetmap.mappinonosm.database.XMLTable;
import org.openstreetmap.mappinonosm.database.TileTable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.openstreetmap.mappinonosm.database.XML;

/**
 *
 * @author nazo
 */
public class MapPINonOSM {
    private PhotoTable photoTable;
    private XMLTable xmlTable;
    private PhotosInterface pi;

    /** local file */
    private File rss_table=null;
    /** local file */
    private File photo_table=null;
    /** local file */
    private File history_table=null;
    /** local file */
    private File rssList=null;

    private String domain=null;
    private String registration=null;

    final static private String [] configKeys=new String []{
        "photoTable",
        "RSSTable", 
        "historyTable",
        "RSSList",
        "domain",
        "registration",
        "flickrKey",
        "flickrSecret",
        "backupDir",
        "dataDir"
    };
    private String flickrKey;
    private String flickrSecret;
    private File backupdir=null;
    private File dataDir=null;

    /** initiallizing */
    public MapPINonOSM() {
        InputStream is;
        String line;
        String value;
        int i;
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("./config.txt"),"UTF-8"));
            while((line=br.readLine())!=null){
                for(i = 0; i < configKeys.length; i++){
                    if(line.startsWith(configKeys[i])){
                        value = line.substring(configKeys[i].length() + 1);
                        switch(i){
                            case 0:
                                photo_table = new File(value);
                                break;
                            case 1:
                                rss_table = new File(value);
                                break;
                            case 2:
                                history_table=new File(value);
                                break;
                            case 3:
                                rssList = new File(value);
                                break;
                            case 4:
                                domain = value;
                                break;
                            case 5:
                                registration = value;
                                break;
                            case 6:
                                flickrKey =value;
                                break;
                            case 7:
                                flickrSecret =value;
                                break;
                            case 8:
                                backupdir=new File(value);
                                if(!backupdir.exists()){
                                    if(!backupdir.mkdir()){
                                        System.out.println("Warning: It cannot make backup directory.");
                                        backupdir=null;
                                    }
                                } else {
                                    if(!backupdir.isDirectory()){
                                        System.out.println("Warning: The '"+backupdir+"' is not directory.");
                                        backupdir=null;
                                    }
                                }
                                break;
                            case 9:
                                dataDir=new File(value);
                                break;
                        }
                    }
                }
            }
            br.close();
        } catch(FileNotFoundException ex) {
            System.out.println("config file cannot be read.");
        } catch(IOException ex){
            System.out.println("config file cannot be closed.");
        }
        
        photoTable = new PhotoTable();
        xmlTable = new XMLTable(photoTable);
        if(rss_table==null){
            System.err.println("RSS table file is not specified.");
            return;
        }
        try {
            xmlTable.load(is=new GZIPInputStream(new FileInputStream(rss_table)));
            is.close();
        } catch(FileNotFoundException ex) {
            System.out.println("RSS table file '"+rss_table.getPath()+"' cannot be read.");
        } catch(IOException ex){
            System.out.println("RSS table file '"+rss_table.getPath()+"' cannot be closed.");
        }
    }
    /** photoload */
    public void photoload() {
        InputStream is;
        if(photo_table==null){
            System.err.println("Photo table file is not specified.");
            return;
        }
        try {
            photoTable.load(is = new GZIPInputStream(new FileInputStream(photo_table)), xmlTable);
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
        XML x;
        for(String u: urls){
            try {
                x = XML.getInstance(new URI(u));
                xmlTable.add(x);
                System.out.println("add: "+u);
            } catch(URISyntaxException ex) {
                System.out.println("Ilreguler URI: " + u);
            }
        }
    }

    /**
     * getRSS from foreground servers registration text file.
     */
    public void getRSS() {
        if(registration == null || domain == null){
            System.err.println("== get command: not set to use ==");
            return;
        }
        BufferedReader br = null;
        URL url = null;
        String s;
        try {
            url = new URL("http", domain, "/"+registration);
            br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            while((s = br.readLine()) != null){
                xmlTable.add(XML.getInstance(new URI(s)));
                System.out.println("got: "+s);
            }
            br.close();
        } catch(MalformedURLException ex){
            System.err.println("Strange URL: "+url); 
        } catch(URISyntaxException ex) {
            System.err.println("URL is strange. Check the config.txt file on the line domain.");
        } catch(IOException ex) {
            System.err.println("The file cannot to be accessed. URL: "+url);
        }
    }
    
    /** second: reading RSS */
    public void read() {
        xmlTable.read();
    }

    /** making tiles and statistics */
    public void makeTiles(){
        /** third stage: making tiles from photo database*/
        TileTable tb;
        Tile t;
        try {
            tb = new TileTable(dataDir);
            for(Photo p: photoTable){
                int id = TileTable.getID(p.getLon(), p.getLat());
                if((t = tb.get(id)) == null){
                    t = new Tile();
                    tb.put(id, t);
                }
                t.add(p);
            }
            tb.save();
        } catch(IOException ex) {
            System.out.println("Fail to open tile directory: "+ex.getMessage());
        }

        if(rssList!=null){
            try {
                xmlTable.toHTML(new FileOutputStream(rssList));
            } catch(FileNotFoundException ex) {
                System.out.println("Error! Cannot make RSS list HTML.");
            }
        }
    }
    /** save the tables
     *
     */
    public void save(){
        /** final stage: put out tiles **/
        OutputStream os;
        if(rss_table!=null){
            try {
                os = new GZIPOutputStream(new FileOutputStream(rss_table));
                xmlTable.save(os);
                os.close();
            } catch(FileNotFoundException ex) {
                System.out.println("Cannot open rss.json.gz");
            } catch(IOException ex) {
                System.out.println("Cannot close rss.json.gz");
            }
        } else {
            System.err.println("Warning: Photo table is not specified, so RSS table has not been saved.");            
        }

        if(photo_table!=null){
            try {
                os = new GZIPOutputStream(new FileOutputStream(photo_table));
                photoTable.save(os);
                os.close();
            } catch(FileNotFoundException ex) {
                System.out.println("Cannot open photo.json.gz");
            } catch(IOException ex) {
                System.out.println("Cannot close photo.json.gz");
            }
        } else {
            System.err.println("Warning: Photo table is not specified, so photo table has not saved.");
        }

        if(backupdir!=null){
            try {
                os = new GZIPOutputStream(new FileOutputStream("backup/photo-" + new SimpleDateFormat("MMddHHmmss").format(new Date()) + ".json.gz"));
                photoTable.save(os);
                os.close();
            } catch(FileNotFoundException ex) {
                System.out.println("Cannot open photo.json.gz");
            } catch(IOException ex) {
                System.out.println("Cannot close photo.json.gz");
            }
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
        System.err.println("\tadd: add RSSes from args on command line. The args are URLs of RSSes.");
        System.err.println("\tget: add RSSes from the registration text file on foreground server.");
        System.err.println("\tread: read the RSSes from the URLs and update databases");
        System.err.println("\ttile: create tiles and rss lists.");
//        System.err.println("\tupload: upload tiles and rss list. arg1:hostname arg2:username arg3:password");
        System.err.println("\tloop: get, read and tile.");
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
        if(args[0].equals("get") || args[0].equals("loop")){
            System.err.println("=========== Registering RSSes ===========");
            database.getRSS();
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
