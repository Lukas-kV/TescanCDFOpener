import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import uk.ac.bristol.star.cdf.AttributeEntry;
import uk.ac.bristol.star.cdf.CdfContent;
import uk.ac.bristol.star.cdf.DataType;
import uk.ac.bristol.star.cdf.Variable;
import uk.ac.bristol.star.cdf.VariableAttribute;


class VariableExt
{
	protected Variable var;
	protected Map<String, VariableAttribute> attributes;

	public VariableExt(Variable _var, Map<String, VariableAttribute> _attributes)
	{
		var = _var;
		attributes = _attributes;
	}
	
	public static Map<String, VariableAttribute> AttributesMap(VariableAttribute[] vatts)
	{
		Map<String, VariableAttribute> att = new HashMap<String, VariableAttribute>();
		for(VariableAttribute a : vatts)
		{
			att.put(a.getName(), a);
		}
		return att;
	}
	
	public static VariableExt[] Vars(CdfContent content)
	{
		Map<String, VariableAttribute> atts =  VariableExt.AttributesMap(content.getVariableAttributes());
		Variable[] vars = content.getVariables();
		VariableExt[] varList = new VariableExt[vars.length];
	
		for(int i = 0; i < vars.length; ++i)
		{
			varList[i] = new VariableExt(vars[i], atts);        		
		}
	
		return varList;
	}
	
	
	public Variable Var() { return var;}
	
	public java.lang.Object getEntryData(java.lang.String attrName) 
	{
		if(attributes.containsKey(attrName))
		{			
			AttributeEntry a = attributes.get(attrName).getEntry(var);
			if(a != null)
				return a.getShapedValue();
			else
			{
				System.out.println("CDF attribute \"" + attrName +"\" empty");
				return null;
			}
		}
		else
		{
			 System.out.println("CDF attribute \"" + attrName +"\" missing");
			 return null;
		}
	}
	
	public int getNumDims()
	{
        int[] dimesions = var.getShaper().getDimSizes();
        return dimesions.length;		
	}
	
	public int[] getDimSizes()
	{
		return var.getShaper().getDimSizes();
	}
	
	public String getName()
	{
		return var.getName();
	}
	
	public int getNumWrittenRecords()
	{
		return var.getRecordCount();	
	}
	
	public java.lang.Object getRecordObject(int r)
	{			
        try {
			return var.readShapedRecord( r, true, var.createRawValueArray() );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public String getRecordString(int r)
	{
		return ((String)getRecordObject(r)).split("\0", 2)[0].trim();
	}
		
	public DataType getDataType()
	{
		return var.getDataType();
	}
				
	static public int DataTypeIndex(DataType type)
	{		
	     switch (type.getName()) 
	     {
	     case "INT1"		:  return 1;
	     case "INT2"        :  return 2 ;
	     case "INT4"        :  return 4 ;
	     case "INT8"        :  return 8 ;
	     case "UINT1"       :  return 11;
	     case "UINT2"       :  return 12;
	     case "UINT4"       :  return 14;
	     case "BYTE"        :  return 41;
	     case "REAL4"       :  return 21;
	     case "REAL8"       :  return 22;
	     case "FLOAT"       :  return 44;
	     case "DOUBLE"      :  return 45;
	     case "EPOCH"       :  return 31;
	     case "EPOCH16"     :  return 32;
	     case "TIME_TT2000" :  return 33;
	     case "CHAR"        :  return 51;
	     case "UCHAR"       :  return 52;
	     }
	     return 1;
	}
}
