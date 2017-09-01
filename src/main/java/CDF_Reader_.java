
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
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Vector;

// NASA CDF dependencies 
import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.Variable;

public class CDF_Reader_ implements PlugIn
{
    static 
    {
        String libDirs = System.getProperty("java.library.path");
        
        String libDir = "./lib/win64";
        if(!libDirs.isEmpty())
        {
            libDir = libDirs.split(";")[0];
        }
        
        String plugDir =  System.getProperty("plugins.dir") + "/plugins";       
        String plugin = plugDir + "/CDF_Reader_.jar";
        
        System.out.println("lib paths = " + libDirs);
        System.out.println("lib path = " + libDir);
        System.out.println("plugins = " + plugDir);
        System.out.println("plugin = " + plugin);
        
        File theDir = new File(libDir);
        boolean result = false;
        try
        {
            result = theDir.mkdirs();            
        }
        catch (SecurityException e)
        {
            System.err.println(e.toString());
            e.printStackTrace();
        }
        
        boolean copy = false;
        if (result)
        {
            copy = true;
            System.out.println("DIR created");
        }
        else
        {
            System.out.println("Checking library date");
            
            BasicFileAttributes pattr = null, lattr = null, l2attr = null;
            
            File cdfnl = new File(libDir + "/cdfNativeLibrary.dll");
            File cdfdll = new File(libDir + "/dllcdf.dll");
            
            if(cdfnl.exists() && cdfnl.isFile() && cdfdll.exists() && cdfdll.isFile())
            {
                try
                {
                    pattr = Files.readAttributes(new File(plugin).toPath(), BasicFileAttributes.class);
                    lattr = Files.readAttributes(cdfnl.toPath(), BasicFileAttributes.class);
                    l2attr = Files.readAttributes(cdfdll.toPath(), BasicFileAttributes.class);

                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                }

                copy = (pattr.lastModifiedTime().compareTo(lattr.lastModifiedTime()) > 0)
                        || (pattr.lastModifiedTime().compareTo(l2attr.lastModifiedTime()) > 0);

                System.out.println("pT  = " + pattr.lastModifiedTime().toString());
                System.out.println("lT  = " + lattr.lastModifiedTime().toString());
                System.out.println("l2T = " + l2attr.lastModifiedTime().toString());
            }
            else
                copy = true;
            
            
            if(copy)
                System.out.println("Extracting new libs");
            else
                System.out.println("Libs OK");                
        }
        
        if (copy)
        {
            try
            {
                CopyLib("win32-x86-64/dllcdf.dll", libDir);
                CopyLib("win32-x86-64/cdfNativeLibrary.dll", libDir);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
//        System.loadLibrary(new File(libDir + "/dllcdf.dll").getAbsolutePath());                
//        System.loadLibrary(new File(libDir + "/cdfNativeLibrary.dll").getAbsolutePath());        
    }
       
    
    public static void CopyLib(String path, String destDir) throws IOException
    {
        System.out.println("extracting file " + path + " to dir " + destDir); 
                
        byte[] buffer = new byte[1024];
        int readBytes;
 
        // Open and check input stream
        InputStream is = CDF_Reader_.class.getResourceAsStream(path);
        if (is == null) 
        {
            throw new FileNotFoundException("File " + path + " was not found inside JAR.");
        }
 
        String[] parts = path.split("/");
        String filename = (parts.length > 1) ? parts[parts.length - 1] : null;
        
        File of = new File(destDir + "/" + filename);
        // Open output stream and copy data between source file in JAR and
        // the temporary file
        OutputStream os = new FileOutputStream(of);
        try
        {
            while ((readBytes = is.read(buffer)) != -1)
            {
                os.write(buffer, 0, readBytes);
            }
        }
        finally
        {
            // If read/write fails, close streams safely before throwing an
            // exception
            os.close();
            is.close();
        }

//        FileTime time = FileTime.fromMillis( new Date().getTime());
//        Files.setAttribute(of.toPath(), "creationTime", time);        
    }
    
    public static File RenameFile(File file, String newname)
    {
        File ff = null;
        Path source = file.toPath();
        try 
        {            
            Path f = source.resolveSibling(newname);
            ff = f.toFile();
            Files.move(source, f);
        } 
        catch (IOException e) 
        {
             e.printStackTrace();
        }
            
        return ff;
    }
    
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
                            name += rec + " records ";

                        int xy = MetaData.getXYCount(var);

                        if (xy > 1)
                            name += " at " + xy + " positions ";

                        name += VarType(var.getDataType());

                        labels[i] = name;
                        def[i] = false;
                    }
                    
                    if(images.size() > 1)
                    	def[1] = true;
                    
                    gd.addCheckboxGroup(images.size(), 1, labels, def, headings);

                    int cnt = MetaData.getXYCount(images.firstElement());
                    if (cnt > 1)
                    {
                        int len = cnt + 1;
                        headings[0] = "Positions XY:";
                        
                        labels = new String[len];
                        def = new boolean[len];
                        
                        labels[0] = "Single Hyperstack";
                        def[0] = true;
                        
                        for (int i = 0; i < cnt; ++i)
                        {
                            labels[i + 1] = Integer.toString(i);
                            def[i + 1] = false;
                        }

                        int cols = 4;
                        int z = len % cols;
                        if (len < cols)
                            gd.addCheckboxGroup(1, len, labels, def, headings);
                        else
                            gd.addCheckboxGroup((z == 0) ? (len / 4) : (cnt / 4 + 1), 4, labels, def, headings);
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

                if (cnt == 1 || gd.getNextBoolean()) // single hyperstack
                {
                    createStackWindow(new VirtualCDFStack(width, height, m));
                }
                else
                    for (int i = 0; i < cnt; ++i)
                    {
                        if (gd.getNextBoolean())
                            createStackWindow(new VirtualCDFStack(width, height, m, i));
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

    private void createStackWindow(VirtualCDFStack stack)
    {
        if (stack != null && stack.getSize() > 0)
        {
            Vector<MetaData> m = stack.getMetaData();
            
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

            stack.setUpXYScrollBar(imp2);
            stack.setUpMetaDataPanel(imp2);
            
            WindowManager.setCurrentWindow(imp2.getWindow());

        }
    }
    
}
