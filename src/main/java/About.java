import javax.swing.JOptionPane;

import ij.plugin.PlugIn;

public final class About implements PlugIn
{
    public void run(String arg)
    {
        about();
    }

    public static void about()
    {
        String msg = "<html><p>TESCAN CDF Plugin for ImageJ</p>" + 
                "<p>Release: 1.0</p>" + 
                "<p>Copyright (C) 2017</p>" + 
                "<p><em><a href=\"https://github.com/Lukas-kV/TescanCDFOpener\">TescanCDF</a></em> plugin suitable for reading 6D hyperstacks <br>from custom CDF files (<a href=\"https://cdf.sci.gsfc.nasa.gov/\">CDF &copy; NASA/Goddard Space Flight Center</a>) <br> produced by <a href=\"http://q-phase.tescan.com/\">TESCAN Q-PHASE</a> microscope.</p>" + 
                "<ul><li>https://github.com/Lukas-kV/TescanCDFOpener</li>" + 
                "<li>https://github.com/mbtaylor/jcdf.git</li>" +
                "<li>https://cdf.sci.gsfc.nasa.gov</li>" + 
                "<li>http://q-phase.tescan.com</li>" + 
                "  </ul>" + 
                "<p>Author: Lukas Kvasnica</p>";

        JOptionPane.showMessageDialog(null, msg, "TESCAN CDF Plugin for ImageJ", 1, null);
    }

    public static void main(String[] args)
    {
        about();
        System.exit(0);
    }

}