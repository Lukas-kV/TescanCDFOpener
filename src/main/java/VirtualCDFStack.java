import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.util.Vector;

import MetaStruct.FluoClipping;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.LookUpTable;
import ij.gui.ImageWindow;
import ij.gui.ScrollbarWithLabel;
import ij.gui.StackWindow;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import uk.ac.bristol.star.cdf.DataType;
import uk.ac.bristol.star.cdf.Variable;


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
    private boolean specialMode; //! when only xy positions are opened

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
        
        specialMode = false;
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
        
        specialMode = (nSlices == 1 &&  meta.firstElement().getXYCount() > 1);
        
        if(specialMode)
        {
        	useSelector = false;
        	nSlices = meta.firstElement().getXYCount();
        }
        
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
        
        //System.out.println("Clipping: " + m.getVar().isClippingPossible());
        //System.out.println("Stack: " + n + "  " +  m.getRecordIndex(czt[2], xyIndex, czt[1]));        
        //System.out.println("Stack: " + czt[0] + "  " + czt[1] + "  " + czt[2] ); 
        
        Object data = var.createRawValueArray();
        //System.out.println("Stack Data type: " + dataType.getName() + "  " + data.getClass().getComponentType());
        try {
            var.readRawRecord(m.getRecordIndex(czt[2], czt[3], czt[1]), data);
            //System.out.println("Stack Data: " + var.getName() + " type: " + dataType.getName() + "  " + data.getClass().getComponentType() + " var: " + var.getRecordVariance() );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        
        if (dataType == DataType.DOUBLE)
        {
            return ClippImage(new FloatProcessor(m.getWidth(), m.getHeight(), (double[]) data), m, czt[2]);
        }

        if (dataType == DataType.FLOAT)
        {
            return ClippImage(new FloatProcessor(m.getWidth(), m.getHeight(), (float[]) data), m, czt[2]);
        }

        if (dataType == DataType.UINT2)
        {
            if(VariableExt.DataTypeIndex(baseDataType) > VariableExt.DataTypeIndex(DataType.UINT2)) // should be just CDF_FLOAT or CDF_DOUBLE both converted to FloatProcessor
                return ClippImage(new FloatProcessor(m.getWidth(), m.getHeight(), (int[]) data), m, czt[2]);
            else
            {
                short[] b = new short[m.getWidth() * m.getHeight()];

                for (int i = 0; i < b.length; ++i)
                {
                    b[i] = (short) ((int[]) data)[i];
                }
                return ClippImage(new ShortProcessor(m.getWidth(), m.getHeight(), b, LookUpTable.createGrayscaleColorModel(false)), m, czt[2]);
            }
        }

        if (dataType == DataType.UINT1)
        {
            if (VariableExt.DataTypeIndex(baseDataType) > VariableExt.DataTypeIndex(DataType.UINT2))
            { // convert to FloatProcessor
                float[] b = new float[m.getWidth() * m.getHeight()];
                for (int i = 0; i < b.length; ++i)
                {
                    b[i] = (float) ((short[]) data)[i];
                }                
                return ClippImage(new FloatProcessor(m.getWidth(), m.getHeight(), b), m, czt[2]);
            }
            else
                if (baseDataType == DataType.UINT2)
                { // convert to ShortProcessor
                    return ClippImage(new ShortProcessor(m.getWidth(), m.getHeight(), (short[]) data, LookUpTable.createGrayscaleColorModel(false)), m, czt[2]);
                }
                else
                { // ByteProcessor
                    byte[] b = new byte[m.getWidth() * m.getHeight()];

                    for (int i = 0; i < b.length; ++i)
                    {
                        b[i] = (byte) ((short[]) data)[i];
                    }

                    return ClippImage(new ByteProcessor(m.getWidth(), m.getHeight(), (byte[]) b), m , czt[2]);
                }
        }
        return null;
    }

    
    public ImageProcessor ClippImage(ImageProcessor image, MetaData m, int timeslot)
    {
    	if(m.getVar().isClippingPossible())
    	{
    		FluoClipping f = m.getMetaStruct(timeslot, 0, 0).fluoClipping;
    		VariableExtImg v = m.getVar();    		 
    		//image.setInterpolationMethod(ImageProcessor.BICUBIC);
    		image.scale(f.magnification, f.magnification);
    		image.setRoi((int)((image.getWidth() - 2) * f.xOffset / 100. / f.magnification), (int)((image.getHeight() + 2) * f.yOffset / 100. / f.magnification), v.getClippedWidth(), v.getClippedHeigth());
    		image = image.crop();
    		return image;
    	}
    	else
    	{
    		return image;
    	}    	
    }
    
    
    /** Returns the number of slices in this stack. */
    public int getSize()
    {
        return nSlices;
    }

    /** Returns the file name of the Nth image. */
    public String getSliceLabel(int n)
    {
        if(specialMode)
        	n = 1;
        
        n--;
        
        String result = "";
        
        int[] czt = getCZT(n);
        MetaData m = meta.elementAt(czt[0]);
        int r = m.getRecordIndex(czt[2], czt[3], czt[1]);
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
    
    public boolean isSpecialMode()
    {
    	return specialMode;
    }

    private int[] getCZT(int n)
	{
		int[] czt = new int[4];
		
    	if(specialMode)
    	{
    		czt[0] = 0;
    		czt[1] = 0;
    		czt[2] = 0;
    		czt[3] = n;
    		
    		return czt;
    	}
    	    	
		MetaData m = meta.firstElement();

		czt[3] = xyIndex;
				
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
        ImageWindow w = im.getWindow();
        
        mv = new MetaDataViewer();
        mv.setFocusable(false);
        w.add(mv);
        w.setSliderHeight(w.getSliderHeight() + mv.getPreferredSize().height + StackWindow.VGAP);
        w.pack();
        w.revalidate();
        w.repaint();        
    }
    
    
            
}