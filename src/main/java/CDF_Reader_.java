/** <a href="http://www.cpupk.com/decompiler">Eclipse Class Decompiler</a> plugin, Copyright (c) 2017 Chen Chao. **/
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import java.awt.Font;
import java.util.Vector;

// NASA CDF dependencies 
import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.Variable;

public class CDF_Reader_ implements PlugIn {
	
	public static String VarType(long dataType) {
	        
		 	if(dataType == CDFConstants.CDF_BYTE) //java.lang.Byte
		 		return "CDF_BYTE (Byte)";
		 
		 	if(dataType == CDFConstants.CDF_INT1) //java.lang.Byte
		 		return "CDF_INT1 (Byte)";
		 	
		 	if(dataType == CDFConstants.CDF_UINT1) //java.lang.Short
		 		return "CDF_UINT1 (Short)";
		 	
		 	if(dataType == CDFConstants.CDF_INT2) //java.lang.Short
		 		return "CDF_INT2 (Short)";
		 	
		 	if(dataType == CDFConstants.CDF_UINT2) //java.lang.Integer
		 		return "CDF_UINT2 (Integer)";
		 	
		 	if(dataType == CDFConstants.CDF_INT4) //java.lang.Integer
		 		return "CDF_INT4 (Integer)";
		 	
		 	if(dataType == CDFConstants.CDF_UINT4) //java.lang.Long
		 		return "CDF_UINT4 (Long)";
		 	
		 	if(dataType == CDFConstants.CDF_INT8) //java.lang.Long
		 		return "CDF_INT8 (Long)";
		 	
		 	if(dataType == CDFConstants.CDF_FLOAT) //java.lang.Float
		 		return "CDF_FLOAT (Float)";
		 	
		 	if(dataType == CDFConstants.CDF_REAL4)	//java.lang.Float
		 		return "CDF_REAL4 (Float)";
		 	
		 	if(dataType == CDFConstants.CDF_DOUBLE) //java.lang.Double
		 		return "CDF_DOUBLE (Double)";
		 	
		 	if(dataType == CDFConstants.CDF_REAL8) //java.lang.Double
		 		return "CDF_REAL8 (Double)";
		 	
		 	if(dataType == CDFConstants.CDF_EPOCH) //java.lang.Double
		 		return "CDF_EPOCH (Double)";
		 	
		 	if(dataType == CDFConstants.CDF_EPOCH16) //java.lang.Double
		 		return "CDF_EPOCH16 (Double)";
		 	
		 	if(dataType == CDFConstants.CDF_TIME_TT2000) //java.lang.Long
		 		return "CDF_TIME_TT2000 (Long)";
		 	
		 	if(dataType == CDFConstants.CDF_CHAR) //java.lang.String
		 		return "CDF_CHAR (String)";
		 	
		 	if(dataType == CDFConstants.CDF_UCHAR) //java.lang.String		 	
		 		return "CDF_UCHAR (String)";
		 	
		 	return "Unknown Type";
	 }
	 
	public void run(String paramString) 
	{
		OpenDialog localOpenDialog = new OpenDialog("Open CDF...", paramString);
		String str1 = localOpenDialog.getDirectory();
		String str2 = localOpenDialog.getFileName();
		if (str2 == null)
			return;
		if (str2 == "")
			return;
		IJ.showStatus("Loading CDF File: " + str1 + str2);

//		PrintStream localPrintStream = System.out;

		CDF localNetcdfFile = null;
		try 
		{
			localNetcdfFile = CDF.open(str1 + str2);
			Vector varList = localNetcdfFile.getVariables();
			Vector<Variable> images = new Vector<Variable>();
			Vector<Variable> meta = new Vector<Variable>();
			

			GenericDialog localGenericDialog = new GenericDialog("Variable Name Selection");
			localGenericDialog.addMessage("Please select variables to be loaded.\n", new Font ("Hevletica", Font.BOLD , 14));

			if (varList.size() < 1) {
				IJ.error("The file did not contain variables. (broken?)");
				localNetcdfFile.close();
				return;
			}
			
			if (varList.size() < 2) 
			{
				localGenericDialog.addCheckbox("single variable", true);
			}
			else 
			{				
				for (int i = 0; i < varList.size(); ++i)
				{					
					Variable var = (Variable) varList.get(i);
					long j = var.getNumDims();
					long[] dimesions = var.getDimSizes();
					
					if(j == 2 && dimesions[0] > 10 && dimesions[1] > 10)
						images.add(var);
					else
						meta.add(var);
				}
				
				if(images.size() > 0)
					localGenericDialog.addMessage("Image data:", new Font ("Hevletica", Font.BOLD , 12));
				
				for (int i = 0; i < images.size(); ++i) 
				{
					Variable var = (Variable) images.get(i);
					long j = var.getNumDims();
					long[] dimesions = var.getDimSizes();
					
					String name = var.getName() + "              " + VarType(var.getDataType()) + " (";					
					for (int k = 0; k < j; ++k) {
						if (k != 0)
							name += "x";
							name += dimesions[k];
					}
					name += ") ";

					long rec = var.getNumWrittenRecords();				
					if(rec > 1)
						name += rec + " records";

					localGenericDialog.addCheckbox(name, false);
				}
				
				
				String name = "";
				for (int i = 0; i < meta.size(); ++i) 
				{
					Variable var = (Variable) meta.get(i);
					long j = var.getNumDims();
					long[] dimesions = var.getDimSizes();
					
					name += "    " + var.getName() + "              " + VarType(var.getDataType()) + " (";					
					for (int k = 0; k < j; ++k) {
						if (k != 0)
							name += "x";
							name += dimesions[k];
					}
					name += ") ";

					long rec = var.getNumWrittenRecords();				
					if(rec > 1)
						name += rec + " records";
					
					name += "\n";					
				}

				if(meta.size() > 0)
				{
					localGenericDialog.addMessage("Meta data:", new Font ("Hevletica", Font.BOLD , 12));					
					localGenericDialog.addMessage(name);
				}
				
				localGenericDialog.showDialog();

				if (localGenericDialog.wasCanceled()) {
					IJ.error("Plugin canceled!");
					return;
				}
			}

			for (int i = 0; i < images.size(); ++i) 
			{
				
				if (!(localGenericDialog.getNextBoolean()))
					continue;
				
				Variable var = (Variable) images.get(i);				
				
				VirtualCDFStack stack = new VirtualCDFStack(var);
				
				if (stack != null && stack.getSize() > 0) 
				{					
					ImagePlus imp2 = new ImagePlus(var.getName(), stack);

//					if (imp2.getStackSize() == 1 && info1!=null)
//						imp2.setProperty("Info", info1);
					
					imp2.show();
				}					
			}
			

		} catch (CDFException e) {
			// TODO Auto-generated catch block
			System.err.println(e.toString());
			e.printStackTrace();
		}
		catch (OutOfMemoryError localOutOfMemoryError) {
			IJ.outOfMemory("Load CDF");
		}

//		try {
//			if (localNetcdfFile != null)
//				localNetcdfFile.close();
//		} catch ( CDFException localIOException2) {
//			System.err.println("Error while closing '" + str1 + str2 + "'");
//			System.err.println(localIOException2);
//			IJ.showStatus("Error closing file.");
//		}

		IJ.showProgress(1.0D);
	}
}



