
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
import java.util.Vector;

import uk.ac.bristol.star.cdf.CdfContent;
import uk.ac.bristol.star.cdf.CdfReader;
import uk.ac.bristol.star.cdf.DataType;
import uk.ac.bristol.star.cdf.Variable;
import uk.ac.bristol.star.cdf.VariableAttribute;

public class CDF_Reader_ implements PlugIn
{
	/*
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
    */
	
    public static String VarType(DataType dataType)
    {

        if (dataType == DataType.BYTE) // java.lang.Byte
            return "CDF_BYTE (Byte)";

        if (dataType == DataType.INT1) // java.lang.Byte
            return "CDF_INT1 (Byte)";

        if (dataType == DataType.UINT1) // java.lang.Short
            return "CDF_UINT1 (Short)";

        if (dataType == DataType.INT2) // java.lang.Short
            return "CDF_INT2 (Short)";

        if (dataType == DataType.UINT2) // java.lang.Integer
            return "CDF_UINT2 (Integer)";

        if (dataType == DataType.INT4) // java.lang.Integer
            return "CDF_INT4 (Integer)";

        if (dataType == DataType.UINT4) // java.lang.Long
            return "CDF_UINT4 (Long)";

        if (dataType == DataType.INT8) // java.lang.Long
            return "CDF_INT8 (Long)";

        if (dataType == DataType.FLOAT) // java.lang.Float
            return "CDF_FLOAT (Float)";

        if (dataType == DataType.REAL4) // java.lang.Float
            return "CDF_REAL4 (Float)";

        if (dataType == DataType.DOUBLE) // java.lang.Double
            return "CDF_DOUBLE (Double)";

        if (dataType == DataType.REAL8) // java.lang.Double
            return "CDF_REAL8 (Double)";

        if (dataType == DataType.EPOCH) // java.lang.Double
            return "CDF_EPOCH (Double)";

        if (dataType == DataType.EPOCH16) // java.lang.Double
            return "CDF_EPOCH16 (Double)";

        if (dataType == DataType.TIME_TT2000) // java.lang.Long
            return "CDF_TIME_TT2000 (Long)";

        if (dataType == DataType.CHAR) // java.lang.String
            return "CDF_CHAR (String)";

        if (dataType == DataType.UCHAR) // java.lang.String
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

        try
        {        	
        	CdfContent content = new CdfContent( new CdfReader(new File(str1 + str2)) );
        	
        	Variable[] varList = content.getVariables();
        	VariableAttribute[] vatts = content.getVariableAttributes();
        	
            Vector<Variable> images = new Vector<Variable>();
            Vector<Variable> meta = new Vector<Variable>();

            GenericDialog gd = new GenericDialog("Variable Name Selection");
            gd.addMessage("Please select variables to be loaded.\n", new Font("Hevletica", Font.BOLD, 14));

            if (varList.length < 1)
            {
                IJ.error("The file did not contain variables. (broken?)");
                return;
            }

            if (varList.length < 2)
            {
                gd.addCheckbox("single variable", true);
            }
            else
            {
                for (int i = 0; i < varList.length; ++i)
                {
                    Variable var = varList[i];
                    int[] dimesions = var.getShaper().getDimSizes();
                    long j = dimesions.length;

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
                        int[] dimesions = var.getShaper().getDimSizes();
                        long j = dimesions.length;

                        String name = var.getName() + " (";
                        for (int k = 0; k < j; ++k)
                        {
                            if (k != 0)
                                name += "x";
                            name += dimesions[k];
                        }
                        name += ")                ";

                        long rec = var.getRecordCount();
                        if (rec > 1)
                            name += rec + " records ";

                        int xy = MetaData.getXYCount(var, vatts);

                        if (xy > 1)
                            name += " at " + xy + " positions ";

                        name += VarType(var.getDataType());

                        labels[i] = name;
                        def[i] = false;
                    }
                    
                    if(images.size() > 1)
                    	def[1] = true;
                    
                    gd.addCheckboxGroup(images.size(), 1, labels, def, headings);

                    int cnt = MetaData.getXYCount(images.firstElement(), vatts);
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
                    int[] dimesions = var.getShaper().getDimSizes();
                    long j = dimesions.length;

                    name += "    " + var.getName() + "              " + VarType(var.getDataType()) + " (";
                    for (int k = 0; k < j; ++k)
                    {
                        if (k != 0)
                            name += "x";
                        name += dimesions[k];
                    }
                    name += ") ";

                    long rec = var.getRecordCount();
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
                    int[] dimesions = var.getShaper().getDimSizes();
                    width = dimesions[1];
                    height = dimesions[0];
                    m.add(new MetaData(var, meta, vatts));
                }
                else
                {
                	int[] dimesions = var.getShaper().getDimSizes();
                    if (width == dimesions[1] || height == dimesions[0])
                    {
                        m.add(new MetaData(var, meta, vatts));
                    }
                    else
                    {
                        IJ.error("Channel " + var.getName()
                                + String.format(" (%dx%d)", dimesions[1], dimesions[0])
                                + " varies in size from first read channel " + m.firstElement().getVar().getName()
                                + String.format(" (%dx%d)", width, height) + "it is going to be skipped");
                    }
                }
            }

            /*
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
            } */
        }
        catch (OutOfMemoryError localOutOfMemoryError)
        {
            IJ.outOfMemory("Load CDF");
        } 
        catch (IOException e) 
        {
			e.printStackTrace();
		}

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
