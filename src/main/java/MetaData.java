import java.io.IOException;
import java.util.Vector;

import com.google.gson.Gson;

import MetaStruct.MetaStruct;
import uk.ac.bristol.star.cdf.Variable;
import uk.ac.bristol.star.cdf.VariableAttribute;
import ij.measure.Calibration;

public class MetaData 
{
	private Variable var;
	private Variable time;
	private Variable globalTimeSlots;
	private Variable meta;
	private Variable position;
	private VariableAttribute[] vatts;
	private int xyCount, zCount, records, timeslots, width, height;	
	
	public MetaData(Variable _var, Vector<Variable> metaList, VariableAttribute[] _vatts)
	{		
		Variable posHologram = null, metaHologram = null;
		var = _var;
		vatts = _vatts;
		for (Variable v : metaList)
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
		
		
		zCount  = 1;
		xyCount = 1;
		
		for(VariableAttribute v : vatts)
		{
			switch(v.getName())
			{
				case "zCount":
					zCount  = (Integer) v.getEntry(var).getShapedValue();
				case "xyCount":
					xyCount = (Integer) v.getEntry(var).getShapedValue();
			}			
		}
		

		records = (int) var.getRecordCount();
		
		timeslots = records / (zCount * xyCount);
		
		int[] dimesions = var.getShaper().getDimSizes();
        width  = dimesions[1];
        height = dimesions[0];
		
		System.out.println(var.getName() + " (xyCount=" + xyCount + "  zCount=" + zCount + " records=" + records + " timeslots=" + timeslots +")");
	}

	public Variable getVar()
	{
		return var;
	}
	
	public String getGlobalTime(int timeslot, int xyIndex, int zIndex) 
	{
		return getGlobalTime(getRecordIndex(timeslot, xyIndex, zIndex));
	}
	
    private Object readShapedRecord( Variable var, int irec, boolean rowMajor ) 
    {
        try {
			return var.readShapedRecord( irec, rowMajor, var.createRawValueArray() );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
	
	public String getGlobalTime(int r)
	{
		String result = null;
		
		if(globalTimeSlots != null)
		{
			result = readShapedRecord( globalTimeSlots, r, true).toString();
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
			result = readShapedRecord(time, r, true).toString();
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
			result = readShapedRecord(meta, r, true).toString();			
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
			result = (double[]) readShapedRecord(position, r, true);
			
		}
		return result;
	} 
	
    static int getXYCount(Variable var, VariableAttribute[] _vatts)
    {
        int c = 1;
        		
		for(VariableAttribute v : _vatts)
		{
			if(v.getName() == "xyCount")
			{
				c  = (Integer) v.getEntry(var).getShapedValue();
				break;
			}
		}
		
        return c;
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
