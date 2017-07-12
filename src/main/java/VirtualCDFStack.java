import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.LookUpTable;
import ij.gui.ScrollbarWithLabel;
import ij.gui.StackWindow;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Vector;

import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.Variable;

/**
 * This class represents an array of disk-resident images.
 */
class VirtualCDFStack extends ImageStack implements AdjustmentListener
{
    private int nSlices;
    private Vector<MetaData> meta;
    private int xyIndex;
    private ScrollbarWithLabel xySelector;
    private boolean useSelector;
    private ImagePlus imp;
    private MetaDataViewer mv;

    /**
     * Creates a new, empty virtual stack.
     * 
     * @throws CDFException
     */
    public VirtualCDFStack(int width, int height, Vector<MetaData> _meta, int _xyIndex) throws CDFException
    {
        super(width, height);
        useSelector = false;
        xySelector = null;
        xyIndex = _xyIndex;
        meta = _meta;
        mv = null;
        nSlices = (int) meta.firstElement().getTimeSlots() * meta.firstElement().getZCount() * meta.size();

        IJ.showStatus("VirtualCDFStack " + nSlices);
    }

    public VirtualCDFStack(int width, int height, Vector<MetaData> _meta) throws CDFException
    {
        super(width, height);
        useSelector = true;
        xySelector = null;
        xyIndex = 0;
        meta = _meta;
        mv = null;
        nSlices = (int) meta.firstElement().getTimeSlots() * meta.firstElement().getZCount() * meta.size();

        IJ.showStatus("VirtualCDFStack " + nSlices);
    }
    
    public Vector<MetaData> getMetaData()
    {
        return meta;
    }
    
    /** Does nothing. */
    public void addSlice(String name)
    {
    }

    /** Does nothing. */
    public void addSlice(String sliceLabel, Object pixels)
    {
    }

    /** Does nothing.. */
    public void addSlice(String sliceLabel, ImageProcessor ip)
    {
    }

    /** Does noting. */
    public void addSlice(String sliceLabel, ImageProcessor ip, int n)
    {
    }

    /** Deletes the specified slice, were 1<=n<=nslices. */
    public void deleteSlice(int n)
    {
        // if (n < 1 || n > nSlices)
        // throw new IllegalArgumentException("Argument out of range: " + n);
        //
        // if (nSlices < 1)
        // return;
        //
        // for (int i = n; i < nSlices; i++)
        // used[i - 1] = used[i];
        // nSlices--;
    }

    /** Deletes the last slice in the stack. */
    public void deleteLastSlice()
    {
        if (nSlices > 0)
            deleteSlice(nSlices);
    }

    /** Returns the pixel array for the specified slice, were 1<=n<=nslices. */
    public Object getPixels(int n)
    {
        ImageProcessor ip = getProcessor(n);
        if (ip != null)
            return ip.getPixels();
        else
            return null;
    }

    /**
     * Assigns a pixel array to the specified slice, were 1<=n<=nslices.
     */
    public void setPixels(Object pixels, int n)
    {
    }

    /**
     * Returns an ImageProcessor for the specified slice, were 1<=n<=nslices.
     * Returns null if the stack is empty.
     */
    public ImageProcessor getProcessor(int n)
    {
        n--;
        if (n < 0 || n >= nSlices)
            return null;

        int[] czt = getCZT(n);
        MetaData m = meta.elementAt(czt[0]);
        Variable var = m.getVar();
        long dataType = var.getDataType();

        Object data;
        try
        {
            data = var.getRecordObject(m.getRecordIndex(czt[2], xyIndex, czt[1])).getRawData();
        }
        catch (CDFException e)
        {
            // TODO Auto-generated catch block
            System.err.println(e.toString());
            e.printStackTrace();
            return null;
        }

        if (dataType == CDFConstants.CDF_DOUBLE)
        {
            return new FloatProcessor(getWidth(), getHeight(), (double[]) data);
        }

        if (dataType == CDFConstants.CDF_FLOAT)
        {
            return new FloatProcessor(getWidth(), getHeight(), (float[]) data);
        }

        if (dataType == CDFConstants.CDF_UINT2)
        {
            int pixels = getWidth() * getHeight();
            short[] b = new short[pixels];

            for (int i = 0; i < pixels; ++i)
            {
                b[i] = (short) ((int[]) data)[i];
            }
            return new ShortProcessor(getWidth(), getHeight(), b, LookUpTable.createGrayscaleColorModel(false));
        }

        if (dataType == CDFConstants.CDF_UINT1)
        {
            int pixels = getWidth() * getHeight();
            byte[] b = new byte[pixels];

            for (int i = 0; i < pixels; ++i)
            {
                b[i] = (byte) (255 * ((short[]) data)[i]);
            }

            return new ByteProcessor(getWidth(), getHeight(), (byte[]) b);
        }

        return null;
    }

