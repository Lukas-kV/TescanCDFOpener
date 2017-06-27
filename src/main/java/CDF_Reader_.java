/** <a href="http://www.cpupk.com/decompiler">Eclipse Class Decompiler</a> plugin, Copyright (c) 2017 Chen Chao. **/
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.image.ColorModel;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import gsfc.nssdc.cdf.Attribute;
import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.Variable;

/*import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable; */

public class CDF_Reader_ implements PlugIn {
	private int progressSteps;

	public CDF_Reader_() {
		this.progressSteps = 50;
	}

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
	 
	public void run(String paramString) {
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
		try {
			localNetcdfFile = CDF.open(str1 + str2);
			Vector localList = localNetcdfFile.getVariables();

			GenericDialog localGenericDialog = new GenericDialog("Variable Name Selection");
			localGenericDialog.addMessage("Please select variables to be loaded.\n");

			if (localList.size() < 1) {
				IJ.error("The file did not contain variables. (broken?)");
				localNetcdfFile.close();
				return;
			}
			
			if (localList.size() < 2) {
				localGenericDialog.addCheckbox("single variable", true);
			} else {
				for (int i = 0; i < localList.size(); ++i) {
					Variable var = (Variable) localList.get(i);
					long j = var.getNumDims();
					long[] dimesions = var.getDimSizes();
					
					String name = j + "D: " + var.getName() + "              " + VarType(var.getDataType()) + " (";					
					for (int k = 0; k < j; ++k) {
						if (k != 0)
							name += "x";
							name += dimesions[k];
					}
					name += ") ";

					long rec = var.getNumWrittenRecords();				
					if(rec > 1)
						name += "records " + rec;

					localGenericDialog.addCheckbox(name, false);
				}
				localGenericDialog.showDialog();

				if (localGenericDialog.wasCanceled()) {
					IJ.error("Plugin canceled!");
					return;
				}
			}

			for (int i = 0; i < localList.size(); ++i) {
				
				if (!(localGenericDialog.getNextBoolean()))
					continue;
				
				Variable var = (Variable) localList.get(i);
				
				long j = var.getNumDims();
				
				long dataType = var.getDataType();
				long[] dimesions = var.getDimSizes();

				System.out.println("Reading Variable: " + var.getName());
				System.out.println("   Rank = " + j + ", Data-type = " + VarType(dataType));

				System.out.print("   Extent in px (level,row,col):");
				for (int k = 0; k < j; ++k)
					System.out.print(" " + dimesions[k]);
				System.out.println("");
				IJ.showStatus("Reading Variable: " + var.getName() + " (" + dimesions[0] + " slices)");

				//Vector attrs = localVariable.getAttributes();
				//Attribute localAttribute = localVariable.findAttribute("element_size_um");
				double[] elementSize = new double[3];
				elementSize[0] = 1.0D;
				elementSize[1] = 1.0D;
				elementSize[2] = 1.0D;

/*				if (localAttribute == null) {
					arrayOfDouble[0] = 1.0D;
					arrayOfDouble[1] = 1.0D;
					arrayOfDouble[2] = 1.0D;
				} else {
					arrayOfDouble[0] = localAttribute.getNumericValue(0).doubleValue();
					arrayOfDouble[1] = localAttribute.getNumericValue(1).doubleValue();
					arrayOfDouble[2] = localAttribute.getNumericValue(2).doubleValue();
				}; */
				
				System.out.println("   Element-Size in um (level,row,col): " + elementSize[0] + ", "
						+ elementSize[1] + ", " + elementSize[2]);

				read2DImageStack(var, str1 + str2);
				
	/*			int l = (int) (dimesions[0] / this.progressSteps);
				if (l < 1)
					l = 1;
				
				if (j == 3) {					
					ImageStack stack = new ImageStack((int)dimesions[2], (int)dimesions[1]);

						for (int i1 = 0; i1 < dimesions[0]; ++i1) {
							
							if (i1 % l == 0)
								IJ.showProgress(i1 / dimesions[0]);
							
							localObject4[0] = i1;
							
							localObject6 = var.read(localObject4, sliceDim);
							if ((localObject1 == DataType.DOUBLE) || (localObject1 == DataType.FLOAT)
									|| (localObject1 == DataType.INT) || (localObject1 == DataType.LONG)) 
							{
								((ImageStack) localObject2).addSlice(null, ((Array) localObject6).get1DJavaArray(Float.TYPE));
								
							} else {
								
								((ImageStack) localObject2).addSlice(null, ((Array) localObject6).copyTo1DJavaArray());
								
							}
						}
						IJ.showProgress(1.0D);
						localObject5 = new ImagePlus(str1 + str2 + " " + localVariable.getName(),
								(ImageStack) localObject2);

						((ImagePlus) localObject5).getCalibration().pixelDepth = arrayOfDouble[0];
						((ImagePlus) localObject5).getCalibration().pixelHeight = arrayOfDouble[1];
						((ImagePlus) localObject5).getCalibration().pixelWidth = arrayOfDouble[2];

						multiStackBrightnessAdjust((ImageStack) localObject2, (ImagePlus) localObject5, (int)dimesions[0]);

						((ImagePlus) localObject5).show();
						((ImagePlus) localObject5).updateStatusbarValue();
					} else if (j == 2) {
						IJ.showProgress(0.0D);
						localObject2 = localVariable.read();
						sliceDim = new ImageStack((int)dimesions[1], (int)dimesions[0]);
						if ((localObject1 == DataType.DOUBLE) || (localObject1 == DataType.FLOAT)
								|| (localObject1 == DataType.INT) || (localObject1 == DataType.LONG)) {
							((ImageStack) sliceDim).addSlice(null,
									((Array) localObject2).get1DJavaArray(Float.TYPE));
						} else {
							((ImageStack) sliceDim).addSlice(null, ((Array) localObject2).copyTo1DJavaArray());
						}
						IJ.showProgress(1.0D);
						localObject4 = new ImagePlus(str1 + str2 + " " + localVariable.getName(),
								(ImageStack) sliceDim);

						((ImagePlus) localObject4).getProcessor().resetMinAndMax();
						((ImagePlus) localObject4).show();

						localObject5 = ((ImagePlus) localObject4).getProcessor();
						int i3 = ((ImagePlus) localObject4).getType();

						double d1 = ((ImageProcessor) localObject5).getMax();
						double d2 = ((ImageProcessor) localObject5).getMin();

						System.out.println("   Min = " + d2 + ", Max = " + d1);
						((ImageProcessor) localObject5).setMinAndMax(d2, d1);
						((ImagePlus) localObject4).updateAndDraw();
						((ImagePlus) localObject4).show();
						((ImagePlus) localObject4).updateStatusbarValue();
					} else {
						System.err.println("   Error: CDF Variable Dimensions " + j + " not supported (yet).");
						IJ.showStatus("Variable Dimension " + j + " not supported");
					} */
				}
			

		} catch (CDFException e) {
			// TODO Auto-generated catch block
			System.err.println(e.toString());
			e.printStackTrace();
		}
		catch (OutOfMemoryError localOutOfMemoryError) {
			IJ.outOfMemory("Load NetCDF");
		}

		try {
			if (localNetcdfFile != null)
				localNetcdfFile.close();
		} catch ( CDFException localIOException2) {
			System.err.println("Error while closing '" + str1 + str2 + "'");
			System.err.println(localIOException2);
			IJ.showStatus("Error closing file.");
		}

		IJ.showProgress(1.0D);
	}

	
	void read2DImageStack(Variable var, String header) throws CDFException
	{
		long j = var.getNumDims();
		if(j != 2)
		{
			System.err.println("Variable" + var.getName() + " is not 2D image");			
			return;
		}
		long[] dim = var.getDimSizes();		
		long rec = var.getNumWrittenRecords();
		
		IJ.showStatus("Loading CDF stack: " + dim[0] + "x" + dim[1] +"x"+ rec);
		
		ImageStack stack = new ImageStack((int)dim[0], (int)dim[1]);
		
		long dataType = var.getDataType();
		
		IJ.showProgress(0.0D);
		
		if(dataType == CDFConstants.CDF_DOUBLE)		
			for (int i1 = 0; i1 < rec; ++i1) {			
				double[] d =  (double[])var.getRecordObject(i1).getRawData();	
				stack.addSlice(null, d);
				IJ.showProgress(i1 / (double)rec);
		}
		else
			if(dataType == CDFConstants.CDF_UINT2)		
				for (int i1 = 0; i1 < rec; ++i1) {			
					int[][] d =  (int[][])var.getRecord(i1);	
					stack.addSlice(null, d);
					IJ.showProgress(i1 / (double)rec);
			}

		
		ImagePlus imPlus = new ImagePlus(header + " " + var.getName(), stack);

		multiStackBrightnessAdjust(stack, imPlus, (int)rec);

		imPlus.show();
		imPlus.updateStatusbarValue();		
	}
		
	
	int byteToUnsignedByte(int paramInt) {
		if (paramInt < 0)
			return (256 + paramInt);
		return paramInt;
	}

