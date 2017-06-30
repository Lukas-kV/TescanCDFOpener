
/** <a href="http://www.cpupk.com/decompiler">Eclipse Class Decompiler</a> plugin, Copyright (c) 2017 Chen Chao. **/
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;

import java.awt.Font;
import java.util.Vector;

// NASA CDF dependencies 
import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.Variable;

public class CDF_Reader_ implements PlugIn
{

    public static String VarType(long dataType)
    {

        if (dataType == CDFConstants.CDF_BYTE) // java.lang.Byte
            return "CDF_BYTE (Byte)";

        if (dataType == CDFConstants.CDF_INT1) // java.lang.Byte
            return "CDF_INT1 (Byte)";

        if (dataType == CDFConstants.CDF_UINT1) // java.lang.Short
            return "CDF_UINT1 (Short)";

        if (dataType == CDFConstants.CDF_INT2) // java.lang.Short
            return "CDF_INT2 (Short)";

        if (dataType == CDFConstants.CDF_UINT2) // java.lang.Integer
            return "CDF_UINT2 (Integer)";

        if (dataType == CDFConstants.CDF_INT4) // java.lang.Integer
            return "CDF_INT4 (Integer)";

        if (dataType == CDFConstants.CDF_UINT4) // java.lang.Long
            return "CDF_UINT4 (Long)";

        if (dataType == CDFConstants.CDF_INT8) // java.lang.Long
            return "CDF_INT8 (Long)";

        if (dataType == CDFConstants.CDF_FLOAT) // java.lang.Float
            return "CDF_FLOAT (Float)";

        if (dataType == CDFConstants.CDF_REAL4) // java.lang.Float
            return "CDF_REAL4 (Float)";

        if (dataType == CDFConstants.CDF_DOUBLE) // java.lang.Double
            return "CDF_DOUBLE (Double)";

        if (dataType == CDFConstants.CDF_REAL8) // java.lang.Double
            return "CDF_REAL8 (Double)";

        if (dataType == CDFConstants.CDF_EPOCH) // java.lang.Double
            return "CDF_EPOCH (Double)";

        if (dataType == CDFConstants.CDF_EPOCH16) // java.lang.Double
            return "CDF_EPOCH16 (Double)";

        if (dataType == CDFConstants.CDF_TIME_TT2000) // java.lang.Long
            return "CDF_TIME_TT2000 (Long)";

        if (dataType == CDFConstants.CDF_CHAR) // java.lang.String
            return "CDF_CHAR (String)";

        if (dataType == CDFConstants.CDF_UCHAR) // java.lang.String
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

        // PrintStream localPrintStream = System.out;

        CDF cdfFile = null;
        try
        {
            cdfFile = CDF.open(str1 + str2);
            Vector<Variable> varList = cdfFile.getVariables();
            Vector<Variable> images = new Vector<Variable>();
            Vector<Variable> meta = new Vector<Variable>();

            GenericDialog gd = new GenericDialog("Variable Name Selection");
            gd.addMessage("Please select variables to be loaded.\n", new Font("Hevletica", Font.BOLD, 14));

            if (varList.size() < 1)
            {
                IJ.error("The file did not contain variables. (broken?)");
                cdfFile.close();
                return;
            }

            if (varList.size() < 2)
            {
                gd.addCheckbox("single variable", true);
            }
            else
            {
                for (int i = 0; i < varList.size(); ++i)
                {
                    Variable var = (Variable) varList.get(i);
                    long j = var.getNumDims();
                    long[] dimesions = var.getDimSizes();

                    if (j == 2 && dimesions[0] > 10 && dimesions[1] > 10)
                        images.add(var);
                    else
                        meta.add(var);
                }

                if (images.size() > 0)
                {
                    // gd.addMessage("Image data:", new Font("Hevletica", Font.BOLD, 12));

                    String[] headings = { "Image data:" };
                    String[] labels = new String[images.size()];
                    boolean[] def = new boolean[images.size()];

                    for (int i = 0; i < images.size(); ++i)
                    {
                        Variable var = images.get(i);
                        long j = var.getNumDims();
                        long[] dimesions = var.getDimSizes();

                        String name = var.getName() + " (";
                        for (int k = 0; k < j; ++k)
                        {
                            if (k != 0)
                                name += "x";
                            name += dimesions[k];
                        }
                        name += ")                ";

                        long rec = var.getNumWrittenRecords();
                        if (rec > 1)
                            name += rec + " records";

                        int xy = MetaData.getXYCount(var);

                        if (xy > 1)
                            name += " at " + xy + " positions ";

                        name += VarType(var.getDataType());

                        labels[i] = name;
                        def[i] = false;
                    }
                    
                    def[1] = true;
                    
                    gd.addCheckboxGroup(images.size(), 1, labels, def, headings);

                    int cnt = MetaData.getXYCount(images.firstElement());
                    if (cnt > 1)
                    {
                        headings[0] = "Positions XY:";

                        labels = new String[cnt];
                        def = new boolean[cnt];
                        for (int i = 0; i < cnt; ++i)
                        {
                            labels[i] = Integer.toString(i);
                            def[i] = false;
                        }
                        def[0] = true;

                        int cols = 4;
                        int z = cnt % cols;
                        if (cnt < cols)
                            gd.addCheckboxGroup(1, cnt, labels, def, headings);
                        else
                            gd.addCheckboxGroup((z == 0) ? (cnt / 4) : (cnt / 4 + 1), 4, labels, def, headings);
                    }
                }

                String name = "";
                for (int i = 0; i < meta.size(); ++i)
                {
                    Variable var = meta.get(i);
                    long j = var.getNumDims();
                    long[] dimesions = var.getDimSizes();

                    name += "    " + var.getName() + "              " + VarType(var.getDataType()) + " (";
                    for (int k = 0; k < j; ++k)
                    {
                        if (k != 0)
                            name += "x";
                        name += dimesions[k];
                    }
                    name += ") ";

                    long rec = var.getNumWrittenRecords();
                    if (rec > 1)
                        name += rec + " records";

                    name += "\n";
                }

                if (meta.size() > 0)
                {
                    gd.setInsets(10, 0, 0);
                    gd.addMessage("Meta data:", new Font("Hevletica", Font.BOLD, 12));
                    gd.setInsets(0, 0, 0);
                    gd.addMessage(name);
                }

                gd.showDialog();

                if (gd.wasCanceled())
                {
                    IJ.error("Plugin canceled!");
                    return;
                }
            }

            Vector<MetaData> m = new Vector<MetaData>();
            int width = 0, height = 0;

            for (int i = 0; i < images.size(); ++i)
            {

                if (!(gd.getNextBoolean()))
                    continue;

                Variable var = (Variable) images.get(i);

                if (m.size() == 0)
                {
                    width = (int) var.getDimSizes()[1];
                    height = (int) var.getDimSizes()[0];
                    m.add(new MetaData(var, meta));
                }
                else
                {
                    if (width == (int) var.getDimSizes()[1] || height == (int) var.getDimSizes()[0])
                    {
                        m.add(new MetaData(var, meta));
                    }
                    else
                    {
                        IJ.error("Channel " + var.getName()
                                + String.format(" (%dx%d)", (int) var.getDimSizes()[1], (int) var.getDimSizes()[0])
                                + " varies in size from first read channel " + m.firstElement().getVar().getName()
                                + String.format(" (%dx%d)", width, height) + "it is going to be skipped");
                    }
                }
            }

            if (m.size() > 0)
            {
                int cnt = m.firstElement().getXYCount();
                for (int i = 0; i < cnt; ++i)
                {

                    if (!(gd.getNextBoolean()))
                        continue;

                    VirtualCDFStack stack = new VirtualCDFStack(width, height, m, i);
                    if (stack != null && stack.getSize() > 0)
                    {
                        ImagePlus imp = new ImagePlus("Q-PHASE", stack);
                        imp.setDimensions(m.size(), m.firstElement().getZCount(), m.firstElement().getTimeSlots());

                        System.out.println("dimensions " + imp.getNDimensions() + " stack size " + imp.getStackSize()
                                + String.format(" HS dims C=%d Z=%d T=%d", m.size(), m.firstElement().getZCount(),
                                        m.firstElement().getTimeSlots()));

                        ImagePlus imp2 = imp;
                        Calibration c = m.firstElement().getCalibration(0);
                        imp.setCalibration(c);

                        if (m.size() > 1 && imp.getBitDepth() != 24)
                        {
                            imp2 = new CompositeImage(imp, IJ.GRAYSCALE);
                            imp2.setCalibration(c);

                            imp2.setOverlay(imp.getOverlay());
                            imp.hide();
                        }

                        imp2.setOpenAsHyperStack(true);

                        imp2.show();
                        WindowManager.setCurrentWindow(imp2.getWindow());

                    }
                }
            }
        }
        catch (CDFException e)
        {
            // TODO Auto-generated catch block
            System.err.println(e.toString());
            e.printStackTrace();
        }
        catch (OutOfMemoryError localOutOfMemoryError)
        {
            IJ.outOfMemory("Load CDF");
        }

        // try {
        // if (cdfFile != null)
        // cdfFile.close();
        // } catch ( CDFException localIOException2) {
        // System.err.println("Error while closing '" + str1 + str2 + "'");
        // System.err.println(localIOException2);
        // IJ.showStatus("Error closing file.");
        // }

        IJ.showProgress(1.0D);
    }

}
