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
import uk.ac.bristol.star.cdf.DataType;
import uk.ac.bristol.star.cdf.Variable;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.util.Vector;


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
    private DataType baseDataType;

    /**
     * Creates a new, empty virtual stack.
     * 
     * @throws CDFException
     */
    public VirtualCDFStack(int width, int height, Vector<MetaData> _meta, int _xyIndex)
    {
        super(width, height);
        useSelector = false;
        xySelector = null;
        xyIndex = _xyIndex;
        meta = _meta;
        mv = null;
        nSlices = (int) meta.firstElement().getTimeSlots() * meta.firstElement().getZCount() * meta.size();

        IJ.showStatus("VirtualCDFStack " + nSlices);
        
        baseDataType = getBaseDataType();        
    }

    public VirtualCDFStack(int width, int height, Vector<MetaData> _meta)
    {
        super(width, height);
        useSelector = true;
        xySelector = null;
        xyIndex = 0;
        meta = _meta;
        mv = null;
        nSlices = (int) meta.firstElement().getTimeSlots() * meta.firstElement().getZCount() * meta.size();

        IJ.showStatus("VirtualCDFStack " + nSlices);
        
        baseDataType = getBaseDataType();
    }
        
    public DataType getBaseDataType()
    {
        DataType result = DataType.UINT1;
        
        for(MetaData m : meta)
        {
        	DataType v = m.getVar().getDataType();            
            if(VariableExt.DataTypeIndex(v) > VariableExt.DataTypeIndex(result))
                result = v;            
        }
        return result;
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
        Variable var = m.getVar().Var();
        DataType dataType = var.getDataType();

        //System.out.println("Stack: " + n + "  " +  m.getRecordIndex(czt[2], xyIndex, czt[1]));
        
        Object data = var.createRawValueArray();
        //System.out.println("Stack Data type: " + dataType.getName() + "  " + data.getClass().getComponentType());
        try {
			var.readRawRecord(m.getRecordIndex(czt[2], xyIndex, czt[1]), data);
            //System.out.println("Stack Data: " + var.getName() + " type: " + dataType.getName() + "  " + data.getClass().getComponentType() + " var: " + var.getRecordVariance() );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

        if (dataType == DataType.DOUBLE)
        {
            return new FloatProcessor(getWidth(), getHeight(), (double[]) data);
        }

        if (dataType == DataType.FLOAT)
        {
            return new FloatProcessor(getWidth(), getHeight(), (float[]) data);
        }

        if (dataType == DataType.UINT2)
        {
            if(VariableExt.DataTypeIndex(baseDataType) > VariableExt.DataTypeIndex(DataType.UINT2)) // should be just CDF_FLOAT or CDF_DOUBLE both converted to FloatProcessor
                return new FloatProcessor(getWidth(), getHeight(), (int[]) data);
            else
            {
                short[] b = new short[getWidth() * getHeight()];

                for (int i = 0; i < b.length; ++i)
                {
                    b[i] = (short) ((int[]) data)[i];
                }
                return new ShortProcessor(getWidth(), getHeight(), b, LookUpTable.createGrayscaleColorModel(false));
            }
        }

        if (dataType == DataType.UINT1)
        {
            if (VariableExt.DataTypeIndex(baseDataType) > VariableExt.DataTypeIndex(DataType.UINT2))
            { // convert to FloatProcessor
                float[] b = new float[getWidth() * getHeight()];
                for (int i = 0; i < b.length; ++i)
                {
                    b[i] = (float) ((short[]) data)[i];
                }                
                return new FloatProcessor(getWidth(), getHeight(), b);
            }
            else
                if (baseDataType == DataType.UINT2)
                { // convert to ShortProcessor
                    return new ShortProcessor(getWidth(), getHeight(), (short[]) data, LookUpTable.createGrayscaleColorModel(false));
                }
                else
                { // ByteProcessor
                    byte[] b = new byte[getWidth() * getHeight()];

                    for (int i = 0; i < b.length; ++i)
                    {
                        b[i] = (byte) ((short[]) data)[i];
                    }

                    return new ByteProcessor(getWidth(), getHeight(), (byte[]) b);
                }
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