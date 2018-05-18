import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import uk.ac.bristol.star.cdf.CdfContent;
import uk.ac.bristol.star.cdf.CdfReader;

public class CDF_Reader_ implements PlugIn
{	
	static public String Clipped = "Clipped:";
	static public String Hologram = "Hologram";
	static public String CompensatedPhase = "Compensated phase";
	
	
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
        	        	
        	VariableExt varList[] = VariableExt.Vars(content);
        	
            Vector<VariableExtImg> images = new Vector<VariableExtImg>();
            Vector<VariableExt> meta = new Vector<VariableExt>();

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
                    VariableExt var = varList[i];
                    long j = var.getNumDims();
                    int[] dimesions = var.getDimSizes();

                    if (j == 2 && dimesions[0] > 10 && dimesions[1] > 10)
                        images.add(new VariableExtImg(var));
                    else
                        meta.add(var);
                }

                int hh = 0, hw = 0;
                boolean  containsClippedFluo = false;
                
                if (images.size() > 0)
                {
                    // gd.addMessage("Image data:", new Font("Hevletica", Font.BOLD, 12));

                    String[] headings = { "Image data:" };
                    String[] labels = new String[images.size()];
                    boolean[] def = new boolean[images.size()];
                    
                    
                    for (int i = 0; i < images.size(); ++i)
                    {
                        VariableExtImg var = images.get(i);
                        if(var.getName().equals(Hologram))
                        {
                        	hh = var.getHeight();
                        	hw = var.getWidth();
                        }
                        else                        	
                        	if(var.getName().contains(Clipped) && var.getDataType().getName().equals("UINT2"))
                        	{
                        		containsClippedFluo = true;
                        	}
                        
                    }

                    for (int i = 0; i < images.size(); ++i)
                    {
                        VariableExtImg var = images.get(i);
                        
                        String name = var.getName() + " (" + var.getWidth() + "x" + var.getHeight() + ")                ";

                        long rec = var.getNumWrittenRecords();
                        if (rec > 1)
                            name += rec + " records ";

                        int xy = MetaData.getXYCount(var);

                        if (xy > 1)
                            name += " at " + xy + " positions ";

                        name += var.getDataType().getName();
                        //name += var.getSummary();
                        
                        // is possible to clipp raw FLUO image
                        var.setClippingPossible(!containsClippedFluo && !var.getName().equals(Hologram) && var.getDataType().getName().equals("UINT2")  
                        		&& hh > 0 && hw > 0 && hh < var.getHeight() && hw < var.getWidth());
                        	                    	                                            
                        labels[i] = name;                                              
                        def[i] = (var.getName().equals(CompensatedPhase));
                    }
                    
//                    if(images.size() > 1)
//                   	def[1] = true;
                    
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
                for (VariableExt var : meta)
                {
                    long j = var.getNumDims();
                    int[] dimesions = var.getDimSizes();

                    //System.out.println("meta dim: " + j + " is Zvar " + var.isZVariable());
                    
                    name += "    " + var.getName() + "              " + var.getDataType().getName() + " ";
                    if( j!=0 ) name += "(";
                    for (int k = 0; k < j; ++k)
                    {
                        if (k != 0)
                            name += "x";
                        name += dimesions[k];
                    }
                    if( j!=0 ) name += ") ";

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

            for (VariableExtImg var : images)
            {

                if (!(gd.getNextBoolean()))
                    continue;

                if (m.size() == 0)
                {                    
                    width = var.getWidth();
                    height = var.getHeight();
                    var.setClippingPossible(false);
                    m.add(new MetaData(var, meta));
                }
                else
                {
                	boolean equ = (width == var.getWidth() && height == var.getHeight());
                	
                    if ( equ || var.isClippingPossible())
                    {       
                    	if(equ)
                    		var.setClippingPossible(false);
                    	else
                    		var.setClippingDimensions(width, height);
                    		
                        m.add(new MetaData(var, meta));
                    }
                    else
                    {
                        IJ.error("Channel " + var.getName()
                                + String.format(" (%dx%d)", var.getWidth(), var.getHeight())
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
            stack.getSliceLabel(1);
            
            WindowManager.setCurrentWindow(imp2.getWindow());

        }
    }
    
}
