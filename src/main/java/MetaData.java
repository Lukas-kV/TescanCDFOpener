import java.util.Vector;

import com.google.gson.Gson;

import MetaStruct.MetaStruct;
import ij.measure.Calibration;

public class MetaData 
{
	private VariableExtImg var;
	private VariableExt time;
	private VariableExt globalTimeSlots;
	private VariableExt meta;
	private VariableExt position;
	private int xyCount, zCount, records, timeslots, width, height;	
	
	public MetaData(VariableExtImg _var, Vector<VariableExt> metaList)
	{		
		VariableExt posHologram = null, metaHologram = null;
		var = _var;
		for (VariableExt v : metaList)
		{		
			String name = v.getName();
			if(name.contains(var.getName()))
			{				
				if(name.contains("time-"))
				{
					time = v;
					continue;
				}
				if(name.contains("meta-"))
				{
					meta = v;
					continue;
				}
				if(name.contains("pos-"))
				{
					position = v;
					continue;
				}				
				
			}
			
			if(name.contains("timestamp"))
				globalTimeSlots = v;
			
			if(name.contains("pos-Hologram"))
				posHologram = v;

			if(name.contains("meta-Hologram"))
				metaHologram = v;			
		}
		
		if(position == null)
		{
			position = posHologram;
			System.out.println("using Hologram position for channel " + var.getName()); 
		}
		
		if(meta == null)
		{
			meta = metaHologram;
			System.out.println("using Hologram metaData for channel " + var.getName()); 
		}
				
		Object o = var.getEntryData("zCount");
		zCount  =  (o!=null) ? (Integer) o:1;
		o = var.getEntryData("xyCount");
		xyCount  = (o!=null) ? (Integer) o:1;
		
		records = var.getNumWrittenRecords();
		
		timeslots = records / (zCount * xyCount);
		
        width  = var.getWidth();
        height = var.getHeight();
		
		System.out.println(var.getName() + " (xyCount=" + xyCount + "  zCount=" + zCount + " records=" + records + " timeslots=" + timeslots +") cl=" + var.isClippingPossible());
		//System.out.println(getMetaString(0));
		//System.out.println(getGlobalTime(0));
	}

	public VariableExtImg getVar()
	{
		return var;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	
	public String getGlobalTime(int timeslot, int xyIndex, int zIndex) 
	{
		return getGlobalTime(getRecordIndex(timeslot, xyIndex, zIndex));
	}
		
	public String getGlobalTime(int r)
	{
		String result = null;
		
		if(globalTimeSlots != null)
		{
			result = globalTimeSlots.getRecordString(r);			
		}
		return result;		
	}
	
	public String getTime(int timeslot, int xyIndex, int zIndex) 
	{
		return getTime(getRecordIndex(timeslot, xyIndex, zIndex));
	}
		
	public String getTime(int r) 
	{
		String result = null;
		
		if(time != null)
		{
			result = time.getRecordString(r);
		}
		return result;
	} 
	
	public String getMetaString(int timeslot, int xyIndex, int zIndex) 
	{
		return getMetaString(getRecordIndex(timeslot, xyIndex, zIndex));
	}
	
	public String getMetaString(int r) 
	{
		String result = null;
		
		if(meta != null)
		{
			result = meta.getRecordString(r);			
//			System.out.println(result);
		}
//		else
//		    System.out.println("empty meta variable");
		
		return result;
	} 
	
	public MetaStruct getMetaStruct(int timeslot, int xyIndex, int zIndex)
	{
	    return getMetaStruct(getRecordIndex(timeslot, xyIndex, zIndex));
	}
	
	public MetaStruct getMetaStruct(int r)
	{
	    String m = getMetaString(r);
	    if(m != null)
	    {
	        Gson parser = new Gson();
	        return parser.fromJson(m, MetaStruct.class);
	    }
	    return null;
	}
	
    public Calibration getCalibration(int r)
    {
        MetaStruct m = getMetaStruct(r);

        if (m != null)
        {

            Calibration c = new Calibration();
            System.out.println("Calibration loaded from MetaStruc version " + m.metaVersion);

            c.setUnit("um");
            c.pixelWidth = m.objectives.fov.x / width;
            c.pixelHeight = m.objectives.fov.y / height;
            return c;
        }

        return null;
    }
	
	
	public double[] get3DPositions(int timeslot, int xyIndex, int zIndex) 
	{
		return get3DPositions(getRecordIndex(timeslot, xyIndex, zIndex));
	}
	
	public double[] get3DPositions(int r) 
	{
		double[] result = null;
		
		if(position != null)
		{
			result = new double[3];				
			result = (double[]) position.getRecordObject(r);
			
		}
		return result;
	} 
	
    static int getXYCount(VariableExt var)
    {       
    	Object o = var.getEntryData("xyCount");
   		return (o!=null) ? (Integer) o:1;		
    }
		
	public int getXYCount()
	{
		return xyCount;
	}

	public int getZCount()
	{
		return zCount;
	}
	
	public int getTimeSlots()
	{
		return timeslots;
	}
	
	public int getRecordIndex(int timeslot, int xyIndex, int zIndex)
	{
		return xyCount * zCount * timeslot + xyIndex * zCount + zIndex;
	}
		
}