	void multiStackBrightnessAdjust(ImageStack paramImageStack, ImagePlus paramImagePlus, int paramInt) {
		paramImagePlus.show();

		IJ.showStatus("Adjusting Brightness/Contrast");
		ImageProcessor[] arrayOfImageProcessor = new ImageProcessor[paramInt];
		//int i = paramImagePlus.getType();

		int j = paramInt / this.progressSteps;
		if (j < 1)
			j = 1;

		double d1 = -3.402823466385289E+038D;
		double d2 = 3.402823466385289E+038D;
		for (int k = 0; k < paramInt; ++k) {
			if (k % j == 0) {
				IJ.showProgress(k / paramInt);
			}
			if (k == 0)
				arrayOfImageProcessor[0] = paramImagePlus.getProcessor();
			else {
				arrayOfImageProcessor[k] = paramImageStack.getProcessor(k + 1);
			}
			d1 = Math.max(d1, arrayOfImageProcessor[k].getMax());
			d2 = Math.min(d2, arrayOfImageProcessor[k].getMin());
		}

		System.out.println("   Min = " + d2 + ", Max = " + d1);

		for (int l = 0; l < paramInt; ++l) {
			arrayOfImageProcessor[l].setMinAndMax(d2, d1);
			paramImagePlus.updateAndDraw();
		}
		IJ.showProgress(1.0D);
	}
}



