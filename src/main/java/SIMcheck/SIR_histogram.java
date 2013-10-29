/*                                                                              
 *  Copyright (c) 2013, Graeme Ball and Micron Oxford,                          
 *  University of Oxford, Department of Biochemistry.                           
 *                                                                               
 *  This program is free software: you can redistribute it and/or modify         
 *  it under the terms of the GNU General Public License as published by         
 *  the Free Software Foundation, either version 3 of the License, or            
 *  (at your option) any later version.                                          
 *                                                                               
 *  This program is distributed in the hope that it will be useful,              
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of               
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                
 *  GNU General Public License for more details.                                 
 *                                                                               
 *  You should have received a copy of the GNU General Public License            
 *  along with this program.  If not, see http://www.gnu.org/licenses/ .         
 */ 

package SIMcheck;
import ij.*;
import ij.plugin.PlugIn;
import ij.gui.HistogramWindow;
import ij.process.*;

/** This plugin takes reconstructed data and produces produces 
 * linear+logarithmic histogram showing relative contribution of 
 * negative values to the reconstructed result.
 * @author Graeme Ball <graemeball@gmail.com>
 */
public class SIR_histogram implements PlugIn, EProcessor {
    
    String name = "Reconstructed Data Histograms";
    ResultSet results = new ResultSet(name);
    double percentile = 0.5;  // 0-100
    double min_ratio = 6.0;
    double mode_tol = 0.25;

    @Override
    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        results = exec(imp);
        results.report();
    }

    /** Execute plugin functionality: plot histogram and calculate +ve/-ve
     * ratio. 
     * @param imps reconstructed SIR data ImagePlus should be first imp
     * @return ResultSet containing histogram plots                                  
     */
    public ResultSet exec(ImagePlus... imps) { 
        IJ.showStatus("Reconstructed data histograms...");
        int nc = imps[0].getNChannels();
        ImagePlus[] plots = new ImagePlus[nc];
        for (int c = 1; c <= nc; c++){
            ImagePlus imp2 = I1l.copyChannel(imps[0], c);
            StackStatistics stats = new StackStatistics(imp2);
            if (Math.abs(stats.dmode) > (stats.stdDev*mode_tol)) {
                IJ.log("  ! warning, ch" + c + " histogram mode=" 
                        + Math.round(stats.dmode) 
                        + " not within " + mode_tol + " stdev of 0\n");
            }
            if (stats.histMin < stats.dmode) {
                // caluclate +ve / -ve ratio if histogram has negatives
                double posNegRatio = calcPosNegRatio(
                		stats, (double)percentile / 100);
                String statDescription = "Ratio of extreme " 
                        + Double.toString(percentile) + "% positive/negative"
                        + " intensities for channel " + c;
                results.addStat(statDescription, 
                        (double)((int)(posNegRatio * 10)) / 10);
                
            } else {
                results.addInfo("  ! histogram minimum above mode for channel "
                        + Integer.toString(c), 
                        "unable to calculate +ve/-ve intensity ratio");
            }
            String newTitle = "Reconstructed Data Histogram Channel " + c;
            EhistWindow histW = new EhistWindow(newTitle, imp2, stats);
            histW.setVisible(false);
            plots[c - 1] = histW.getImagePlus();
        }
        String title = "log-scaled intensity counts in gray";
        ImagePlus impAllPlots = I1l.mergeChannels(title, plots);
        impAllPlots.setDimensions(nc, 1, 1);
        impAllPlots.setOpenAsHyperStack(true);
        results.addImp(title, impAllPlots);
        results.addInfo("Intensity ratio above / below mode", 
                "<3 inadequate, 3-6 low, 6-12 good, >12 excellent");
        return results;
    }

    /** Calulate the ratio of extreme positive versus negative values in the 
     * image histogram. 
     * @param stats for the ImagePlus
     * @param pc (0-1) fraction of histogram to use at lower AND upper ends
     * @return (Imax - Imode) / (Imode - Imin), i.e. positive / negative ratio 
     */
    double calcPosNegRatio(ImageStatistics stats, double pc) {
        int[] hist = stats.histogram;
        // find hist step (bin size), and number of pixels in image 
        double histStep = (stats.histMax - stats.histMin)
                / (double)hist.length;
        long nPixels = 0;
        for (int i = 0; i < hist.length; i++) {
            nPixels += hist[i];
        }
        // for negative histogram extreme, add until percentile reached
        double negPc = 0;
        double negTotal = 0;
        int bin = 0;
        double binValue = stats.histMin;
        while (negPc < pc && binValue < stats.dmode && bin < hist.length) {
            negPc += (double)hist[bin] / nPixels;
            negTotal += (binValue * hist[bin]) - stats.dmode;
            bin += 1;
            binValue += histStep;
        }
        pc = negPc;  // consider same-sized positive & negative extrema
        // for positive histogram extreme, add until percentile reached
        double posPc = 0;
        double posTotal = 0;
        bin = hist.length - 1;
        binValue = stats.histMax;
        while ((posPc < pc) && (bin >= 0)) {
            posPc += (double)hist[bin] / nPixels;
            // adjust so that histogram mode is zero intensity
            posTotal += (binValue * hist[bin]) - stats.dmode;  
            bin -= 1;
            binValue -= histStep;
        }
        // negTotal may or may not be negative
        double posNegRatio = Math.abs(posTotal / negTotal);  
        return posNegRatio;
    }

    /** Extend ImageJ HistogramWindow for auto log scaling and stack stats. */
    class EhistWindow extends HistogramWindow {
        private static final long serialVersionUID = 1L;
        /* customize the HistogramWindow during construction */
        public EhistWindow(String title, ImagePlus imp, ImageStatistics stats){
            super(title, imp, stats);
            super.logScale = true;
            this.showHistogram(imp, stats);
        }

    }
    
}