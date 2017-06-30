import java.util.Vector;

import com.google.gson.Gson;

import MetaStruct.MetaStruct;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.Variable;
import ij.measure.Calibration;

public class MetaData 
{
	private Variable var;
	private Variable time;
	private Variable globalTimeSlots;
	private Variable meta;
	private Variable position;	
	private int xyCount, zCount, records, timeslots, width, height;	
	
	public MetaData(Variable _var, Vector<Variable> metaList)
	{		
		Variable posHologram = null, metaHologram = null;
		var = _var;
		for (int i = 0; i < metaList.size(); ++i)
		{		
			Variable v = metaList.get(i);
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
		
		
		try 
		{
			zCount  = (Integer) var.getEntryData("zCount");
		} 
		catch (CDFException e) 
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
			zCount  = 1;
		}
		
		try 
		{
			xyCount = (Integer) var.getEntryData("xyCount");
		} 
		catch (CDFException e) 
		{
			// TODO Auto-generated catch block
			//e.printStackTrace();
			xyCount = 1;
		}
		
		try 
		{
			records = (int) var.getNumWrittenRecords();
		} 
		catch (CDFException e) 
		{
			// TODO Auto-generated catch block
			records = 0;
			e.printStackTrace();
		}
		
		timeslots = records / (zCount * xyCount);
		
        width = (int) var.getDimSizes()[1];
        height = (int) var.getDimSizes()[0];
		
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
	
	public String getGlobalTime(long r)
	{
		String result = null;
		
		if(globalTimeSlots != null)
		{
			try 
			{
				result = ((String[]) globalTimeSlots.getRecordObject(r).getData())[0];
			} 
			catch (CDFException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return result;		
	}
	
	public String getTime(int timeslot, int xyIndex, int zIndex) 
	{
		return getTime(getRecordIndex(timeslot, xyIndex, zIndex));
	}
		
	public String getTime(long r) 
	{
		String result = null;
		
		if(time != null)
		{
			try 
			{
				result = ((String[]) time.getRecordObject(r).getData())[0];
			} 
			catch (CDFException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return result;
	} 
	
	public String getMetaString(int timeslot, int xyIndex, int zIndex) 
	{
		return getMetaString(getRecordIndex(timeslot, xyIndex, zIndex));
	}
	
	public String getMetaString(long r) 
	{
		String result = null;
		
		if(meta != null)
		{
			try 
			{
				result = ((String[]) meta.getRecordObject(r).getData())[0];
			} 
			catch (CDFException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();				
			}
			
		}
		return result;
	} 
	
	public MetaStruct getMetaStruct(int timeslot, int xyIndex, int zIndex)
	{
	    return getMetaStruct(getRecordIndex(timeslot, xyIndex, zIndex));
	}
	
	public MetaStruct getMetaStruct(long r)
	{
	    String m = getMetaString(r);
	    if(m != null)
	    {
	        Gson parser = new Gson();
	        return parser.fromJson(m, MetaStruct.class);
	    }
	    return null;
	}
	
    public Calibration getCalibration(long r)
    {
        MetaStruct m = getMetaStruct(r);

        if (m != null)
        {

            Calibration c = new Calibration();
            System.out.println("Calibration loaded from MetaStruc version" + m.metaVersion);

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
	
	public double[] get3DPositions(long r) 
	{
		double[] result = null;
		
		if(position != null)
		{
			try 
			{
				result = new double[3];				
				result = ((double[][]) position.getRecordObject(r).getData())[0];
			} 
			catch (CDFException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return result;
	} 
	
    static int getXYCount(Variable var)
    {
        int c;        
        try 
        {
            c = (Integer) var.getEntryData("xyCount");
        } 
        catch (CDFException e) 
        {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            c = 1;
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
