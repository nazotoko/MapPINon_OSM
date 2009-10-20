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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;

/**
 *
 * @author nazo
 */
public class TileTable extends HashMap<Integer,Tile>{
    private File baseDirectory;

    static public int getID(double lon,double lat){
        return (int)Math.round(lon*20)+7200*(int)Math.round(lat*20);
    }

    /**
     * Constractor of Tile Table
     * @param dataDir Directory to be stored backup of tables.
     * @throws java.io.IOException When the dataDir is not directory.
     */
    public TileTable(File dataDir) throws IOException{
        if(!dataDir.exists()){
            if(dataDir.mkdir()==false){
                throw new IOException("dataDir directory cannot be make.");
            }
        } else if(!dataDir.isDirectory()){
                throw new IOException("dataDir must be a dicectory.");            
        }
        baseDirectory=dataDir;
    }

    public void save() {
        File tileF=null;
        FileOutputStream os;
        for(Integer i: keySet()){
            int lat=i/7200;
            int lon=i%7200;
            if(lon < -3200){
                lat--;
                lon += 7200;
            }
            if(3200 < lon){
                lat++;
                lon -= 7200;
            }
            try{
                tileF=new File(baseDirectory,"photo" + ((lon > 0) ? "+" : "") + lon + ((lat > 0) ? "+" : "") + lat+".js");
                os = new FileOutputStream(tileF);
                get(i).toJavaScript(os);
                os.close();
            } catch(IOException ex) {
                System.err.println("A tile cannot be write. The file name is "+tileF);
            }
        }
    }
}
