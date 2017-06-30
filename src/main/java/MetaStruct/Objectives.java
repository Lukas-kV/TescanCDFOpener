package MetaStruct;

public class Objectives
{
    public Objectives()
    {
        AbsZ = RelZ = PieX = PieY = branchLength = 0.;
        lense = 0;
        NA = refractiveIndex = magnification = opticalMag = 1;
        objShutter = refShutter = false;
    }

    public double AbsZ, RelZ,        
        PieX, PieY, branchLength;
        
    public int lense;    
    public double NA, refractiveIndex, magnification, opticalMag; 
    public FOV fov;
    public boolean objShutter, refShutter;
    
}