    /** Returns the number of slices in this stack. */
    public int getSize()
    {
        return nSlices;
    }

    /** Returns the file name of the Nth image. */
    public String getSliceLabel(int n)
    {
        n--;

        String result = "";

        int[] czt = getCZT(n);
        MetaData m = meta.elementAt(czt[0]);
        int r = m.getRecordIndex(czt[2], xyIndex, czt[1]);
        result = m.getVar().getName();
        
        if(mv != null)
            mv.update(m, czt[2], r);
        
//        try
//        {
//            PrintWriter writer = new PrintWriter("d:/meta.txt", "UTF-8");
//            writer.println(m.getMetaString(r));
//            writer.close();
//        }
//        catch (IOException e)
//        {
//            // do something
//        }

        //System.out.println(m.getMetaString(r));
        return result;
    }

    /** Returns null. */
    public Object[] getImageArray()
    {
        return null;
    }

    /** Does nothing. */
    public void setSliceLabel(String label, int n)
    {
    }

    /** Always return true. */
    public boolean isVirtual()
    {
        return true;
    }

    /** Does nothing. */
    public void trim()
    {
    }

    private int[] getCZT(int n)
    {
        int[] czt = new int[3];
        MetaData m = meta.firstElement();

        int segment = m.getZCount() * meta.size();
        czt[2] = n / segment; // T

        int subSegment = n % segment;
        czt[1] = subSegment / meta.size(); // Z

        czt[0] = subSegment % meta.size(); // C

        return czt;
    }
    
    
    public void setUpXYScrollBar(ImagePlus im)
    {
        if(!useSelector)
            return;
        
        imp = im;
        
        StackWindow w = (StackWindow)im.getWindow();
        
        int c = meta.firstElement().getXYCount();

        if (c > 1)
        {
            xySelector = new ScrollbarWithLabel(w, 1, 1, 1, c + 1, 'P');
            w.add(xySelector);
            w.setSliderHeight(w.getSliderHeight() + xySelector.getPreferredSize().height + StackWindow.VGAP);
            w.pack();

            ImageJ ij = IJ.getInstance();
            if (ij != null)
                xySelector.addKeyListener(ij);
            xySelector.addAdjustmentListener(this);
            xySelector.setFocusable(false);
            w.revalidate();
            w.repaint();
        }
    }

    public synchronized void adjustmentValueChanged(AdjustmentEvent e) 
    {
        if (xySelector != null)
        {
            if (e.getSource() == xySelector)
            {
                int c = xySelector.getValue() - 1;
                if (c == xyIndex && e.getAdjustmentType() == AdjustmentEvent.TRACK)
                    return;

                xyIndex = c;
                int s = imp.getCurrentSlice();
                if(s == 1)
                {
                    imp.setSlice(s + 1);
                    imp.setSlice(s);
                }
                else
                {
                    imp.setSlice(s - 1);
                    imp.setSlice(s);
                }
            }
        }
    }
    
    
    public void setUpMetaDataPanel(ImagePlus im)
    {
        StackWindow w = (StackWindow)im.getWindow();
        
        mv = new MetaDataViewer();
        mv.setFocusable(false);
        w.add(mv);
        w.setSliderHeight(w.getSliderHeight() + mv.getPreferredSize().height + StackWindow.VGAP);
        w.pack();
        w.revalidate();
        w.repaint();        
    }
    
    
            
}