import ij.IJ;
import ij.ImageStack;
import ij.LookUpTable;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.Variable;

/**
This class represents an array of disk-resident images.
*/
class VirtualCDFStack extends ImageStack
{
	int nSlices;
	int used[];
	Variable var;
	
	/** Creates a new, empty virtual stack. 
	 * @throws CDFException */
	public VirtualCDFStack(Variable _var) throws CDFException 
	{		
		super((int)_var.getDimSizes()[0], (int)_var.getDimSizes()[1]);
		var = _var;
		
		nSlices = (int) var.getNumWrittenRecords();
				
		used = new int[nSlices];
		for(int i = 0; i < nSlices; ++i)
			used[i] = i;
		
		IJ.showStatus("VirtualCdfStack " + nSlices);
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
		if (n < 1 || n > nSlices)
			throw new IllegalArgumentException("Argument out of range: " + n);

		if (nSlices < 1)
			return;
		
		for (int i = n; i < nSlices; i++)
			used[i - 1] = used[i];		
		nSlices--;
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
		if (ip!=null)
			return ip.getPixels();
		else
			return null;
	}		
	
	 /** Assigns a pixel array to the specified slice,
		were 1<=n<=nslices. */
	public void setPixels(Object pixels, int n) 
	{
	}

   /** Returns an ImageProcessor for the specified slice,
		were 1<=n<=nslices. Returns null if the stack is empty.
	*/
	public ImageProcessor getProcessor(int n) 
	{
		n -= 1;
		if(n < 0 || n >= nSlices)
			return null;
		
		long dataType = var.getDataType();			
		
		Object data;
		try 
		{
			data = var.getRecordObject(used[n]).getRawData();
		} 
		catch (CDFException e) 
		{
			// TODO Auto-generated catch block
			System.err.println(e.toString());
			e.printStackTrace();
			return null;
		}
				
		if(dataType == CDFConstants.CDF_DOUBLE)	
		{			
			return new FloatProcessor(getWidth(), getHeight(), (double[]) data);
		}
		
		if(dataType == CDFConstants.CDF_FLOAT)	
		{			
			return new FloatProcessor(getWidth(), getHeight(), (float[]) data);
		}
		
		if(dataType == CDFConstants.CDF_UINT2)	
		{
			int pixels = getWidth() * getHeight();
			short[] b = new short[pixels];
						
			for(int i = 0; i < pixels; ++i)
			{				
				b[i] = (short) ((int[])data)[i]; 
			}
			return new ShortProcessor(getWidth(), getHeight(), b, LookUpTable.createGrayscaleColorModel(false));
		}
		
		if(dataType == CDFConstants.CDF_UINT1)	
		{			
			int pixels = getWidth() * getHeight();
			byte[] b = new byte[pixels];
						
			for(int i = 0; i < pixels; ++i)
			{				
				b[i] = (byte) ((short[])data)[i]; 
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
		 return "slice " + (n - 1);
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
		
}