/* Copyright (c) 2013, Graeme Ball.                          
 *                                                                               
 * This program is free software: you can redistribute it and/or modify         
 * it under the terms of the GNU General Public License as published by         
 * the Free Software Foundation, either version 3 of the License, or            
 * (at your option) any later version.                                          
 *                                                                               
 * This program is distributed in the hope that it will be useful,              
 * but WITHOUT ANY WARRANTY; without even the implied warranty of               
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                
 * GNU General Public License for more details.                                 
 *                                                                               
 * You should have received a copy of the GNU General Public License            
 * along with this program. If not, see http://www.gnu.org/licenses/ .         
 */

package SIMcheck;

import java.util.*;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import java.math.BigDecimal;
import java.math.MathContext;

/** ImageJ1 plugin result container using HashMaps: items must have 
 * unique names within result types.
 * @author Graeme Ball <graemeball@gmail.com>
 */
public class ResultSet {

    private String resultSetName = "";
    private LinkedHashMap<String, ImagePlus> imps = 
            new LinkedHashMap<String, ImagePlus>();
    private LinkedHashMap<String, Double> stats =
            new LinkedHashMap<String, Double>();
    private LinkedHashMap<String, String> infos = 
            new  LinkedHashMap<String, String>();
    
    ResultSet(String name) {
        resultSetName = name;
    }

    /** Add ImagePlus result & description: title+description MUST be unique. */
    public void addImp(String description, ImagePlus imp) {
        description = imp.getTitle() + ": " + description;  // more unique
        if (imps.containsKey(description)) {
            throw new IllegalArgumentException(description + " already exists");
        }
        imps.put(description, imp);
    }
    
    /** Return Imp number nImp (insert order, 0+) in imps hashmap, else null */
    public ImagePlus getImp(int nImp) {
        Iterator<ImagePlus> it = imps.values().iterator();
        int n = 0;
        while (it.hasNext()) {
            if (n == nImp) {
                return it.next();
            }
        }
        return null;  // did not reach desired nImp
    }
    
    /** Add a named Double statistic result: statName MUST be unique. */
    public void addStat(String statName, Double value) {
        if (stats.containsKey(statName)) {
            throw new IllegalArgumentException(statName + " already exists");
        }
        stats.put(statName, value);
    }
    
    /** Add an information string with title: title MUST be unique. */
    public void addInfo(String title, String info) {
        if (infos.containsKey(title)) {
            throw new IllegalArgumentException(title + " already exists");
        }
        infos.put(title, info);
    }
    
    /** Report all results */
    public void report() {
        IJ.log("");
        IJ.log(resultSetName);
        IJ.log(new String(new char[resultSetName.length()]).replace("\0", "-"));
        for (Map.Entry<String, ImagePlus> entry : imps.entrySet()) {
            String description = entry.getKey();
            ImagePlus imp = (ImagePlus)entry.getValue();
            IJ.log("  Displaying " + description);
            imp.show();
        }
        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            String statName = entry.getKey();
            Double stat = entry.getValue();
            BigDecimal bd = new BigDecimal(stat);  
            bd = bd.round(new MathContext(2));  // OOMG
            double stat2sigFig = bd.doubleValue();  
            IJ.log("  " + statName + " = " + stat2sigFig);
        }
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String infoTitle = entry.getKey();
            String info = entry.getValue();
            IJ.log("  " + infoTitle + ": " + info);
        }
        IJ.log("---");
    }
    
    /** Return an Object[] representation of the results. */
    public Object[] objects() {
        List<Object> objList = new ArrayList<Object>();
        objList.add(resultSetName);
        objList.addAll(Arrays.asList(imps.keySet().toArray()));
        objList.addAll(Arrays.asList(imps.values().toArray()));
        objList.addAll(Arrays.asList(stats.keySet().toArray()));
        objList.addAll(Arrays.asList(stats.values().toArray()));
        objList.addAll(Arrays.asList(infos.keySet().toArray()));
        objList.addAll(Arrays.asList(infos.values().toArray()));
        Object[] objArray = objList.toArray();
        return objArray;
    }
    
    /** test method */
    public static void main(String[] args) {
        
        ResultSet results = new ResultSet("ResultSet Test");
        new ImageJ();
        ImagePlus limp = IJ.openImage(
                "/Users/graemeb/Documents/testData/Lena.tif");
        results.addImp("a picture of Lena", limp);
        results.addStat("stat1, imWidth", (double)limp.getWidth());
        results.addStat("stat2, imHeight", (double)limp.getHeight());
        results.addStat("stat3, imBytesPerPix", 
                (double)limp.getBytesPerPixel());
        results.addInfo("about", "this is a picture of Lena");
        
            

        IJ.log("report()  - should show all images and log"
                + " all stats, info. Check stats appear in order.");
        results.report();
        
        IJ.log("checking identity of " + results.objects().length 
                + " (expected " + 11 + ") returned objects...");
        Object[] objs = results.objects();
        IJ.log("resultSetName, objs[0] instanceof String? " 
                + new Boolean(objs[0] instanceof String).toString());
        IJ.log("imps key 1, objs[1] instanceof String? " 
                + new Boolean(objs[1] instanceof String).toString());
        IJ.log("imps value 1, objs[2] instanceof ImagePlus? " 
                + new Boolean(objs[2] instanceof ImagePlus).toString());
        IJ.log("stats key 1, objs[3] instanceof String? " 
                + new Boolean(objs[3] instanceof String).toString());
        IJ.log("stats key 2, objs[4] instanceof String? " 
                + new Boolean(objs[4] instanceof String).toString());
        IJ.log("stats key 3, objs[5] instanceof String? " 
                + new Boolean(objs[5] instanceof String).toString());
        IJ.log("stats value 1, objs[6] instanceof Double? " 
                + new Boolean((Double)objs[6] instanceof Double).toString());
        IJ.log("stats value 2, objs[7] instanceof Double? " 
                + new Boolean((Double)objs[6] instanceof Double).toString());
        IJ.log("stats value 3, objs[8] instanceof Double? " 
                + new Boolean((Double)objs[6] instanceof Double).toString());
        IJ.log("infos key 1, objs[9] instanceof String? " 
                + new Boolean(objs[9] instanceof String).toString());
        IJ.log("infos value 1, objs[10] instanceof String? " 
                + new Boolean(objs[10] instanceof String).toString());

        IJ.log("addStat() duplicate throws IllegalArgument exception?");
        try {
            results.addStat("stat1, imWidth", 7.0d);
            IJ.log("NO EXCEPTION");
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                IJ.log("YES");
            } else {
                IJ.log("WRONG EXCEPTION");
            }
        }
    }
}