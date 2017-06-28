import java.util.Vector;

import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.Variable;

public class MetaData 
{
	private Variable time;
	private Variable meta;
	private Variable position;
	private int xyCount, zCount, records, timeslots;
	
	
	public MetaData(Variable var, Vector<Variable> metaList)
	{		
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
		
		
		System.out.println(var.getName() + " (xyCount=" + xyCount + "  zCount=" + zCount + " records=" + records + " timeslots=" + timeslots +")");
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

	public int getRecordIndex(int timeslot)
	{
		return xyCount * zCount * timeslot;
	}
		
}
